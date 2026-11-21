# GiggleOS Setup Wizard

## Purpose
GiggleOS Setup is the first-run experience shown after a clean installation, first boot, or explicitly requested factory reset. It guides the owner through essential configuration before opening the home screen. This is a specification, not a completed application or firmware build.

## Experience principles
- One clear task per screen, with Back, Continue, and Skip where safe.
- Save completed steps so an interrupted setup can resume after reboot.
- Keep essential setup possible without internet.
- Explain permissions and data transfer in plain language.
- Use the original GiggleOS visual language: refined typography, layered surfaces, selective translucency, custom vector icons, and restrained fluid transitions.
- Respect reduced motion, accessibility text scaling, contrast, and low-memory performance.
- Never imply a feature works until its platform and device implementation are verified.

## First-boot flow
| Order | Screen | Behavior |
|---|---|---|
| 1 | Welcome | GiggleOS identity, welcome message, Start action, language shortcut |
| 2 | Language | Select system language and preview localized labels |
| 3 | Region | Country/region, locale conventions, time zone, automatic date/time option |
| 4 | Wi-Fi | Show available networks, password entry and connection status; allow offline skip |
| 5 | SIM and mobile network | Detect supported SIM/eSIM capabilities; show available settings; allow skip |
| 6 | Data transfer | Offer Android migration, limited iPhone import, or Set up as new |
| 7 | Screen lock | Configure PIN/password/pattern using supported platform APIs; explain any skip option |
| 8 | Biometrics | Offer fingerprint/face enrollment only when a verified biometric HAL is available |
| 9 | Lock screen and appearance | Preview clock, wallpaper, notification privacy, light/dark mode and accent |
| 10 | System preferences | Navigation gestures/buttons, notifications, sound and accessibility shortcuts |
| 11 | Review | Summarize choices and allow changes |
| 12 | Finish | Apply settings, mark setup complete, then open the home screen |

Wi-Fi and migration remain optional. Offline setup must not be blocked by unavailable internet.

## Data migration

Offer three paths:
- Android device: guided local transfer using a temporary pairing code or QR code, with explicit category selection.
- iPhone: limited import for data supported by documented export or interoperability paths.
- Set up as new: skip migration.

Initial categories to investigate: contacts, photos and videos, calendar entries, locally stored documents and selected downloads. Message history is supported only where a documented, permissioned import path exists.

Do not promise transfer of every app, login, password, DRM-protected item or private app database. Android apps generally need to be installed again; app data depends on each app and supported backup mechanisms. iOS app data is not generally portable to Android. Never ask for Apple ID or Google account passwords. Prefer direct local transfer. Show the source, destination, categories and estimated size when available, then request confirmation. Do not erase or modify the old phone. Provide cancellation and retry.

## Settings applied during setup
The wizard must configure real system settings through Android framework/system APIs:
- Wi-Fi and network preferences
- SIM/mobile-network entry points supported by the device
- Locale, time zone and date/time behavior
- Screen lock and supported biometric enrollment
- Lock-screen clock, wallpaper and notification privacy
- Light/dark appearance and accent
- Navigation mode
- Sound and notification preferences
- Accessibility shortcuts and reduced-motion preferences

Hardware-dependent controls must be hidden or clearly marked unavailable until validated.

## Lock-screen preview
Show the selected clock layout, wallpaper crop/readability, notification visibility (full content, sensitive content hidden, or none), supported shortcuts, and appearance. Apply changes to the real lock screen only after confirmation.

## Technical architecture
- Implement a dedicated first-run system component integrated with Android platform setup state.
- Persist progress and the setup-complete flag in protected system storage.
- Use platform services for Wi-Fi, telephony, locale, lock credentials, biometrics, notifications and navigation.
- Keep device-specific checks behind capability interfaces; do not hardcode begonia-only behavior into shared UI.
- Keep migration separate, narrowly permissioned and documented.
- Never store source-device credentials, pairing secrets or copied data in logs.
- Test navigation, process recreation, reboot resume, skip paths, offline setup, migration cancellation and completion.
- Add device integration tests before exposing hardware-dependent steps.

## Redmi Note 8 Pro (begonia) constraints
The exact model number and RAM variant are not confirmed. Fingerprint, telephony, Wi-Fi, Bluetooth and other hardware features must be validated against the selected AOSP base and device-specific HAL/vendor implementation before their setup screens are enabled.

## Current status
**Specification only.** No Setup Wizard application, platform integration, data-transfer implementation or installable GiggleOS image has been built yet.
