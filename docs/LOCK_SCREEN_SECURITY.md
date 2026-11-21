# GiggleOS lock screen and credential security

## Current app prototype

The Setup app includes a polished, full-screen lock-screen preview at `setup/app/src/main/java/org/giggleos/setup/LockScreenActivity.kt`.

The preview currently provides:
- Live system-formatted time and date through Android `TextClock`.
- Current battery percentage and charging state from the system battery broadcast.
- A restrained gradient background and adaptive layout with no third-party UI dependencies or continuous blur effects.
- Notification privacy text sourced from Setup preferences. This is a preference preview only; it does not render or redact real system notifications.
- Immersive full-screen presentation and screenshot blocking while the preview is visible.
- A security status sourced from Android `KeyguardManager.isDeviceSecure`.
- A credential-confirmation handoff to Android's trusted credential UI, plus a route to Android security settings.

## Credential security policy

GiggleOS must never collect a device PIN, pattern or password in its own text fields, store credentials in preferences, log them, send them over a network, or implement a home-grown lockout algorithm.

The preview delegates confirmation to Android. Android owns credential storage, secure verification, retry throttling and supported credential types. The application receives only the authentication result. The app preview does not replace the device's lock screen and does not protect the device before Android starts.

Biometric enrollment and management remain in operating-system security settings. GiggleOS must not access or store fingerprint templates.

## Production AOSP work still required

Production GiggleOS must integrate its lock-screen presentation with AOSP SystemUI/Keyguard and preserve Android's trusted credential, Gatekeeper/Weaver, hardware-backed Keystore and biometric services. Do not replace those services with a custom Kotlin or Rust password checker.

The production implementation must cover:
- Keyguard lifecycle across boot, sleep, reboot, user switching and device policy changes.
- Real notification rendering and per-app/private-content redaction.
- Trusted credential confirmation and biometric fallback.
- Emergency calling and accessibility affordances.
- Lockout, recovery and device-reset behavior delegated to platform security services.
- Device-specific validation of fingerprint sensor and secure hardware support.
- Always-on display and burn-in-safe clock placement, only where the panel and device configuration support it.

## Performance and accessibility

The app preview intentionally uses platform widgets, one static gradient and no continuous GPU-heavy blur. Time formatting is delegated to `TextClock`, so no custom polling timer is needed. Battery updates are registered only while the activity is in the foreground and are unregistered on pause. Controls have readable labels and focus support.

## Status

The updated app preview is not the actual Android Keyguard. AOSP SystemUI integration, real notification rendering, biometric integration testing, device validation and a signed system image are not complete.
