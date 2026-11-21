# GiggleOS Setup

Android first-run wizard and System UI interaction prototypes for GiggleOS.

## Modules

- app/: native setup flow, lock-screen preview, credential handoff to Android Keyguard, and System UI preview.
- native/: Rust navigation validation, transfer-category bitmask validation and SHA-256 integrity primitive.
- docs/SETUP_WIZARD.md: first-run flow and data migration boundaries.
- docs/LOCK_SCREEN_SECURITY.md: lock-screen preview and credential security contract.
- docs/SYSTEM_UI.md: quick-settings preview and AOSP SystemUI integration plan.

## System UI preview

After completing the setup flow, the final screen offers a System UI preview. The preview includes a live clock/date, battery level, quick-setting entry points and an explicit empty notification state. Cards open real Android settings pages; they do not pretend to toggle privileged hardware state.

## Build the Rust library on Windows

Install Rust, Android Studio/SDK and Android NDK. Then install cargo-ndk:

```powershell
cargo install cargo-ndk
cd setup/native
.\build-android.ps1
```

Build the Android prototype from the setup directory using Android Studio or Gradle:

```powershell
gradle :app:assembleDebug
```

The Kotlin activity has a fallback for environments where the Rust shared library has not yet been built. The Rust core is loaded when libgiggle_setup_core.so is present under the matching Android ABI.

## Capability boundary

This is application-level prototype code, not a privileged AOSP first-boot wizard or SystemUI replacement. Android retains control of the real notification shade, status bar, Quick Settings tiles, power menu, device credentials and hardware services. No APK, system image or flashable firmware is claimed as built or device-tested.
