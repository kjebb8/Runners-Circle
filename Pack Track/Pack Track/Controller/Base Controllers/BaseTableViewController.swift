//
//  BaseTableViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-07-10.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import MessageUI

class BaseTableViewController: UITableViewController, MFMailComposeViewControllerDelegate {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        hideKeyboardWhenTappedAround()
    }
    
    
    func showAlert(title: String, message: String) {
        
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        let okAction = UIAlertAction(title: "Ok", style: .cancel, handler: nil)
        alert.addAction(okAction)
        
        present(alert, animated: true, completion: nil)
    }
    
    
    func configureTableView() {
        
        tableView.backgroundView = UIImageView(image: UIImage(named: "Green Background"))
        tableView.separatorStyle = .none
        
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 120
    }
    
    
    //MARK: - Feedback Email Methods
    
    @IBAction func feedbackBarButtonPressed(_ sender: Any) {
        showFeedbackEmail()
    }
    
    
    func showFeedbackEmail() {
        
        if MFMailComposeViewController.canSendMail() {
            
            let mail = MFMailComposeViewController()
            mail.mailComposeDelegate = self
            mail.setToRecipients(["runnerscircleapp@gmail.com"])
            mail.setSubject("App Feedback")
            mail.setMessageBody("", isHTML: false)
            present(mail, animated: true, completion: nil)
            
        } else {
            
            showAlert(title: "Could Not Send Email", message: "Device could not send e-mail")
            print("Failed Email")
        }
    }
    
    
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true, completion: nil)
    }
    
    
}
