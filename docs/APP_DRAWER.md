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

## Icon appearance
Long-press an installed app tile to choose a custom image background, a preset color, or reset it. The selected background is stored per package name. The actual app icon remains in the foreground. See [ICON_PERSONALIZATION.md](ICON_PERSONALIZATION.md).

## Store entry
The app drawer links to Giggle Store. Its catalog is a discovery layer; app listing and installation are handed off to Google Play when available. See [STORE.md](STORE.md).
