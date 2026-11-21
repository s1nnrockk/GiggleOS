# GiggleOS Home Screen prototype

## Included
- Native Android home screen with a live clock and date.
- A custom visual identity using a restrained teal/blue palette, layered cards and rounded app tiles.
- Original vector-drawn line icons for the built-in app grid and dock; no emoji icon placeholders.
- Dynamic-Island-inspired capsule used as an entry point to the control center.
- App grid and dock with Android intent hand-off for dialer, messaging, camera, gallery, browser, settings, calendar, clock, files, contacts and audio apps.
- Control center cards open real Android settings pages; they do not pretend to toggle privileged hardware state.
- Explicit empty notification state rather than fake notifications.
- Animation respects the system animator duration scale and avoids continuous rendering.

## Prototype boundary
This Activity is an app-level launcher prototype. It is not the privileged Android HOME role, does not replace the platform launcher or SystemUI, and cannot control protected device services. The browser tile uses a generic web intent; Android chooses the installed browser. Some app intents may be unavailable on a given device and show a fallback message.

## Next integration steps
1. Add a proper HOME intent filter and onboarding/default-launcher flow after the platform build is selected.
2. Add adaptive icon assets and refine icon accessibility labels.
3. Add a real notification listener only with explicit user consent and lock-screen redaction.
4. Move control center actions into privileged AOSP SystemUI for real Quick Settings.
5. Add widget hosting, app drawer/search, wallpaper selection and gesture navigation.
6. Validate memory use, font scaling, rotation, accessibility and touch targets on begonia.
