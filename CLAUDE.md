# Assistral - CLAUDE.md

## Project Overview

Assistral is a minimal Android WebView wrapper for Mistral's Le Chat (chat.mistral.ai). It is privacy-focused, blocking unnecessary URLs by default while providing a clean native app experience.

Originally forked from Divested Computing Group's GMaps WV (GPLv3).

## Architecture

Single-activity Android app — no fragments, no ViewModel, no dependency injection.

- `app/src/main/java/org/shano/assistral/MainActivity.java` — all core logic
- `app/src/main/java/org/shano/assistral/SwipeTouchListener.java` — swipe gesture handling
- `app/src/main/java/org/shano/assistral/GithubStar.java` — star/rating prompt dialog
- `app/src/main/AndroidManifest.xml` — permissions and activity config
- `app/src/main/res/layout/activity_main.xml` — layout (WebView + ImageButton)

## Key Behaviours

### URL Allowlisting (restricted mode, default ON)
`shouldInterceptRequest` and `shouldOverrideUrlLoading` both enforce the same allowlist:
- `cdn.auth0.com`, `chat.mistral.ai`, `mistral.ai`, `api.mistral.ai`, `console.mistral.ai`, `mistralcdn.net`, `blob.core.windows.net` (Azure Blob — Mistral audio/file uploads)
- Gravatar avatars are intercepted and replaced with a local `assets/avatar.png`
- Microsoft/Google/Apple OAuth domains trigger a toast + `resetChat()`

Toggle via the lock button (top-right). Swipe up to hide it.

### Permissions
- `INTERNET` — network access
- `RECORD_AUDIO` — voice input; requested at runtime via `onPermissionRequest` WebChromeClient callback; pending WebView `PermissionRequest` stored as field so it can be granted after Android runtime permission is approved
- `READ_EXTERNAL_STORAGE` — file uploads via `onShowFileChooser`
- `MODIFY_AUDIO_SETTINGS` — audio routing

### Audio / Voice Input
WebView `onPermissionRequest` handles `RESOURCE_AUDIO_CAPTURE`. A `pendingPermissionRequest` field stores the WebView request across the async Android permission dialog so `request.grant()` can be called in `onRequestPermissionsResult`.

### File Upload
`onShowFileChooser` + `onActivityResult` with `FILE_CHOOSER_REQUEST_CODE = 1`.

### User Agent
Modified to appear as a Linux desktop browser (`modUserAgent()`) to avoid mobile-specific Mistral UI limitations. Applies in both restricted and unrestricted modes.

### Cookies
First-party cookies enabled; third-party cookies disabled.

## Build

Standard Gradle Android project. Min/target SDK in `app/build.gradle`.

```
./gradlew assembleRelease
```

Signing properties passed as Gradle properties (see `gradle.properties` and CI workflows).

## Release Process

Releases are triggered by pushing a `v*` tag. The `release.yml` workflow builds a signed APK and publishes a GitHub release automatically.

**Before tagging, always bump the version in `app/build.gradle`** — three values must stay in sync:
```
versionCode 324                          # increment by 1 each release
versionName "3.2.4"                      # human-readable version
project.ext.versionNameString = "3.2.4" # controls the APK filename
```

The APK filename is generated from `versionNameString`, not the git tag. If you tag without bumping these, the release artifact will show the old version number.

```bash
# Full release checklist:
# 1. Bump versionCode, versionName, versionNameString in app/build.gradle
# 2. Commit and push
git add app/build.gradle && git commit -m "chore: bump version to X.Y.Z"
git push
# 3. Tag and push to trigger the release workflow
git tag vX.Y.Z && git push origin vX.Y.Z
```

## Distribution

- F-Droid: `metadata/org.shano.assistral.yml` + `metadata/en-US/`
- Fastlane metadata: `fastlane/metadata/android/`
- GitHub Actions: `.github/workflows/main.yml` (CI), `release.yml` (signed APK release)

## Conventions

- No third-party libraries beyond AndroidX WebKit compat
- Java only (no Kotlin)
- Keep the allowlist minimal and relevant to Mistral
- Do not add Google/social login support — by design
- Version is hardcoded in build config for F-Droid compatibility
