# Xonar Browser

Xonar is a modern, privacy-first Android browser built with Jetpack Compose and Kotlin.
It features strict Identity Isolation to keep your browsing sessions, tabs, history, and downloads separated by user-defined contexts.

## Architecture

Xonar relies on modern Android development practices:
- **UI:** Jetpack Compose (Material Design 3)
- **Dependency Injection:** Hilt
- **Local Storage:** Room Database and DataStore
- **Concurrency & Async Tasking:** Kotlin Coroutines and WorkManager
- **Web Rendering:** WebKit via Android WebView

## Key Features

1. **Identity Isolation:** Manage independent sessions (Work, Personal, Shopping, etc.).
2. **Ad & Tracker Blocking:** Uses a custom WebViewClient interception engine.
3. **Biometric Security:** Identities can be locked with Fingerprint/Face Unlock via `BiometricPrompt`.
4. **Download Manager:** Uses WorkManager for background downloading per-identity.
5. **Reader Mode:** Dynamically re-styles web pages via JavaScript evaluation.
6. **Gesture Navigation:** Swipe to go back/forward in tabs.
7. **Privacy Controls:** Data clearing and incognito template support.
8. **Extension-Ready:** Domain architecture is laid out for future plugin integration.

## Build Instructions

To build Xonar, open the project in Android Studio or run the following Gradle task:

```bash
./gradlew assembleDebug
```

## Note on WebView

Xonar uses multiple independent WebViews allocated per active TabSession. Future optimization will pool these views to minimize memory footprint. Currently, session caching isolates cookies and DOM storage based on the `Identity` context.
