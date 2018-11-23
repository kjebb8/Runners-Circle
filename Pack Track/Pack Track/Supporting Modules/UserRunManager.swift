//
//  UserRunManager.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-06-10.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import Foundation
import CoreLocation


protocol MapDelegate {
    func updateUserOnMap(newLocations: [CLLocation])
    func clearMap()
}


protocol StatDelegate {
    func updateStats(newDistance: Measurement<UnitLength>?, time: Int)
}


class UserRunManager: NSObject {
    
    private let locationManager = LocationManager.shared //Single instance of location manager
    
    private var mapDelegateVC: MapDelegate?
    private var statDelegateVC: StatDelegate?
    
    var runStarted: Bool = false
    private var runTime: Int = 0

    private var distance = Measurement(value: 0, unit: UnitLength.meters)
    private var locationList: [CLLocation] = []
    private var currentLocation: CLLocation?
    
    override init() {
        
        super.init()
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.requestWhenInUseAuthorization()
        startLocationUpdates()
    }

    
    //MARK: - Setting Delegate Methods
    
    func setMapDelegate(_ delegate: MapDelegate) {
        
        mapDelegateVC = delegate
        
        if locationList.count >= 1 {
            mapDelegateVC?.updateUserOnMap(newLocations: locationList) //Sends existing locations to the map for the polyline
        }
    }
    
    
    func setStatDelegate(_ delegate: StatDelegate) {
        
        statDelegateVC = delegate
        statDelegateVC?.updateStats(newDistance: distance, time: runTime)
    }
    
    
    //MARK: - Methods for Keeping State
    
    func startRun() {
        
        runStarted = true
        
        runTime = 0
        distance = Measurement(value: 0, unit: UnitLength.meters)
        locationList.removeAll()
        
        mapDelegateVC?.clearMap()
        statDelegateVC?.updateStats(newDistance: distance, time: runTime)
        
        if let startLocation = currentLocation {
            runGroupManager?.uploadUserRunData(location: startLocation.coordinate, distance: distance.value, pace: nil)
            locationList.append(startLocation)
        }
    }
    
    
    func startLocationUpdates() {
        
        locationManager.delegate = self
        locationManager.activityType = .fitness //Helps save power
        locationManager.distanceFilter = 15 //Doesn't affect power usage. 10 is a good balance between zig-zagging readings (too small) and pixelated readings (too large)
        locationManager.startUpdatingLocation()
    }
    
    
    func stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
    }
    
    
    func incrementRunTime() {
        
        runTime += 1
        statDelegateVC?.updateStats(newDistance: nil, time: runTime)
    }
    
    
    func endRun() {
        
        runStarted = false
        
        if distance.value >= 1000 {
            runGroupManager?.incrementRunsInGroup()
        }
        
        runGroupManager?.removeRunningData()
    }

    
}


//Called each time Core Location updates the user's location
extension UserRunManager: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        for newLocation in locations {
            
            let howRecent = newLocation.timestamp.timeIntervalSinceNow
            guard newLocation.horizontalAccuracy < 20 && abs(howRecent) < 10 else { continue } //Ensure good accuracy of data
            
            if runStarted {
            
                if let lastLocation = locationList.last {
                    
                    let delta = newLocation.distance(from: lastLocation) //Accounts for earth curvature
                    distance = distance + Measurement(value: delta, unit: UnitLength.meters)
                    statDelegateVC?.updateStats(newDistance: distance, time: runTime)
                    
                    mapDelegateVC?.updateUserOnMap(newLocations: [lastLocation, newLocation])
                }
                
                locationList.append(newLocation)
                
                let newPace = distance.value != 0 ? Double(runTime) / distance.value * 1000 / 60 : nil
                
                runGroupManager?.uploadUserRunData(location: newLocation.coordinate, distance: distance.value, pace: newPace)
                
            } else {
                
                if locationList.count <= 1 {locationList = [newLocation]} //For the case where you go to the map before the run starts but you're not moving, it will have a location to send the map
                mapDelegateVC?.updateUserOnMap(newLocations: [newLocation])
            }
            
            currentLocation = newLocation
        }
    }
}
