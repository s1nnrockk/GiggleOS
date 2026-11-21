# GiggleOS icon personalization

## Available in the Android prototype

Long-press an icon on the home screen or in the app drawer to open **Icon appearance**:
- Choose an image from the Android document picker as that icon's background.
- Choose from the built-in accent color palette.
- Reset the icon to its default appearance.

Overrides are stored locally in app-private preferences and keyed by the home-screen icon key or installed app package name. Image access is persisted through Android's Storage Access Framework; the app does not request broad storage permissions. If a selected image is removed or becomes inaccessible, the UI falls back to the saved/default color.

The app drawer keeps the original installed-app icon above the selected background. The home screen keeps GiggleOS's original vector glyph above it. The override changes only the launcher presentation; it does not modify the APK, the app's actual icon resource, or icons shown in other launchers.

## Production direction

- Use Android adaptive icon masks and monochrome layers when integrating into AOSP.
- Keep custom background and foreground layers separate.
- Support image crop, preview, reset, and per-icon color.
- Persist settings per Android user/profile and handle package uninstall/reinstall.
- Add image size limits, decode sampling, URI revocation handling and accessibility descriptions.
- Respect reduced transparency and contrast preferences.
