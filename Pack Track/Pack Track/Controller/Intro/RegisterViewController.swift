//
//  RegisterViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase
import SVProgressHUD

class RegisterViewController: BaseViewController {

    @IBOutlet weak var nameTextField: UITextField!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        nameTextField.becomeFirstResponder()
    }

    
    @IBAction func registerButtonPressed(_ sender: UIButton) {
        
        let name = nameTextField.text!
        let email = emailTextField.text!
        let password = passwordTextField.text!

        if name != "" {
            
            SVProgressHUD.show()
            
            //Set up a new user on our Firbase database
            Auth.auth().createUser(withEmail: email, password: password) { (user, error) in
                
                if error != nil {
                    
                    print(error!)
                    SVProgressHUD.dismiss()
                    self.showAlert(title: "Registration Failed", message: "\(error!.localizedDescription)")
                    
                } else {
                    
                    //Success, add user info to database
                    guard let userID = user?.user.uid else {return}
                    
                    let ref = Database.database().reference()
                    let userDB = ref.child(FireBaseString.users.rawValue).child(userID)
                    
                    let userDictionary = [FireBaseString.name.rawValue : name,
                                          FireBaseString.email.rawValue : email]
                    
                    userDB.setValue(userDictionary) { (error, reference) in
                        
                        if error != nil {
                            print(error!)
                        } else {

                            SVProgressHUD.dismiss()
                            self.performSegue(withIdentifier: "goToGroups", sender: self)
                        }
                    }
                }
            }
        } else {
            showAlert(title: "Registration Filed", message: "Please add your name")
        }
    }


}
