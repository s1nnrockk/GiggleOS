# GiggleOS System UI prototype

## Scope

The app-level System UI prototype provides a cohesive quick-settings surface, live clock/date and battery reading, an explicit empty notification state, and entry points to actual Android settings screens.

## Implemented in the prototype

- Native Android views; no WebView or external UI framework.
- Responsive vertical layout with two-column quick-setting cards.
- Live time/date through Android TextClock.
- Battery percentage queried from the system battery broadcast when rendering.
- Wi-Fi, Bluetooth, display, sound, battery, security, notification and general settings cards open corresponding Android settings activities.
- A route to the GiggleOS lock-screen preview.
- Reduced animation behavior respects the system animator duration scale.
- Screenshot capture is disabled while the preview is visible.

## Honest capability boundary

This is not the privileged AOSP SystemUI process. It cannot replace Android's real status bar or notification shade, render real notification records, directly change privileged radios, adjust system brightness without permissions, show a trusted power menu, or perform shutdown/reboot. Quick-setting cards intentionally open Android settings instead of pretending to toggle device state.

The notification panel is an explicit empty state, not a fabricated feed.

## Production integration plan

1. Move presentation into an AOSP SystemUI module and connect it to the platform status-bar service.
2. Use platform Quick Settings tile APIs for internet, Bluetooth, Do Not Disturb, airplane mode, battery saver, flashlight and screen controls, with per-device capability checks.
3. Subscribe to the platform notification pipeline and apply lock-screen redaction rules from LOCK_SCREEN_SECURITY.md.
4. Implement trusted power-menu actions through the platform global-actions service, retaining emergency and accessibility affordances.
5. Integrate lock-screen visuals with Keyguard rather than launching an app Activity.
6. Add tests for rotation, font scaling, night mode, accessibility traversal, process recreation and device configuration changes.
7. Validate on the exact target device before marking hardware tiles supported.

## Performance

The prototype uses platform TextViews, TextClock, simple shape drawables and a ScrollView. It avoids continuous animation loops, image-heavy backgrounds, custom blur and polling timers.
