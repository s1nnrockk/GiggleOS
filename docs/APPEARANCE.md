# GiggleOS Appearance

The prototype now stores a shared appearance choice:
- Appearance mode: follow Android system, light, or dark.
- Accent palette: Ocean, Jade, or Coral.
- Lock-screen wallpaper presets: Quiet Coast, Northern Lights, Dawn, and Meadow.
- The selected accent also recolors the lock-screen preview; home, settings, app drawer and System UI preview use the shared palette and light/dark surface colors.
- Choices persist in app-private SharedPreferences and are read by GiggleOS UI activities.
- No privileged Android theme APIs or overlay permissions are requested.

This is an app-level theme for the prototype. A production AOSP port must connect these preferences to framework/SystemUI resources and support dynamic color contrast, wallpaper-derived colors, per-user settings and accessibility contrast requirements.

## Per-icon appearance
The home screen and app drawer support per-icon background overrides. A long press opens image selection, a built-in color palette, and reset. Images are selected through Android's document picker and persisted by URI; no broad storage permission is requested. The setting changes only the launcher surface, not the installed APK resources. See [ICON_PERSONALIZATION.md](ICON_PERSONALIZATION.md).
