# GiggleOS product architecture

## Product decisions

- **Platform:** AOSP-based operating system, with device-specific ports.
- **Interface direction:** hybrid. Material 3 is the foundation for layout, hierarchy, accessibility and interaction patterns. Liquid Glass is reserved for selected surfaces such as quick panels, dialogs and transient overlays. GiggleOS owns the visual identity, iconography, motion language and system experiences.
- **Application strategy:** both. Ship first-party GiggleOS apps while preserving Android application compatibility through the standard Android application framework and runtime.
- **First device target:** Xiaomi Redmi Note 8 Pro (`begonia`). Hardware bring-up remains research-only.

## Layer boundaries

| Layer | Responsibility | Initial implementation direction |
|---|---|---|
| Device / vendor | Kernel, HAL, firmware interfaces, device-specific configuration | Audited upstream components for one exact device |
| Android platform | Framework APIs, ART, package manager, permissions, services | AOSP baseline selected after device compatibility audit |
| Giggle System UI | Home, lock screen, status/navigation surfaces, quick settings | Kotlin + Android platform APIs; custom design system |
| Giggle Services | Cross-app settings, device capabilities, update coordination | Small, separately testable platform services |
| First-party apps | Settings, Files, Phone, Messages, Camera, Gallery, Clock, Calculator | Independent Android app modules |
| Compatibility | Install and run standard Android APKs | Preserve platform package/runtime behavior; test API and ABI coverage |

## Application compatibility policy

GiggleOS will not claim that every Android app works. Compatibility depends on the selected AOSP release, Android API level, ABI support, Google-dependent services, DRM, device integrations and app-specific restrictions.

The baseline goal is:
1. Support standard APK installation using Android's package manager.
2. Preserve Android framework APIs and ART behavior from the selected AOSP base.
3. Test common app categories and document known limitations.
4. Treat Google Mobile Services as a separate licensing and distribution question; do not bundle proprietary services without authorization.

## First-party app roadmap

1. Settings — device information, connectivity entry points, display, sound, privacy and system updates.
2. Files — local storage browsing and document provider integration.
3. Clock — alarms, timer, stopwatch and world clock.
4. Calculator — accessible standard and scientific modes.
5. Gallery — local media browsing.
6. Camera — only after device camera HAL capabilities are validated.
7. Phone and Messages — only after telephony stack and regional requirements are validated.

## UI principles

- Readable typography and clear contrast come before decoration.
- Motion should communicate state changes and remain interruptible.
- Glass effects are limited to selected elevated surfaces; avoid persistent blur behind every screen.
- Provide reduced-motion and high-contrast behavior.
- Do not hard-code one accent color as the only theme.
- Use vector icons and a consistent icon grid rather than emoji placeholders.

## Engineering constraints

- Keep product UI separate from device-specific code.
- Do not embed device-specific assumptions in shared apps.
- Every device profile must identify its codename and research/validation status.
- Keep secrets, signing keys and private credentials out of the repository.
- CI validates metadata and project structure only; it does not pretend to build AOSP or produce flashable images.

## Current non-goals

- No universal firmware image.
- No one-click flashing promise before verified recovery and compatibility paths exist.
- No unofficial proprietary service bundle.
- No claim of stable device support before hardware testing.
