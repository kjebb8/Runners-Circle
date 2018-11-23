//
//  Runner.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-05-12.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation
import CoreLocation

class Runner {
    
    var runnerID: String = ""
    var name: String = ""
    var numRunsInGroup: Int = 0
    var connected: Bool = true
    
    //Optionals are given values when the person is tracking their run
    var location: CLLocationCoordinate2D?
    var distance: Double?
    var averagePace: Double?
    
    func copyRunnerData(for templateRunner: Runner) {
        
        self.runnerID = templateRunner.runnerID
        self.name = templateRunner.name
        self.numRunsInGroup = templateRunner.numRunsInGroup
        self.connected = templateRunner.connected
        self.location = templateRunner.location
        self.distance = templateRunner.distance
        self.averagePace = templateRunner.averagePace
    }
}
