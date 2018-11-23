//
//  TipsViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-07-25.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit

class TipsViewController: UIViewController {

    @IBOutlet weak var textView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }


    @IBAction func donePressed(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
    }
    
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        textView.setContentOffset(CGPoint.zero, animated: false)
    }
    
    
}
