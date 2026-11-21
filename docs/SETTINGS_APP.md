# GiggleOS Settings prototype

## Implemented
- Native settings hub with grouped sections and live search.
- Connectivity entries for Wi-Fi, Bluetooth, mobile network and airplane-mode settings.
- Personalization entries for display, theme-related Android settings and lock-screen preview.
- Sound, notification, security, privacy, battery and accessibility entries.
- About dialog reports the actual host device and Android version and clearly labels the build as a prototype.
- Appearance settings persist system/light/dark mode and Ocean/Jade/Coral palette choices.
- Lock-screen wallpaper picker persists one of four local gradient presets.
- Protected system controls open Android's own settings activities instead of simulating privileged access.

## Boundary
This is a settings hub inside the prototype APK, not the platform Settings provider. Some settings actions depend on the Android version and device vendor; unavailable destinations show a short message. Preferences are not yet wired into a custom AOSP settings provider.

## Next
- Add GiggleOS-owned appearance preferences and persistent theme switching.
- Connect system settings providers during AOSP integration.
- Add per-device capability checks and accessibility tests.
