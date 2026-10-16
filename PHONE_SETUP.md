# Build from a Samsung phone (GitHub)
1. Extract the ZIP using Samsung My Files. Do not upload the ZIP alone.
2. In your GitHub repository, use Add file > Upload files. Select project files and directories from the extracted folder. GitHub mobile web may not support uploading whole directories; if so use Chrome > Desktop site, or a Git client app supporting folder uploads.
3. Confirm `app/build.gradle`, `settings.gradle`, `build.gradle`, and `.github/workflows/build-apk.yml` exist in the repository. The `.github` folder may be hidden by My Files; enable Show hidden system files.
4. Commit to the `main` branch.
5. Open repository > Actions > Build Android APK > Run workflow. A push to main also triggers it.
6. Wait for the workflow to finish. Open its run > Artifacts > TLI-Loot-Tracker-debug-apk, download and extract the artifact ZIP, then install `app-debug.apk`.
7. Samsung may require permission to install unknown apps. Only install APKs from your own GitHub Actions build.
8. When first launched, allow the Android screen-capture prompt. This is an unverified prototype; it may crash or report unrelated screen text as loot.
If the workflow fails, copy the first meaningful error from the Build debug APK step back into ChatGPT.
