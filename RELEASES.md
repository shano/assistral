# Release Process

## Current Stable Release

- **Tag:** `v3.2.2`
- **Version Name:** `3.2.2`
- **Version Code:** `321`

## Release Checklist

1. Update app version values in `app/build.gradle`:
   - `versionName`
   - `versionCode`
   - `project.ext.versionNameString`
2. Add changelog file: `metadata/en-US/changelogs/<versionCode>.txt`
3. Commit and push to `master`.
4. Create and push tag:
   ```bash
   git tag -a vX.Y.Z -m "Release version X.Y.Z"
   git push origin vX.Y.Z
   ```
5. Confirm GitHub Actions `Build Release APK` workflow succeeded.
6. Confirm GitHub Release contains asset `assistral-release-X.Y.Z.apk`.

## GitHub Secrets Required For Signed Releases

Configured in repository settings under **Secrets and variables -> Actions**:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_STORE_PASSWORD`
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`

These map to Gradle properties used by `app/build.gradle`:

- `MYAPP_RELEASE_STORE_FILE`
- `MYAPP_RELEASE_STORE_PASSWORD`
- `MYAPP_RELEASE_KEY_ALIAS`
- `MYAPP_RELEASE_KEY_PASSWORD`

## Keystore Setup

Generate keystore:

```bash
keytool -genkeypair -v \
  -keystore assistral-release-key.keystore \
  -alias assistral \
  -keyalg RSA -keysize 4096 -validity 10000
```

Convert keystore to one-line base64 (for `SIGNING_KEYSTORE_BASE64`):

```bash
base64 assistral-release-key.keystore | tr -d '\n'
```

## Local Signed Build (Optional)

```bash
./gradlew :app:assembleRelease \
  -PMYAPP_RELEASE_STORE_FILE=/abs/path/to/assistral-release-key.keystore \
  -PMYAPP_RELEASE_STORE_PASSWORD="$MYAPP_RELEASE_STORE_PASSWORD" \
  -PMYAPP_RELEASE_KEY_ALIAS="$MYAPP_RELEASE_KEY_ALIAS" \
  -PMYAPP_RELEASE_KEY_PASSWORD="$MYAPP_RELEASE_KEY_PASSWORD"
```

## F-Droid Update Notes

When updating `fdroiddata` metadata:

1. Add a **new** block in `Builds:` for each release (do not replace old blocks).
2. Set `commit` to the exact commit/tag used for the GitHub release APK.
3. Update:
   - `CurrentVersion`
   - `CurrentVersionCode`
4. Keep `AllowedAPKSigningKeys` unchanged unless signing key changes.

