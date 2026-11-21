# GiggleOS Appearance

The prototype now stores a shared appearance choice:
- Appearance mode: follow Android system, light, or dark.
- Accent palette: Ocean, Jade, or Coral.
- Choices persist in app-private SharedPreferences and are read by GiggleOS UI activities.
- No privileged Android theme APIs or overlay permissions are requested.

This is an app-level theme for the prototype. A production AOSP port must connect these preferences to framework/SystemUI resources and support dynamic color contrast, wallpaper-derived colors, per-user settings and accessibility contrast requirements.
