# TLI Loot Scanner — Android proof of concept

This is **source code, not an APK**, and has not been compiled or tested on a Samsung phone. It targets Android 10+ and requires an Android build environment. AndroidIDE may build Gradle projects on-device, but its original repository is archived/unmaintained. Use a trusted, compatible Android IDE or a cloud build service. No computer is intrinsically required if an on-phone build environment works.

## Functionality
- MediaProjection foreground service requests explicit screen sharing approval each capture session.
- OCR samples the shared display about every two seconds using Google's on-device ML Kit Latin recognizer.
- Live mode emits *unverified visible text candidates*, NOT confirmed loot. It will also detect UI text and may create many false positives. Candidate review is required.
- Inventory baseline stores recognized lines locally. Comparison reports newly seen text, which is NOT a reliable item-level inventory diff (scroll position, text layout, quantities and OCR noise can change).
- Confirmed drops, estimated FE value, costs, session clock, estimated FE/hour, JSON share export are saved locally.

## Build
1. Extract ZIP to a directory.
2. Open directory as an existing Gradle project in a compatible Android IDE.
3. Allow Gradle to download Android Gradle Plugin and ML Kit; build/install a debug APK.
4. Open app, grant notifications if asked, tap Start LIVE scan and accept Android's screen-sharing dialog. Switch to Torchlight Infinite.
5. Return to the tracker, tap candidate list, select candidate, enter estimated FE and confirm. Or use inventory baseline/compare mode.

## Known limitations
- OCR is **not a reliable automatic loot drop detector**. It captures arbitrary text and does not know whether an item was picked up. The current build is an experimental screen OCR logger with manual validation.
- Capture may stop when Android revokes permission, game changes resolution, or screen capture is blocked.
- Inventory mode uses exact text-set comparison, not item images or item-stack reconciliation.
- No Exchange price feed, game database, trade tax model, map segmentation, or automatic drop prices.
- FE/hour includes confirmed entries only and manual costs. Prices are estimates. Session time continues until reset and includes breaks.
- On some phones the OS may throttle the capture service. Keep the foreground notification active.
- Privacy: screens can contain private notifications or messages. The app processes OCR locally and saves candidate text locally. Avoid capturing sensitive screens; stop capture when leaving the game.
- Please review game terms and Android screen-capture rules before using third-party overlays/tools.

## Next engineering steps
1. Collect redacted screenshots of Torchlight loot labels and inventory, then calibrate ROI coordinates and UI state detection.
2. Build item-name allowlist from a user-provided game item catalog and add OCR fuzzy matching.
3. Inventory image/template matching and stack-quantity parsing; distinguish pickup vs mere visibility.
4. Add per-map start/finish detection and a manual FE price book or licensed official price source.
5. Add explicit review queue, candidate deletion, quantity editing, and better exports.

This is a prototype intended for further development, not a ready-to-install verified drop tracker.
