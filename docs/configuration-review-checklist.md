# Verify a build

From `android-app` with JDK 17 and SDK 34, run:

```sh
./gradlew :app:assembleDebug :app:assembleRelease
```

Debug allows unencrypted traffic only to loopback for local development. The main network policy prohibits cleartext traffic. Release uses Android's R8 optimizer with the default optimized rules, code minification and resource shrinking. The Gradle property for changing the API accepts an HTTPS origin without credentials.

Inspect these local artifacts:

- `app/build/outputs/mapping/release/mapping.txt`: confirm mappings for app code. Android entry-point names can remain stable.
- `app/build/outputs/apk/release/app-release-unsigned.apk`: inspect the merged manifest. Debuggable and backup should be disabled. A release for distribution requires your own signing process.
- An exported JSON from **Export build report** on the device: debug reports debugging enabled and R8 disabled, release reports the opposite. The R8 value declares the configured build variant. Validate the artifact separately when auditing a real release.

The mapping, APKs, keys and signing material stay out of Git. The UI reports build settings only. It does not infer that a device is compromised or that an attack occurred.

[Android R8 build configuration](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization) · [Android network security configuration](https://developer.android.com/privacy-and-security/security-config)
