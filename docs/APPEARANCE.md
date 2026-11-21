# GiggleOS Appearance

The prototype now stores a shared appearance choice:
- Appearance mode: follow Android system, light, or dark.
- Accent palette: Ocean, Jade, or Coral.
- The selected accent also recolors the lock-screen preview; home, settings and app drawer use the shared palette and light/dark surface colors.
- Choices persist in app-private SharedPreferences and are read by GiggleOS UI activities.
- No privileged Android theme APIs or overlay permissions are requested.

This is an app-level theme for the prototype. A production AOSP port must connect these preferences to framework/SystemUI resources and support dynamic color contrast, wallpaper-derived colors, per-user settings and accessibility contrast requirements.
