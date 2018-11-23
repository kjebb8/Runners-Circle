//
//  GroupsViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase

class GroupsTableViewController: BaseTableViewController {
    
    var userRunGroups = Dictionary<String, RunGroup>()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        configureTableView()
        
        monitorForSubscribedGroups() //Also loads the existing group names
        
        monitorForGroupUpdates()
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        tableView.reloadData()
    }
    
    
    override func configureTableView() {
        
        super.configureTableView()
        
        tableView.register(UINib(nibName: "AddGroupCell", bundle: nil), forCellReuseIdentifier: "addGroupCell")
        tableView.register(UINib(nibName: "SearchGroupCell", bundle: nil), forCellReuseIdentifier: "searchGroupCell")
        tableView.register(UINib(nibName: "RunGroupCell", bundle: nil), forCellReuseIdentifier: "runGroupCell")
    }
    
    
    //MARK: - TableView Data Source Methods
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return userRunGroups.count + 2
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        if indexPath.row == 0 {
            
            let cell = tableView.dequeueReusableCell(withIdentifier: "addGroupCell", for: indexPath) as! AddGroupCell
            
            return cell
            
        } else if indexPath.row == 1 {
            
            let cell = tableView.dequeueReusableCell(withIdentifier: "searchGroupCell", for: indexPath) as! SearchGroupCell
            
            return cell
            
        } else {
        
            let cell = tableView.dequeueReusableCell(withIdentifier: "runGroupCell", for: indexPath) as! RunGroupCell
        
            let group = Array(userRunGroups.values.sorted(by: {$0.name < $1.name}))[indexPath.row - 2]
            
            cell.groupNameLabel.text = group.name
            cell.runsWithGroupLabel.text = "Runs With Circle: \(group.runsWithGroup)"
            cell.groupOwnerLabel.text = "Owned By: " + group.ownerName
            cell.groupID = group.fireBaseID
            
            cell.mainBackground.layer.cornerRadius = 5
            cell.mainBackground.layer.masksToBounds = true
            
            cell.backgroundColor = UIColor.clear
            
            return cell
        }
    }
    
    
    //MARK: - TableView Delegate Methods
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if indexPath.row == 0 {
            performSegue(withIdentifier: "goToAddGroup", sender: self)
        } else if indexPath.row == 1 {
            performSegue(withIdentifier: "goToSearchGroup", sender: self)
        } else {
            let cell = tableView.cellForRow(at: indexPath) as! RunGroupCell
            let groupID = cell.groupID
            enterGroup(withID: groupID)
        }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        if segue.identifier == "goToRun" {
            
            if let indexPath = tableView.indexPathForSelectedRow {
                
                let selectedCell = tableView.cellForRow(at: indexPath) as! RunGroupCell
                let selectedGroupID = selectedCell.groupID
                guard let selectedGroup = userRunGroups[selectedGroupID] else {return}
                runGroupManager = RunGroupManager(selectedGroup)
            }
            
        } else if segue.identifier == "goToSearchGroup" {
            
            let destinationVC = segue.destination as! SearchTableViewController
            
            var groupIds = [String]()
            
            for group in userRunGroups {
                groupIds.append(group.key)
            }
            destinationVC.userRunGroupIds = groupIds
        }
    }
    
    
    //MARK: - Adding and Retrieving from Firebase
    
    //Fetches required = (Number of groups subscribed to) x 3
    //Loops required = Number of groups subscribed to
    func monitorForSubscribedGroups() {
        
        let ref = Database.database().reference()
        
        guard let userID = Auth.auth().currentUser?.uid else {return}
        
        let userGroupsDB = ref.child(FireBaseString.users.rawValue).child(userID).child(FireBaseString.groups.rawValue)
        
        //Get the key for each group the user subscribes to
        userGroupsDB.observe(.childAdded) { (userGroupSnapshot) in

            let userGroupKey = userGroupSnapshot.key
            
            let runGroupDB = ref.child(FireBaseString.runGroups.rawValue).child(userGroupKey)
            
            //Go into each subscribed group and pull out the group info to be displayed
            runGroupDB.observeSingleEvent(of: .value) { (groupSnapshot) in
               
                let groupSnapshotValue = groupSnapshot.value as! [String : AnyObject]
                let groupName = groupSnapshotValue[FireBaseString.name.rawValue] as! String
                let groupOwner = groupSnapshotValue[FireBaseString.owner.rawValue] as! String
                let groupID = groupSnapshot.key
                
                let newRunGroup = RunGroup()
                newRunGroup.name = groupName
                newRunGroup.ownerID = groupOwner
                newRunGroup.fireBaseID = groupID
                newRunGroup.runsWithGroup = userGroupSnapshot.value as! Int
                
                let ownerNameDB = ref.child(FireBaseString.users.rawValue).child(groupOwner).child(FireBaseString.name.rawValue)
                
                //Get the name of the owner and add it to the RunGroup object
                ownerNameDB.observeSingleEvent(of: .value) { (ownerNameSnapshot) in
                    
                    newRunGroup.ownerName = ownerNameSnapshot.value as! String
                
                    self.userRunGroups[groupID] = newRunGroup
                    
                    self.tableView.reloadData()
                }
            }
        }
    }
    
    
    func monitorForGroupUpdates() {
        
        let ref = Database.database().reference()
        
        guard let userID = Auth.auth().currentUser?.uid else {return}
        
        let userGroupsDB = ref.child(FireBaseString.users.rawValue).child(userID).child(FireBaseString.groups.rawValue)
        
        //Get the key for each group the user subscribes to in order to read their runs in the group
        userGroupsDB.observe(.childChanged) { (userGroupSnapshot) in
            
            let groupID = userGroupSnapshot.key
            let groupRuns = userGroupSnapshot.value as! Int
            
            self.userRunGroups[groupID]?.runsWithGroup = groupRuns
            
            self.tableView.reloadData()
        }
    }
    
    
    func enterGroup (withID groupID: String) {
        
        let ref = Database.database().reference()
        
        guard let userID = Auth.auth().currentUser?.uid else {return}
        
        let membersDB = ref.child(FireBaseString.members.rawValue).child(groupID).child(userID).child(FireBaseString.active.rawValue)
        
        //Change the value in the members dictionary to true to indicate they are active in the group
        membersDB.onDisconnectRemoveValue()
        membersDB.setValue(true) { (error, ref) in
            
            if error == nil {
                self.performSegue(withIdentifier: "goToRun", sender: self)
            }
        }
    }
    
    

}
