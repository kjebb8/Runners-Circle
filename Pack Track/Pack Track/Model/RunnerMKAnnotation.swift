//
//  RunnerMKAnnotation.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-20.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation
import MapKit
import Contacts

class RunnerMKAnnotation: NSObject, MKAnnotation {
    
    let runnerName: String
    var owner: Bool = false //Use for colouring
    dynamic var coordinate: CLLocationCoordinate2D
    var distance: Double
    var avgPace: Double?

    let title: String? = nil
    let subtitle: String? = nil
    
    var markerTintColor: UIColor  {
        
        if owner == true {return .black}
        else {return .red}
    }
    
    init(name: String, coordinate: CLLocationCoordinate2D, distance: Double, pace: Double?) {
        
        self.runnerName = name
        self.coordinate = coordinate
        self.distance = distance
        self.avgPace = pace
        
        super.init()
    }

    
}
