//
//  UnitExtensions.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-10.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation

//Convert speeds to pace using the UnitConverter class
class UnitConverterPace: UnitConverter {
    
    private let coefficient: Double
    
    init(coefficient: Double) {
        self.coefficient = coefficient
    }
    
    
    override func baseUnitValue(fromValue value: Double) -> Double {
        return reciprocal(value * coefficient)
    }
    
    
    override func value(fromBaseUnitValue baseUnitValue: Double) -> Double {
        return reciprocal(baseUnitValue * coefficient)
    }
    
    
    private func reciprocal(_ value: Double) -> Double {
        
        guard value != 0 else { return 0 }
        return 1.0 / value
    }
    
    
}


//Extend UnitSpeed class to include relevent pace values
extension UnitSpeed {
    
    class var secondsPerMeter: UnitSpeed {
        return UnitSpeed(symbol: "sec/m", converter: UnitConverterPace(coefficient: 1))
    }
    
    
    class var minutesPerKilometer: UnitSpeed {
        return UnitSpeed(symbol: "min/km", converter: UnitConverterPace(coefficient: 60.0 / 1000.0)) //CHANGED THE LABEL FOR STATS VC
    }
    
    
    class var minutesPerMile: UnitSpeed {
        return UnitSpeed(symbol: "min/mi", converter: UnitConverterPace(coefficient: 60.0 / 1609.34))
    }
    
    
}
