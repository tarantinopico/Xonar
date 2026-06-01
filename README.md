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

Xonar allocates independent profiles using `androidx.webkit.ProfileStore` to guarantee absolute data isolation. Each identity utilizes a separate storage footprint on the disk.

## Testing Strategy

The complete test suite verifies core business rules and behavior without brittle UI assumptions. This includes:
- **Unit Tests:** Run locally utilizing JUnit 4, verifying `IdentityManager` logic and repositories.
- **Mocking:** Utilization of `MockK` for validating dependencies.
- **Coroutines:** Use of `kotlinx-coroutines-test` for flow and async emissions.

Testing is split across domain verification and UI-focused instrumentation. Run via `./gradlew test`.

## Privacy and Security Notes

Xonar is built to act natively inside the constraint boundaries of Android 12+ (API 34 compliant)
- Uses secure biometrics prompt abstractions which fallback appropriately to user credentials if strong biometrics are absent.
- Clears WebView active references intelligently during memory pressure and lifecycle callbacks.

## Extension-Ready Architecture

The domain scaffolding currently acts defensively. Abstractions such as `ReaderModeEngine`, `AdBlockerEngine`, and future URL interceptors lie within clean generic interfaces so adding 3rd party plugins down the line is seamless without re-architecting the web views framework.
