//
//  ViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-08.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase

class WelcomeViewController: BaseViewController {

    override func viewDidLoad() {
        
        let attributes = [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 30, weight: .semibold)]
        navigationController?.navigationBar.largeTitleTextAttributes = attributes
        
        if Auth.auth().currentUser != nil {
            performSegue(withIdentifier: "skipToGroups", sender: self)
        }
        
        super.viewDidLoad()
    }

    
}

