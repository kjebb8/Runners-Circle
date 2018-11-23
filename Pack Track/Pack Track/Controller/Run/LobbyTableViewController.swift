//
//  LobbyViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase

class LobbyTableViewController: BaseTableViewController, RunGroupManagerDelegate {

    var runnersDictionary = Dictionary<String, Runner>()
    var runnersDisplayArray = [Runner]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        configureTableView()
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        runGroupManager?.setDelegateAs(self)
    }
    
    
    override func configureTableView() {
        
        super.configureTableView()
        
        tableView.register(UINib(nibName: "RunnerCell", bundle: nil), forCellReuseIdentifier: "runnerCell")
    }
    
    
    //MARK: - Run Group Updates

    func updateRunners(_ runners: [Runner]) {
        
        organizeRunnerDisplayArray(for: runners)
        tableView.reloadData()
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
    
    
    //MARK: - Helper Methods
    
    func organizeRunnerDisplayArray(for runnerArray: [Runner]) {
        
        for runner in runnerArray {
            runnersDictionary[runner.runnerID] = runner
        }
        
        let runnersBaseArray = Array(runnersDictionary.values)
        let filteredArray = runnersBaseArray.filter({$0.connected})
        runnersDisplayArray = filteredArray.sorted(by: {$0.name < $1.name})
    }

    
    //MARK: - TableView Data Source Methods
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return runnersDisplayArray.count + 1
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "runnerCell", for: indexPath) as! RunnerCell
        
        cell.backgroundColor = UIColor.clear
        
        if indexPath.row == 0 {
            
            cell.nameLabel.textColor = UIColor.black
            cell.numRunsLabel.textColor = UIColor.black
            cell.statusLabel.textColor = UIColor.black
            
            cell.nameLabel.text = "Name"
            cell.numRunsLabel.text =  "#Runs"
            cell.statusLabel.text = "Status"
            
            cell.mainBackground.backgroundColor = UIColor.clear
            
            cell.isUserInteractionEnabled = false
            
        } else {
        
            let runner = runnersDisplayArray[indexPath.row - 1]
            cell.nameLabel.text = runner.name
            cell.numRunsLabel.text =  "\(runner.numRunsInGroup)"
            
            let status: String
            
            if runner.location == nil {
                
                cell.mainBackground.backgroundColor = UIColor(white: 0.32, alpha: 0.8)
                status = "Connected"
                
            } else {
                
                cell.mainBackground.backgroundColor = UIColor(white: 0.13, alpha: 0.8)
                status = "Running"
            }
            
            cell.statusLabel.text = status
            
            cell.mainBackground.layer.cornerRadius = 5
            cell.mainBackground.layer.masksToBounds = true
            
            cell.isUserInteractionEnabled = false
        }
        
        return cell
    }


}
