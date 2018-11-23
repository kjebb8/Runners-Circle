//
//  MapViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-04-15.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import MapKit

class MapViewController: UIViewController, RunGroupManagerDelegate, MapDelegate {
    
    var runnerAnnotationsDictionary = Dictionary<String, RunnerMKAnnotation>()
    
    var userRunManager: UserRunManager!
    
    var initialMapRegionSet: Bool = false
    
    var centreCoordinate: CLLocationCoordinate2D?
    
    var ownerID: String?
    var userID: String?
    
    @IBOutlet weak var mapView: MKMapView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        ownerID = runGroupManager?.ownerID()
        userID = runGroupManager?.userID()

        mapView.delegate = self
        mapView.userLocation.title = ""
        mapView.showsUserLocation = true
        mapView.isZoomEnabled = true
        mapView.isScrollEnabled = true
        mapView.register(RunnerMarkerView.self, forAnnotationViewWithReuseIdentifier: MKMapViewDefaultAnnotationViewReuseIdentifier)
        mapView.register(RunnerClusterAnnotationView.self, forAnnotationViewWithReuseIdentifier: MKMapViewDefaultClusterAnnotationViewReuseIdentifier)
        
        let runTabBarController = self.tabBarController as? RunTabBarController
        userRunManager = runTabBarController?.userRunManager
        userRunManager.setMapDelegate(self)
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        runGroupManager?.setDelegateAs(self)
    }
    
    
    //MARK: - Run Group Updates
    
     func updateRunners(_ runners: [Runner]) {
        updateMapAnnotation(for: runners)
    }
    
    
    func notifyDatabaseDisconnect() {
        
        let  alert = UIAlertController(title: "Disconnected from Server", message: "", preferredStyle: .alert)
        
        let stayAction = UIAlertAction(title: "Stay in Circle", style: .cancel) { stayAction in
            runGroupManager?.addUserBackToGroup()
        }
        
        let exitAction = UIAlertAction(title: "Exit Circle", style: .default) { (exitAction) in
            let runTabBarController = self.tabBarController as? RunTabBarController
            runTabBarController?.exitGroup()
        }
        
        alert.addAction(stayAction)
        alert.addAction(exitAction)
        
        alert.preferredAction = alert.actions[0]
        
        present(alert, animated: true, completion: nil)
    }
    
    
    //MARK: - Map Updates for Group
    
    func updateMapAnnotation(for runnerArray: [Runner]) {
        
        for runner in runnerArray {
            
            guard runner.runnerID != userID else {return}

            if let coordinate = runner.location { //If person is running and is not the user, proceed
                
                if let annotation = runnerAnnotationsDictionary[runner.runnerID] { //Have existing annotation
                    
                    annotation.coordinate = coordinate
                    annotation.distance = runner.distance! //Exists if location exists
                    annotation.avgPace = runner.averagePace
                    
                    let annotationView = mapView.view(for: annotation) as? RunnerMarkerView
                    annotationView?.setDetailCalloutLabel()
                
                } else { //No existing annotation
                
                    let newAnnotation = RunnerMKAnnotation(name: runner.name, coordinate: coordinate, distance: runner.distance!, pace: runner.averagePace)

                    if runner.runnerID == ownerID {newAnnotation.owner = true}
                    
                    runnerAnnotationsDictionary[runner.runnerID] = newAnnotation
                    mapView.addAnnotation(newAnnotation)
                }
                
            } else { //Person does not have a location
                removeRunnerAnnotation(withID: runner.runnerID)
            }
        }
    }
    
    
    func removeRunnerAnnotation(withID runnerID: String) {
        
        if let annotation = runnerAnnotationsDictionary[runnerID] {
            
            mapView.removeAnnotation(annotation)
            runnerAnnotationsDictionary.removeValue(forKey: runnerID)
        }
    }
    
    
    //MARK: - Map Updates for User
    
    func updateUserOnMap(newLocations: [CLLocation]) {
            
        centreCoordinate = newLocations.last!.coordinate
        
        if newLocations.count > 1 { //Run started
            
            for i in 0..<newLocations.count - 1 {
                
                let coordinates = [newLocations[i].coordinate, newLocations[i+1].coordinate]
                mapView.addOverlay(MKPolyline(coordinates: coordinates, count: coordinates.count)) //Add a new line segment given the new location
            }
        }
        
        if !initialMapRegionSet {
            
            let region = MKCoordinateRegion.init(center: centreCoordinate!, latitudinalMeters: 2000, longitudinalMeters: 2000) //Set the map region based on the new measurement
            mapView.setRegion(region, animated: false)
            initialMapRegionSet = true
        }
    }
    
    
    func clearMap() {
        mapView.removeOverlays(mapView.overlays)
    }
    
    
    @IBAction func centrePressed(_ sender: UIButton) {
        centreMap()
    }
    
    
    func centreMap() {
        
        guard let coordinate = centreCoordinate else {return}
        mapView.setCenter(coordinate, animated: true)
    }

    
}


//Add a new line segment to the map
extension MapViewController: MKMapViewDelegate {
    
    func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
        
        guard let polyline = overlay as? MKPolyline else {
            return MKOverlayRenderer(overlay: overlay)
        }
        
        let renderer = MKPolylineRenderer(polyline: polyline)
        renderer.strokeColor = .blue
        renderer.lineWidth = 5
        return renderer
    }
    
    
}
