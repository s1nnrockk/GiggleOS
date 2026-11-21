# GiggleOS product architecture

## Product decisions
- **Platform:** AOSP-based operating system, with device-specific ports.
- **Interface direction:** original GiggleOS visual identity inspired by a next-generation iOS-like experience. Material 3 is explicitly not the visual foundation.
- **First boot:** a dedicated GiggleOS Setup Wizard runs after a clean install or factory reset, before the home screen.
- **Application strategy:** first-party GiggleOS apps plus the goal of Android application compatibility through the standard framework and runtime.
- **First device target:** Xiaomi Redmi Note 8 Pro (begonia). Hardware bring-up remains research-only.

## Layer boundaries
| Layer | Responsibility | Initial implementation direction |
|---|---|---|
| Device / vendor | Kernel, HAL, firmware interfaces, device-specific configuration | Audited components for one exact device |
| Android platform | Framework APIs, ART, package manager, permissions, services | AOSP baseline selected after device compatibility audit |
| Giggle Setup | First-run flow, setup state, migration entry points, initial preferences | Dedicated platform-integrated setup component |
| Giggle System UI | Home, lock screen, status/navigation surfaces, quick settings | Kotlin + Android platform APIs; original design system |
| Giggle Services | Cross-app settings, device capabilities, update coordination | Small, separately testable platform services |
| First-party apps | Settings, Files, Phone, Messages, Camera, Gallery, Clock, Calculator | Independent Android app modules |
| Compatibility | Install and run standard Android APKs | Preserve platform package/runtime behavior; test API and ABI coverage |

## Setup Wizard boundary
The Setup Wizard is separate from the everyday Settings app. It runs only while initial setup is incomplete, saves progress across reboots, and marks setup complete only after final confirmation. It must support offline setup and safe skip paths.

The wizard requests actual platform settings through Android system APIs. It must not show fake Wi-Fi, SIM, lock-screen, biometric or accessibility controls. Device capabilities are queried through shared interfaces; unsupported hardware steps are omitted or explained.

Data migration is a separate permission-scoped component. It must not collect account passwords, promise universal app-data transfer, or modify the source device. See [SETUP_WIZARD.md](SETUP_WIZARD.md).

## Setup implementation status

The application-level prototype is in [setup/](../setup/). Its UI is implemented with native Android views, while a small Rust library provides state-transition bounds, transfer-category validation and SHA-256 integrity primitives through JNI. Rust is not used to replace Android framework services.

The prototype opens Android settings for protected operations. Production integration still requires a privileged AOSP setup component, verified platform APIs and device-specific capability checks. The migration UI is a foundation only; a secure cross-device transport/receiver remains future work.


## Application compatibility policy
GiggleOS will not claim that every Android app works. Compatibility depends on AOSP release, API level, ABI support, Google-dependent services, DRM, device integrations and app-specific restrictions.

Baseline goals:
1. Support standard APK installation using Android's package manager.
2. Preserve Android framework APIs and ART behavior from the selected AOSP base.
3. Test common app categories and document known limitations.
4. Treat Google Mobile Services as a separate licensing and distribution question.

## First-party app roadmap
1. Settings — device information, connectivity, display, sound, privacy and updates.
2. Files — local storage browsing and document provider integration.
3. Clock — alarms, timer, stopwatch and world clock.
4. Calculator — accessible standard and scientific modes.
5. Gallery — local media browsing.
6. Camera — only after camera HAL validation.
7. Phone and Messages — only after telephony stack and regional validation.

## UI principles
- Original GiggleOS identity; no Material 3 foundation.
- Refined typography, clear hierarchy and strong contrast.
- Fluid, interruptible transitions with restrained spring motion.
- Selective translucent surfaces.
- Adaptive light/dark palettes and multiple accents; no forced black/purple default.
- Custom vector icons.
- Reduced-motion, reduced-transparency and accessibility support.

## Engineering constraints
- Keep product UI separate from device-specific code.
- Do not embed device-specific assumptions in shared apps.
- Every device profile must identify codename and validation status.
- Keep secrets, signing keys and private credentials out of the repository.
- CI validates metadata and structure only; it does not build AOSP or produce flashable images.

## Current non-goals
- No universal firmware image.
- No one-click flashing promise before verified recovery and compatibility paths exist.
- No unauthorized proprietary service bundle.
- No stable-support claim before hardware testing.
