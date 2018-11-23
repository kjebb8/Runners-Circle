//
//  RunGroupManager.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation
import Firebase
import CoreLocation

protocol RunGroupManagerDelegate {
    
    func updateRunners(_ runners: [Runner])
    func notifyDatabaseDisconnect()
}

var runGroupManager: RunGroupManager? //Only 1 global instance

class RunGroupManager {
    
    private var runGroup: RunGroup
    
    private var runnersDictionary = Dictionary<String, Runner>()
    
    private var delegateVC: RunGroupManagerDelegate?
    
    var databaseReadPeriod: Int = 4 //Number of seconds between database reads
    
    init(_ group: RunGroup) {
        runGroup = group
    }
    
    
    //MARK: - Get/Set Private Variable Methods
    
    func ownerID() -> String {
        return runGroup.ownerID
    }
    
    
    func userID() -> String? {
        return Auth.auth().currentUser?.uid
    }
    
    
    func runGroupName() -> String {
        return runGroup.name
    }
    
    
    func setDelegateAs(_ delegate: RunGroupManagerDelegate?) {
        
        delegateVC = delegate
        delegateVC?.updateRunners(Array(runnersDictionary.values))
    }
    
    
    //MARK: - Adding and Retrieving from Firebase
    
    func loadActiveRunners() { //Refreshes changes to active members
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID)
        
        //Get the userIDs of all the active users
        membersDB.observeSingleEvent(of: .value) { (membersSnapshot) in
            
            for member in membersSnapshot.children.allObjects as! [DataSnapshot] {
                
                let memberData = member.value as! [String : AnyObject]
                
                let memberID = member.key
                
                let lat = memberData[FireBaseString.latitude.rawValue] as? Double
                let long = memberData[FireBaseString.longitude.rawValue] as? Double
                let coord = (lat != nil && long != nil) ? CLLocationCoordinate2D(latitude: lat!, longitude: long!) : nil
                let dist = memberData[FireBaseString.distance.rawValue] as? Double
                let pace = memberData[FireBaseString.avgPace.rawValue] as? Double
                
                if let runner = self.runnersDictionary[memberID] { //If already active runner, update their object with new data
                    
                    if (runner.location != nil && coord == nil) || !runner.connected { //If it is the first read since finishing a run
                        
                        runner.connected = true
                        
                        let userGroupDB = ref.child(FireBaseString.users.rawValue).child(memberID).child(FireBaseString.groups.rawValue).child(groupID)
                        
                        //Get the most recent value of runs in group for the runner
                        userGroupDB.observeSingleEvent(of: .value) { (userGroupSnapshot) in
                            
                            let groupRuns = userGroupSnapshot.value as! Int
                            runner.numRunsInGroup = groupRuns //Note, this might happen after the delegateVC is updated about the runner. Then it would show this after the next database read
                        }
                    }
                    
                    runner.location = coord
                    
                    //Will not overwrite a value with nil unless they have started running
                    if (runner.location != nil) {
                        
                        runner.distance = dist
                        runner.averagePace = pace
                    }
                    
                    self.delegateVC?.updateRunners([runner])
                    
                } else { //If not already an active runner, add them to the active list
                    
                    let userDB = ref.child(FireBaseString.users.rawValue).child(memberID)
                    
                    //Get the name of each active user and add the information to local runners dictionary
                    userDB.observeSingleEvent(of: .value) { (userSnapshot) in
                        
                        let userSnapshotValue = userSnapshot.value as! [String : AnyObject]
                        
                        let userName = userSnapshotValue[FireBaseString.name.rawValue] as! String
                        
                        let groupRuns = userSnapshotValue[FireBaseString.groups.rawValue]![groupID] as! Int
                        
                        let newRunner = Runner() //Initializes connected to true
                        newRunner.runnerID = memberID
                        newRunner.name = userName
                        newRunner.numRunsInGroup = groupRuns
                        newRunner.location = coord
                        newRunner.distance = dist
                        newRunner.averagePace = pace
                        
                        self.runnersDictionary[memberID] = newRunner
                        
                        self.delegateVC?.updateRunners([newRunner])
                    }
                }
            }
        }
    }
    
    
    func monitorForRemovedRunners() {
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID)
        
        membersDB.observe(.childRemoved) { (memberRemovedSnapshot) in
            
            let memberID = memberRemovedSnapshot.key
            
            if memberID == self.userID() {
                
                self.delegateVC?.notifyDatabaseDisconnect()
                
            } else {
                
                if let removedRunner = self.runnersDictionary[memberID] {
                    
                    removedRunner.location = nil
                    removedRunner.connected = false
                    
                    self.delegateVC?.updateRunners([removedRunner])
                }
            }
        }
    }
    
    
    func uploadUserRunData(location: CLLocationCoordinate2D, distance: Double, pace: Double?) {
        
        guard let userID = userID() else {return}
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID).child(userID)
        
        let uploadPace: Any = pace ?? NSNull()
        
        let runDataDictionary: Dictionary<String, Any> = [FireBaseString.active.rawValue : true,
                                                           FireBaseString.latitude.rawValue : location.latitude,
                                                           FireBaseString.longitude.rawValue : location.longitude,
                                                           FireBaseString.distance.rawValue : distance,
                                                           FireBaseString.avgPace.rawValue : uploadPace]
        
        membersDB.onDisconnectRemoveValue()
        membersDB.updateChildValues(runDataDictionary)
    }
    
    
    func removeRunningData() {
        
        guard let userID = userID() else {return}
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID).child(userID)
        
        let removalDictionary: Dictionary<String, Any> = [FireBaseString.latitude.rawValue : NSNull(),
                                                          FireBaseString.longitude.rawValue : NSNull(),
                                                          FireBaseString.distance.rawValue : NSNull(),
                                                          FireBaseString.avgPace.rawValue : NSNull()]
        
        membersDB.updateChildValues(removalDictionary)
    }
    
    
    func incrementRunsInGroup() {
        
        guard let userID = userID() else {return}
        
        guard let user = runnersDictionary[userID] else {return}
        
        user.numRunsInGroup += 1
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let userGroupDB = ref.child(FireBaseString.users.rawValue).child(userID).child(FireBaseString.groups.rawValue).child(groupID)
        
        //Use the reference to the group to add the new number of runs
        userGroupDB.setValue(user.numRunsInGroup)
    }
    
    
    func removeRunnerFromGroup() {
        
        guard let userID = userID() else {return}
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID)

        membersDB.removeAllObservers()
        
        membersDB.child(userID).removeValue() //This cleans up the database so reads only include active runners
    }
    
    
    //MARK: - Handle Firebase Timeouts From App Becoming Active
    
    func addUserBackToGroup() { //Called from delegateVC if the user wants to stay in the group after being disconnected
        
        guard let userID = userID() else {return}
        
        let groupID = runGroup.fireBaseID
        
        let ref = Database.database().reference()
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID).child(userID).child(FireBaseString.active.rawValue)
        
        membersDB.onDisconnectRemoveValue()
        membersDB.setValue(true) //Change the value in the members dictionary to true to indicate they are active in the group
       
        loadActiveRunners()
    }
    
    
}
