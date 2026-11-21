# GiggleOS Setup

Android first-run wizard prototype for the GiggleOS platform.

## Screens

The native Android activity contains the complete first-run navigation flow:
1. Welcome
2. Language
3. Region and time
4. Wi-Fi entry point
5. SIM and mobile-network entry point
6. Data-transfer source and category selection
7. Screen-lock setup entry point
8. Biometric enrollment entry point
9. Lock-screen clock and notification privacy preview
10. Appearance, navigation and accessibility preferences
11. Review
12. Finish

The UI uses Android platform widgets and a custom GiggleOS visual layer. It does not depend on a webview or a component-template framework.

## Modules

- `app/`: Android setup UI, lock-screen preview, platform credential confirmation, progress persistence and system-settings entry points.
- `native/`: Rust navigation validation, transfer-category bitmask validation and SHA-256 integrity primitive.
- `docs/SETUP_WIZARD.md`: product behavior, data-transfer boundaries and AOSP integration contract.

## Build the Rust library on Windows

Install Rust, Android Studio/SDK and Android NDK. Then install cargo-ndk:

```powershell
cargo install cargo-ndk
cd setup/native
.\build-android.ps1
```

Build the Android prototype from the `setup` directory using Android Studio or Gradle:

```powershell
gradle :app:assembleDebug
```

The Kotlin activity has a fallback for environments where the Rust shared library has not yet been built. The Rust core is loaded when `libgiggle_setup_core.so` is present under the matching Android ABI.

## What is and is not functional

- Step navigation and progress persistence are implemented.
- Language, region, appearance, navigation and lock-screen preview choices are stored by the prototype.
- Wi-Fi, date/time, security, biometric and accessibility actions open Android system settings. They do not bypass platform permission checks.
- The lock-screen preview uses Android's trusted credential confirmation UI; it never collects or stores a PIN/password. It is not yet the real system lock screen. See [LOCK_SCREEN_SECURITY](../docs/LOCK_SCREEN_SECURITY.md).
- Data-source and category selection are implemented, but the cross-device transport, pairing protocol and receiver are not implemented.
- Selected appearance and lock-screen preferences are not yet applied to the real System UI.
- The activity is not yet registered as the privileged first-boot Setup Wizard in AOSP.

This is source code for an application-level prototype. No APK, system image or flashable firmware has been built or device-tested yet.
