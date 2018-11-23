//
//  SearchTableViewController.swift
//  Pack Track
//
//  Created by Keegan Jebb on 2018-05-07.
//  Copyright © 2018 Keegan Jebb. All rights reserved.
//

import UIKit
import Firebase

class SearchTableViewController: BaseTableViewController {
    
    var userRunGroupIds: [String]? {
        didSet {
            configureTableView()
            downloadGroups()
        }
    }
    
    var baseRunGroupsList = [RunGroup]()
    
    var displayedRunGroups = [RunGroup]()
    
    var alert: UIAlertController?

    @IBOutlet weak var searchBar: UISearchBar!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        searchBar.delegate = self
    }
    
    
    override func configureTableView() {
        
        super.configureTableView()
        
        tableView.register(UINib(nibName: "RunGroupCell", bundle: nil), forCellReuseIdentifier: "runGroupCell")
    }
    

    //MARK: - TableView Data Source Methods
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return displayedRunGroups.count
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "runGroupCell", for: indexPath) as! RunGroupCell
        
        cell.groupNameLabel.text = displayedRunGroups[indexPath.row].name
        cell.groupOwnerLabel.text = "Owned By: " + displayedRunGroups[indexPath.row].ownerName
        cell.runsWithGroupLabel.isHidden = true
        
        cell.mainBackground.layer.cornerRadius = 5
        cell.mainBackground.layer.masksToBounds = true
        
        cell.mainBackground.backgroundColor = UIColor(white: 0.32, alpha: 0.8)
        
        cell.backgroundColor = UIColor.clear
        
        return cell
    }
    
    
    //MARK: - TableView Delegate Methods
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        alert = UIAlertController(title: "Join This Circle?", message: "", preferredStyle: .alert)
        
        alert?.addTextField(configurationHandler: { (alertText) in
            alertText.placeholder = "Enter Member Code"
        })

        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel) { cancelAction in
            tableView.cellForRow(at: indexPath)?.isSelected = false
        }
        
        let joinAction = UIAlertAction(title: "Join", style: .default) { (addAction) in
            
            if self.alert?.textFields?[0].text == self.displayedRunGroups[indexPath.row].memberCode {

                self.addUserToGroup(withID: self.displayedRunGroups[indexPath.row].fireBaseID)

            } else {

                tableView.cellForRow(at: indexPath)?.isSelected = false
                self.alert?.dismiss(animated: true, completion: nil)
                self.showAlert(title: "Incorrect Code", message: "Please Try Again")
            }
        }
        
        alert?.addAction(cancelAction)
        alert?.addAction(joinAction)
        
        alert?.preferredAction = alert?.actions[1] //Return from keyboard triggers join action
        
        present(alert!, animated: true, completion: nil)
    }
    
    
    //MARK: - Adding and Retrieving from Firebase
    
    func downloadGroups(containing dbSearchString: String? = nil) {
        
        let ref = Database.database().reference()
        
        guard let userGroupIds = userRunGroupIds else {return}
        
        var runGroupsQuery = ref.child(FireBaseString.runGroups.rawValue).queryLimited(toLast: 10)
        
        if let searchString = dbSearchString {
            
            runGroupsQuery = runGroupsQuery.queryOrdered(byChild: FireBaseString.name.rawValue).queryStarting(atValue: searchString).queryEnding(atValue: (searchString + "\u{F8FF}"))
        
        } else {
            
            runGroupsQuery = runGroupsQuery.queryOrderedByKey()
            baseRunGroupsList.removeAll() //Should only happen on the initial download
        }
        
        displayedRunGroups.removeAll()
        tableView.reloadData() //If no results are returned, then it will be blank because of this line. Otherwise, it would show the base groups
        
        runGroupsQuery.observeSingleEvent(of: .value) { (groupsSnapshot) in

            for group in groupsSnapshot.children.allObjects as! [DataSnapshot] {

                let groupKey = group.key
                
                if !(userGroupIds.contains(groupKey)) {
                    
                    let groupSnapshotValue = group.value as! [String : AnyObject]
                    let groupName = groupSnapshotValue[FireBaseString.name.rawValue] as! String
                    let groupOwner = groupSnapshotValue[FireBaseString.owner.rawValue] as! String
                    let groupCode = groupSnapshotValue[FireBaseString.memberCode.rawValue] as! String

                    let newRunGroup = RunGroup()
                    newRunGroup.name = groupName
                    newRunGroup.ownerID = groupOwner
                    newRunGroup.fireBaseID = groupKey
                    newRunGroup.memberCode = groupCode

                    let ownerNameDB = ref.child(FireBaseString.users.rawValue).child(groupOwner).child(FireBaseString.name.rawValue)

                    //Get the name of the owner and add it to the RunGroup object
                    ownerNameDB.observeSingleEvent(of: .value) { (ownerNameSnapshot) in

                        newRunGroup.ownerName = ownerNameSnapshot.value as! String
                        
                        if (dbSearchString == nil) {
                            self.baseRunGroupsList.append(newRunGroup)
                        }
                        
                        self.displayedRunGroups.append(newRunGroup)

                        self.tableView.reloadData()
                    }
                }
            }
        }
    }
    
    
    //Can make this a function accessible to groups VC too
    func addUserToGroup(withID groupID: String) {
        
        let ref = Database.database().reference()
        
        guard let userID = Auth.auth().currentUser?.uid else {return}
        
        let userGroupDB = ref.child(FireBaseString.users.rawValue).child(userID).child(FireBaseString.groups.rawValue).child(groupID)
        
        //Add the group to the user's branch under child "groups" and set runs to 0
        userGroupDB.setValue(0) { (error, reference) in
            
            if error != nil {
                print(error!)
            } else {
                self.navigationController?.popViewController(animated: true)
            }
        }
    }
    

}



//MARK: - Search Bar Methods Extension

extension SearchTableViewController: UISearchBarDelegate {
    
    //When the search button is clicked in the keyboard
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        
        //Remove the keyboard and stop typing in searchbar
        DispatchQueue.main.async {
            searchBar.resignFirstResponder()
        }
        
        if searchBar.text! != "" {
            downloadGroups(containing: searchBar.text)
        }
    }

    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {

        if searchText == "" {

            displayedRunGroups = baseRunGroupsList
            tableView.reloadData()

        }
    }
    
    
}
