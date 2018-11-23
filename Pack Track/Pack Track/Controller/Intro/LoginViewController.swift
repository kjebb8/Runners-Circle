//
//  LoginViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase
import SVProgressHUD

class LoginViewController: BaseViewController {

    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        emailTextField.becomeFirstResponder()
    }
 
    
    @IBAction func loginButtonPressed(_ sender: UIButton) {
        
        SVProgressHUD.show()
        
        //Log in the user
        Auth.auth().signIn(withEmail: emailTextField.text!, password: passwordTextField.text!) { (user, error) in
            
            if error != nil {
                
                print(error!)
                SVProgressHUD.dismiss()
                self.showAlert(title: "Log In Failed", message: "\(error!.localizedDescription)")
                
            } else {
                
                SVProgressHUD.dismiss()
                self.performSegue(withIdentifier: "goToGroups", sender: self)
            }
        }
    }
    

}
