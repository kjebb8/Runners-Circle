//
//  NewGroupViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-07-04.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase

class NewGroupViewController: BaseViewController {

    @IBOutlet weak var groupName: UITextField!
    @IBOutlet weak var memberCode: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        groupName.becomeFirstResponder()
    }

    
    @IBAction func registerGroupPressed(_ sender: UIButton) {
        
        if groupName.text != "" && memberCode.text != "" {
            
            let connectedRef = Database.database().reference(withPath: ".info/connected")
            
            connectedRef.observeSingleEvent(of: .value) { snapshot in
                
                guard let connected = snapshot.value as? Bool else {return}
                
                if connected {self.addGroup()}
            }
            
        } else {
            showAlert(title: "Invalid Circle", message: "Make sure all fields are complete")
        }
    }
    
    
    func addGroup() {
        
        guard let groupName = groupName.text else {return}
        
        guard let memberCode = memberCode.text else {return}
        
        let ref = Database.database().reference()
        
        guard let userID = Auth.auth().currentUser?.uid else {return}
        
        let runGroupsDB = ref.child(FireBaseString.runGroups.rawValue)
        
        let groupInfoDictionary = [FireBaseString.name.rawValue : groupName,
                                   FireBaseString.owner.rawValue : userID,
                                   FireBaseString.memberCode.rawValue : memberCode]
        
        //Add the new group to the list of groups with an auto ID. Creator is automatically added as the owner
        runGroupsDB.childByAutoId().setValue(groupInfoDictionary) { (error, reference) in
            
            if error != nil {
                
                print(error!)
                
            } else {
                
                let groupReference = reference
                
                let userGroupDB = ref.child(FireBaseString.users.rawValue).child(userID).child(FireBaseString.groups.rawValue).child(groupReference.key!)
                
                //Use the reference to the new group to add the group to the user's branch under child "groups" and set runs to 0
                userGroupDB.setValue(0)
            }
        }
        
        self.navigationController?.popViewController(animated: true) //Prevents user from mashing register circle button when .info/connected returns true when not connected
    }
    
    
}
