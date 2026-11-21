# GiggleOS App Drawer

## Implemented
- Lists actual launchable applications installed on the host Android device.
- Uses each installed app's own icon and display name.
- Live search filters the installed app list.
- Launches the selected app through its resolved launcher component.
- Handles empty results and unavailable launch targets without crashing.
- Does not request broad package-install or storage permissions.

## Android package visibility
The manifest declares only the launcher intent query needed to enumerate apps. Android package visibility rules still apply, and vendor/system apps without a launcher activity are intentionally not listed.

## Prototype boundary
This app drawer runs inside the GiggleOS launcher prototype. A production AOSP launcher should use the platform launcher model, profile-aware app list, work profile separation, suspended-app state and proper task/recents integration.
