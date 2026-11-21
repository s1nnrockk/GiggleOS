# GiggleOS Setup

Native Android first-run wizard prototype source.

## Modules
- `app/`: Android UI and platform-intent integration.
- `native/`: Rust state validation and migration-package integrity primitives.
- `docs/SETUP_WIZARD.md`: full product flow and limitations.

## Build the Rust library on Windows
Install Rust, Android NDK and Android SDK, then install cargo-ndk:

```powershell
cargo install cargo-ndk
cd setup/native
.\build-android.ps1
```

Then build the Android module from the `setup` directory with Android Studio or Gradle.

The Kotlin app has a safe fallback if the Rust shared library has not been built yet. Rust acceleration is active only when `libgiggle_setup_core.so` is present.

## Platform integration status
This is an application-level prototype. A production first-boot wizard must be integrated as a privileged AOSP setup component and connected to the selected device's verified system services. Wi-Fi, SIM, lock credentials and biometrics are opened through Android settings entry points here; this prototype does not silently change protected system settings.

Data migration UI and category selection are present as a flow foundation. A complete cross-device transfer protocol and receiver are not implemented yet.
