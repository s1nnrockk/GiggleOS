# GiggleOS UI design direction

## Chosen direction: Hybrid

GiggleOS combines three clearly separated influences:

- **Material 3 foundation:** spacing, typography hierarchy, adaptive layouts, accessibility semantics, controls and predictable navigation.
- **Selective Liquid Glass:** translucent quick settings, media controls, compact overlays and selected dialogs. Glass is an accent, not a full-screen treatment.
- **GiggleOS identity:** original icon family, custom system surfaces, motion language, illustrations, sounds and carefully selected color palettes.

## Visual rules

1. Do not default every screen to black/purple.
2. Support light and dark themes, plus user-selectable accent colors.
3. Keep text readable over translucent surfaces; use opaque fallbacks when contrast is insufficient.
4. Use designed vector icons, not emoji as permanent UI icons.
5. Keep surfaces calm: clear hierarchy, restrained shadows and consistent corner radii.
6. Avoid blur-heavy effects on low-memory devices; provide a low-effects mode.
7. Use responsive layouts and scalable text.

## Motion rules

- Prefer short, purposeful transitions with spring-like easing only where it clarifies movement.
- Animate state changes, not every element continuously.
- Respect Android animator scale and reduced-motion preferences.
- Never block taps or navigation while an animation is running.
- Test scrolling and transitions on the first target hardware before enabling expensive effects by default.

## Core surfaces

- Lock screen: time/date, notifications, privacy indicators and unlock affordance.
- Home: app grid, dock, widgets and search.
- Quick settings: connectivity controls, brightness, media and system status.
- Notification shade: grouped notifications and conversation actions.
- Settings: searchable categories with clear state descriptions.
- Recents: task switching and app continuity.

## Design status

This document records the selected direction, not a completed UI implementation. Visual components will be implemented and tested incrementally.
