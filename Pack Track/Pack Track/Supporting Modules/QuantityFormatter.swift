//
//  QuantityFormatter.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-10.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation

//Formatting values uniformly in the program
struct FormatDisplay {
    
    static func distance(_ distance: Double) -> String {
        
        let distanceMeasurement = Measurement(value: distance, unit: UnitLength.meters)
        return FormatDisplay.distance(distanceMeasurement)
    }
    
    
    static func distance(_ distance: Measurement<UnitLength>) -> String {
        
        let formatter = MeasurementFormatter()
        formatter.unitOptions = .providedUnit
        
        let numberFormatter = NumberFormatter()
        numberFormatter.maximumFractionDigits = 2
        numberFormatter.minimumFractionDigits = 2
        numberFormatter.minimumIntegerDigits = 1
        
        formatter.numberFormatter = numberFormatter
        
        if distance.unit == UnitLength.meters {
            
            let distanceKM = Measurement(value: distance.value / 1000, unit: UnitLength.kilometers)
            return formatter.string(from: distanceKM)
            
        } else {
            return formatter.string(from: distance)
        }
    }
    
    
    static func time(_ seconds: Int) -> String {
        
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = [.hour, .minute, .second]
        formatter.unitsStyle = .positional
        formatter.zeroFormattingBehavior = .pad
        
        return formatter.string(from: TimeInterval(seconds))!
    }
    
    
    //Can use locale to decide what units to display when the function is called
    static func pace(distance: Measurement<UnitLength>, seconds: Int, outputUnit: UnitSpeed) -> String {
        
        let formatter = MeasurementFormatter()
        formatter.unitOptions = [.providedUnit]
        
        let numberFormatter = NumberFormatter()
        numberFormatter.maximumFractionDigits = 1
        numberFormatter.minimumFractionDigits = 1
        numberFormatter.minimumIntegerDigits = 1
        
        formatter.numberFormatter = numberFormatter
        
        let speedMagnitude = seconds != 0 ? distance.value / Double(seconds) : 0
        let speed = Measurement(value: speedMagnitude, unit: UnitSpeed.metersPerSecond)
        
        return formatter.string(from: speed.converted(to: outputUnit))
    }
    
    
    static func date(_ timestamp: Date?) -> String {
        
        guard let timestamp = timestamp as Date? else { return "" }
        
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        
        return formatter.string(from: timestamp)
    }
    
    
}
