//
//  StatsViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit

class StatsTableViewController: BaseTableViewController, RunGroupManagerDelegate, StatDelegate {

    var runnersDictionary = Dictionary<String, Runner>()
    
    var userRunManager: UserRunManager!
    
    var runTime: Int = 0
    var runDistance = Measurement(value: 0, unit: UnitLength.meters)
    var timeForPace: Int = 0
    
    var totalDistance: Double = 0
    var numberOfRunningMembers: Int = 0
    var numberOfConnectedMembers: Int = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let runTabBarController = self.tabBarController as? RunTabBarController
        userRunManager = runTabBarController?.userRunManager
        userRunManager.setStatDelegate(self)
        
        configureTableView()
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        runGroupManager?.setDelegateAs(self)
    }
    
    
    override func configureTableView() {
        
        super.configureTableView()
        
        tableView.rowHeight = 275
        
        tableView.register(UINib(nibName: "StatCell", bundle: nil), forCellReuseIdentifier: "statCell")
    }
    
    
    //MARK: - Run Group Updates
    
    func updateRunners(_ runners: [Runner]) {
        updateGroupStats(for: runners)
    }
    
    
    func notifyDatabaseDisconnect() {
        
        let  alert = UIAlertController(title: "Disconnected from Server", message: "", preferredStyle: .alert)
        
        let stayAction = UIAlertAction(title: "Stay in Circle", style: .cancel) { stayAction in
            runGroupManager?.addUserBackToGroup()
        }
        
        let exitAction = UIAlertAction(title: "Exit Circle", style: .default) { (exitAction) in
            let runTabBarController = self.tabBarController as? RunTabBarController
            runTabBarController?.exitGroup()
        }
        
        alert.addAction(stayAction)
        alert.addAction(exitAction)
        
        alert.preferredAction = alert.actions[0]
        
        present(alert, animated: true, completion: nil)
    }
    
    
    //MARK: - TableView Data Source Methods
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 2
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "statCell", for: indexPath) as! StatCell
        
        cell.backgroundColor = UIColor.clear
        
        if indexPath.row == 0 {
            
            cell.headingLabel.text = "Personal Stats"
            
            cell.stat1LabelName.text = "Time: "
            cell.stat2LabelName.text = "Distance: "
            cell.stat3LabelName.text = "Pace: "
            
            cell.stat1Label.text = FormatDisplay.time(runTime)
            cell.stat2Label.text = FormatDisplay.distance(runDistance)
            cell.stat3Label.text = paceString(runDistance, timeForPace)
            
        } else if indexPath.row == 1 {
            
            cell.headingLabel.text = "Collective Stats"
            cell.stat1LabelName.text = "Total Distance: "
            cell.stat2LabelName.text = "Members Running: "
            cell.stat3LabelName.text = "Members Connected: "
            
            cell.stat1Label.text = FormatDisplay.distance(totalDistance)
            cell.stat2Label.text = "\(numberOfRunningMembers)"
            cell.stat3Label.text = "\(numberOfConnectedMembers)"
        }
        
        cell.headingBackground.layer.cornerRadius = 5
        cell.headingBackground.layer.masksToBounds = true
        
        cell.statsBackground.layer.cornerRadius = 5
        cell.statsBackground.layer.masksToBounds = true
        
        cell.isUserInteractionEnabled = false
        
        return cell
    }
    
    
    //MARK: - Stat Calculation Methods
    
    func updateStats(newDistance: Measurement<UnitLength>?, time: Int) {
        
        runTime = time
        
        if let distance = newDistance {
            
            runDistance = distance
            timeForPace = time
        }
        
        tableView.reloadData()
    }
    
    
    func updateGroupStats(for runnerArray: [Runner]) {
        
        for runner in runnerArray {
    
            if let existingRunner = runnersDictionary[runner.runnerID] {
                
                if existingRunner.connected {
                    
                    numberOfConnectedMembers -= 1
                    
                    if existingRunner.location != nil {
                        numberOfRunningMembers -= 1
                    }
                }
                
                if let oldDistanceRun = existingRunner.distance {
                    totalDistance -= oldDistanceRun
                }
                
            } else {
                
                runnersDictionary[runner.runnerID] = Runner() //Make a new runner object for new runners. If the dictionary uses the passed runner directly, it will change when the RunGroupManager runner object changes (a pointer to the Runner). This makes it impossible to detect changes/updates for a runner in this class, which is required to calculate stats
            }
            
            if runner.connected {
                
                numberOfConnectedMembers += 1
                
                if runner.location != nil {
                    numberOfRunningMembers += 1
                }
            }
            
            if let newDistanceRun = runner.distance {
                totalDistance += newDistanceRun
            }
            
            runnersDictionary[runner.runnerID]?.copyRunnerData(for: runner) //The runner object will exist in the dictionary at this point
        }
        
        tableView.reloadData()
    }
    

}
