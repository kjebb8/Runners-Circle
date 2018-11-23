//
//  RunGroupCell.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-07-12.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit

class RunGroupCell: UITableViewCell {
    
    @IBOutlet weak var mainBackground: UIView!
    @IBOutlet weak var groupNameLabel: UILabel!
    @IBOutlet weak var runsWithGroupLabel: UILabel!
    @IBOutlet weak var groupOwnerLabel: UILabel!
    
    var groupID: String = ""
}
