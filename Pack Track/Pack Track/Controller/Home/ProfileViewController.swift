//
//  ProfileViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-07-24.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase

class ProfileViewController: BaseViewController {

    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var totalCirclesLabel: UILabel!
    @IBOutlet weak var totalRunsLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        loadUserData()
    }
    
    
    func loadUserData() {
        
        guard let userID = Auth.auth().currentUser?.uid else {return}
        
        let ref = Database.database().reference()
        
        let userDB = ref.child(FireBaseString.users.rawValue).child(userID)
        
        //Get the name of each active user and add the information to local runners dictionary
        userDB.observeSingleEvent(of: .value) { (userSnapshot) in
            
            let userSnapshotValue = userSnapshot.value as? [String : AnyObject]
            
            let userName = userSnapshotValue?[FireBaseString.name.rawValue] as? String
            
            let userEmail = userSnapshotValue?[FireBaseString.email.rawValue] as? String
            
            let groups = userSnapshotValue?[FireBaseString.groups.rawValue] as? Dictionary<String, Int>

            let totalCircles: Int = groups?.count ?? 0
            let totalRuns: Int = groups?.values.reduce(0, +) ?? 0
            
            self.nameLabel.text = "Name: " + (userName ?? "")
            self.emailLabel.text = "Email: " + (userEmail ?? "")
            self.totalCirclesLabel.text = "Total Circles: \(totalCircles)"
            self.totalRunsLabel.text = "Total Runs: \(totalRuns)"
        }
    }
    
    
    @IBAction func privacyPolicyPressed(_ sender: UIButton) {

        if let privacyURL = NSURL(string: "https://kjebb8.github.io/Runners-Circle/") {
            UIApplication.shared.open(privacyURL as URL, options: [:])
        }
    }
    
    
    @IBAction func logoutPressed(_ sender: UIButton) {
        
        do {
            let ref = Database.database().reference()
            
            guard let userID = Auth.auth().currentUser?.uid else {return}
            
            let userGroupsDB = ref.child(FireBaseString.users.rawValue).child(userID).child(FireBaseString.groups.rawValue)
            
            userGroupsDB.removeAllObservers()
            
            try Auth.auth().signOut()
            navigationController?.popToRootViewController(animated: true) //Root VC is the first one on the navigation stack
        }
        catch {
            print("Error signing out")
        }
    }


}
