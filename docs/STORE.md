# GiggleOS Store

## Current implementation

The prototype includes a native Giggle Store discovery screen with:
- A curated starter catalog and category/search filtering.
- App detail hand-off through the Android market intent.
- A browser fallback to the public Google Play listing/search page when no market handler is available.
- Entry points from the home screen and installed-app drawer.

The store does not host APK files, scrape app packages, impersonate Google Play, or claim to install applications itself. The selected external store handles listing details, user consent, installation, updates, signatures and payment.

## Google Play boundary

Google Play Store and Google Mobile Services are proprietary Google products. They cannot simply be copied into an AOSP image or redistributed by this project. A production GiggleOS build may only include Google Play through an authorized device/build distribution path and applicable Google licensing/certification. The prototype only opens Google Play when it is already available on the user's device, otherwise it opens the public web listing.

## Next engineering steps

1. Decide whether GiggleOS will ship with licensed Google Mobile Services on eligible builds or remain AOSP-only.
2. Add a real first-party catalog backend only after choosing a lawful app metadata source and update policy.
3. Verify package identity, signing certificates, source provenance and version metadata before offering any direct APK install.
4. Use Android PackageInstaller with explicit user confirmation for any future sideload flow; never silently install or update packages.
5. Add accessibility, offline, error-state and low-memory tests.
