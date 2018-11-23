//
//  RunnerMarkerView.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-27.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation
import MapKit

class RunnerMarkerView: MKMarkerAnnotationView {
    
    let detailLabel = UILabel()
    
    func setDetailCalloutLabel() {
        
        guard let runner = annotation as? RunnerMKAnnotation else { return }
        
        let distanceString: String = FormatDisplay.distance(runner.distance)
        
        var paceString: String = ""
        
        if let pace = runner.avgPace {
            paceString = pace.getFormattedPaceString()
        }
        
        detailLabel.text = runner.runnerName + "\n" + distanceString + "\n" + paceString
    }
    
    
    override var annotation: MKAnnotation? {
        
        willSet {
            
            guard let runner = newValue as? RunnerMKAnnotation else { return }
            
            clusteringIdentifier = "runnerCluster"
            
            titleVisibility = .hidden
            subtitleVisibility = .hidden
            
            markerTintColor = runner.markerTintColor
            
            canShowCallout = true
            calloutOffset = CGPoint(x: 0, y: 5)
            
            let calloutButton = UIButton(type: .detailDisclosure)
            calloutButton.isEnabled = false
            rightCalloutAccessoryView = calloutButton
            
            glyphText = String(runner.runnerName).getNameInitials()
            
            detailLabel.numberOfLines = 0
            detailLabel.font = detailLabel.font.withSize(20)
            detailCalloutAccessoryView = detailLabel
        }
        
        didSet {
             setDetailCalloutLabel()
        }
        
    }
}


class RunnerClusterAnnotationView: MKMarkerAnnotationView {
    
    override var annotation: MKAnnotation? {
        
        didSet {
            
            guard let cluster = annotation as? MKClusterAnnotation else {return}
            
            let numberOfRunners = cluster.memberAnnotations.count
            
            titleVisibility = .hidden
            subtitleVisibility = .hidden
            markerTintColor = UIColor.red
            
            canShowCallout = true
            calloutOffset = CGPoint(x: 0, y: 5)
            
            let calloutButton = UIButton(type: .detailDisclosure)
            calloutButton.isEnabled = false
            rightCalloutAccessoryView = calloutButton
            
            var clusterNames: String = "Runners:\n"
            
            for memberInCluster in cluster.memberAnnotations {
                guard let runnerInCluster = memberInCluster as? RunnerMKAnnotation else {continue}
                clusterNames += runnerInCluster.runnerName + "\n"
            }
            
            glyphText = String(numberOfRunners)
            
            let detailLabel = UILabel()
            detailLabel.numberOfLines = 0
            detailLabel.font = detailLabel.font.withSize(20)
            detailLabel.text = clusterNames
            detailCalloutAccessoryView = detailLabel
        }
    }
}

