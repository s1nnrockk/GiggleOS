# GiggleOS UI design direction

## Chosen direction: next-generation iOS-inspired, with an original GiggleOS identity

The user explicitly rejected Material 3. Do not use Material 3 as the visual foundation.

GiggleOS should feel like a polished next-generation mobile interface in the spirit of what the user imagines for iOS 27: fluid, precise, calm, layered and premium. This is a visual direction, not a claim about Apple's unreleased design and not a request to copy Apple's UI.

## Design language

- **Original identity first:** GiggleOS owns its layout details, icons, typography choices, widgets, sounds, transitions and system behavior.
- **Layered surfaces:** translucent glass-like panels may be used for Control Center-style quick controls, media surfaces and contextual overlays. Keep primary content readable and avoid putting blur everywhere.
- **Fluid motion:** spring-like but controlled transitions, interactive dismissals, smooth app opening/closing and continuous-feeling navigation.
- **Depth and light:** subtle highlights, material depth, soft shadows and restrained reflections. Avoid excessive glow and visual noise.
- **Typography:** clean, expressive and highly legible system typography with strong hierarchy.
- **Color:** adaptive light/dark palettes and several accent families. Do not default to black/purple.
- **Icons:** a custom coherent vector icon family. No emoji placeholders in shipped UI.
- **Widgets:** elegant, information-dense cards with consistent spacing and responsive sizes.

## Core surfaces

1. Lock screen — expressive clock layouts, layered wallpaper treatment, notifications and privacy indicators.
2. Home screen — app grid, dock, widgets, search and smooth page transitions.
3. Quick controls — translucent control surface, brightness, connectivity, media and device controls.
4. Notifications — readable grouped cards and clear notification actions.
5. App switcher — fluid task cards and predictable gestures.
6. Settings — custom grouped navigation, search and clear state controls.
7. System overlays — volume, power menu, permission prompts and incoming-call surfaces.

## Performance and accessibility

- Keep animations interruptible and respect Android animation scale / reduced-motion settings.
- Provide a reduced-transparency mode and opaque fallbacks for legibility.
- Avoid expensive real-time blur on low-memory hardware; use cached or simplified effects where possible.
- Test frame pacing and memory usage on the Redmi Note 8 Pro before enabling visual effects by default.
- Support scalable text, screen readers, contrast requirements and one-handed reach.

## Implementation boundaries

The UI direction does not change the AOSP platform strategy or Android APK compatibility goal. System UI and first-party apps will be implemented as original Android platform components. Device-specific behavior stays outside the shared design system.

## Status

This document records the chosen visual direction. It is not a completed interface implementation and does not imply that an iOS 27 design specification is publicly known.
