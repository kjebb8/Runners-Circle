//
//  Extensions.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-29.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation


//MARK: - Int Extension

extension Int {
    
    var inMinutes: Double {return Double(self) / 60}
    
    var hours: Int {return self / 3600}
    var minutes: Int {return self / 60 % 60}
    var seconds: Int {return self % 60}
    
    func getFormattedRunTimeString() -> (String) {
        
        if self.hours >= 1 {
            return String(format: "%i:%02i:%02i", self.hours, self.minutes, self.seconds)
        } else {
            return String(format: "%i:%02i", self.minutes, self.seconds)
        }
    }
}


//MARK: - Double Extension

extension Double {
    
    var minutes: Int {return Int(self.rounded(.down))}
    var seconds: Int {return Int(((self - self.rounded(.down)) * 6000).rounded() / 100)}
    
    func getFormattedPaceString() -> (String) {
        return String(format: "%i:%02i", self.minutes, self.seconds) + " min/km"
    }
}

//Pace Helper Function
func paceString(_ distace: Measurement<UnitLength>,_ time: Int) -> String {
    
    let pace = distace.value != 0 ? time.inMinutes / (distace.value / 1000) : 0
    return pace.getFormattedPaceString()
}


//MARK: - String Extension

extension String {

    public func getNameInitials(separator: String = "") -> String {

        let initials = self.components(separatedBy: " ").map({ String($0.first!) }).joined(separator: separator);
        return initials;
    }
}
