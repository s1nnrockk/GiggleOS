# GiggleOS lock screen and credential security

## Current application prototype

`setup/app/src/main/java/org/giggleos/setup/LockScreenActivity.kt` is an application-level lock-screen preview. It includes:
- A GiggleOS clock and date layout.
- Clock-size and notification-privacy preview values saved by Setup.
- A status line that checks whether Android reports the device as secured.
- A lock action that delegates credential verification to Android's trusted Keyguard confirmation screen.
- A direct route to Android security settings when no device credential is configured.
- Screenshot blocking for this preview activity with `FLAG_SECURE`.

## Credential security policy

GiggleOS must never collect a device PIN, pattern or password in its own text fields, store credentials in preferences, log them, send them over a network, or implement a home-grown lockout algorithm.

The prototype asks Android's `KeyguardManager` whether the device is secure and uses `createConfirmDeviceCredentialIntent` for credential confirmation. Android owns credential storage, secure verification, retry throttling and supported credential types. The application receives only the authentication result.

Biometric enrollment and management remain in the operating system's security settings. The app must not access or store fingerprint templates.

## Production AOSP work still required

This preview is not the phone's actual lock screen and cannot protect the device before Android starts. Production GiggleOS must integrate its lock-screen presentation with AOSP SystemUI/Keyguard and preserve Android's trusted credential, Gatekeeper/Weaver, hardware-backed Keystore and biometric services. Do not replace those services with a custom Kotlin or Rust password checker.

The final implementation must also cover:
- Keyguard lifecycle and lock state across boot, sleep, reboot and user switching.
- Secure notification redaction and per-app notification visibility.
- Trusted credential confirmation and biometric fallback.
- Emergency calling and relevant accessibility affordances.
- Lockout, recovery and device-reset behavior delegated to platform security services.
- Device-specific validation of fingerprint sensor and secure hardware support.

## Status

The preview source is added. AOSP SystemUI integration, real notification rendering, biometric integration testing and device validation are not complete. No production lock-screen or firmware claim is made.
