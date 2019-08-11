# Initial

- [X] Add Http-Handling
- [X] Add Storage-Handling
- [X] Add Switch to WirelessSocketCard
- [X] Add Routing
- [X] Improve Layout
- [X] Add Filter for Areas to ListPage
- [X] Add Possibility to add and edit areas and wireless sockets
	- [X] Add Area
	- [X] Edit Area
	- [X] Delete Area
	- [X] Add WirelessSocket(WIP)
	- [X] Edit WirelessSocket(WIP)
	- [X] Delete WirelessSocket
- [X] Reload

# Feature

- [X] Show snackBar for succeeded or failed action 

- [X] Add settings page
    - [X] change user data (userName, password & baseUrl)
    - [X] choose theme (light & dark)

- [ ] Android background service with intent filter to accept broadcasts to toggle WirelessSockets 
    - https://dev.to/protium/flutter-background-services-19a4
    - https://flutter.dev/docs/get-started/flutter-for/android-devs#what-is-the-equivalent-of-an-intent-in-flutter
    - https://stuff.mit.edu/afs/sipb/project/android/docs/guide/components/intents-filters.html

- [X] PlayStore Release
    - https://flutter.dev/docs/deployment/android

# Future

- [X] Add periodic task functionality
    - [X] Load periodic task
    - [X] Show in wireless socket card (icon -> onPress routes to task list for socket -> with actions)
    - [X] Add periodic task
    - [X] Update periodic task
    - [X] Delete periodic task

# Bugs

- [X] Fix routing to loading while adding/updating/deleting
    - [X] Area
    - [X] WirelessSocket
    - [X] PeriodicTask
- [X] Fix dropdown index bug after adding an area
- [X] Stabilize method fromString in helper/icon.helper
- [ ] Update wireless socket icon in detail view while editing
    - Barely possible with TextFormField... TextField supports onChange, but is not made for Forms...
- [X] Fix layout in details view
    - [X] Area
    - [X] WirelessSocket
    - [X] PeriodicTask