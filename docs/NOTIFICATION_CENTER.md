# GiggleOS Notification Center

## Implemented in the app prototype
- Optional Android NotificationListenerService, enabled only by the user through Android's Notification Access settings.
- Stores a maximum of 30 recent notification records.
- Stores only the source app label, package identity, notification key and arrival time.
- Never reads or stores notification title, body, images, actions, or reply content.
- Removes entries when Android reports the notification was removed.
- The home control center shows a clear empty state until access is explicitly granted.
- Tapping a record opens its source application, not the private notification content.
- The user can clear the local notification index.

## Privacy and platform boundary
The service does not post, cancel, reply to, or interact with notifications. The app-level control center is not the Android notification shade. A production AOSP SystemUI implementation must use the platform notification pipeline and per-user lock-screen redaction rules.
