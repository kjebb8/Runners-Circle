//
//  RunTabBarController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-05-09.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit

class RunTabBarController: BaseTabBarController {
    
    var userRunManager = UserRunManager()
    
    var timer = Timer()
    var timeCheck: Int = 0
        
    @IBOutlet weak var startStopButton: UIBarButtonItem!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        guard let groupNameTitle = runGroupManager?.runGroupName() else {
            
            self.navigationController?.popViewController(animated: true)
            return
        }
        
        if groupNameTitle.count > 15 {
            
            let attributes = [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 25, weight: .semibold)]
            navigationController?.navigationBar.largeTitleTextAttributes = attributes
        }
        
        navigationItem.title = groupNameTitle
        
        timer = Timer.scheduledTimer(timeInterval: 1.0,
                                     target: self,
                                     selector: #selector(self.timerIntervalTick),
                                     userInfo: nil,
                                     repeats: true)
        
        runGroupManager?.loadActiveRunners()
        runGroupManager?.monitorForRemovedRunners()
    }
    
    
    func exitGroup() {
        
        let attributes = [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 30, weight: .semibold)]
        navigationController?.navigationBar.largeTitleTextAttributes = attributes
        
        timer.invalidate()
        userRunManager.stopLocationUpdates()
        if userRunManager.runStarted {userRunManager.endRun()}
        runGroupManager?.removeRunnerFromGroup()
        runGroupManager = nil
        
        self.navigationController?.popViewController(animated: true)
    }
    
    
    @objc func timerIntervalTick() {
        
        timeCheck += 1
        
        if timeCheck >= (runGroupManager?.databaseReadPeriod)! { //>= so that it enters if the period is changed from 60 to 4 when entering foreground
            
            runGroupManager?.loadActiveRunners()
            timeCheck = 0
        }
        
        if userRunManager.runStarted {userRunManager.incrementRunTime()}
    }
    
    
    @IBAction func startStopButtonPressed(_ sender: UIBarButtonItem) {
        
        if !userRunManager.runStarted {
            
            userRunManager.startRun()
            startStopButton.title = "Stop"
            
        } else {
            
            userRunManager.endRun()
            startStopButton.title = "Start"
        }
    }
    
    @IBAction func exitButtonPressed(_ sender: UIBarButtonItem) {
        
        if !userRunManager.runStarted {
            
            exitGroup()
            
        } else {
            
            let alert = UIAlertController(title: "Exit Circle?", message: "Are you sure you want to exit while running?", preferredStyle: .alert)
            
            let cancelAction = UIAlertAction(title: "Stay in Circle", style: .cancel, handler: nil)
            
            let exitAction = UIAlertAction(title: "Exit Circle", style: .default) { (exitAction) in
                self.exitGroup()
            }
            
            alert.addAction(cancelAction)
            alert.addAction(exitAction)
            
            alert.preferredAction = alert.actions[0]
            
            present(alert, animated: true, completion: nil)
        }
    }
    
    
}
