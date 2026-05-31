# Kurio

**Speak. Transcribe. Continue.**

Kurio is a premium offline speech-to-text application powered by local open-source speech recognition models. All transcription runs entirely on-device — no cloud, no accounts, no data leaves your device.

---

## Features

- **Offline Speech Transcription** — Whisper.cpp, Moonshine, and Vosk models run locally
- **Model Management** — Download, switch, and delete speech models on demand
- **Transcription History** — Stores your last 10 transcriptions with full search
- **Floating Overlay (Android)** — Access transcription from anywhere on your device
- **Premium Beige Design** — Minimal, elegant, Apple-like UI with dark mode
- **Fully Offline** — Internet only needed for model downloads and optional updates
- **Privacy First** — No accounts, no cloud processing, no audio uploads

---

## Screenshots

*(Screenshots placeholder)*

---

## Architecture

```
┌─────────────────────────────────────────────┐
│              Feature Modules                 │
│  Home  │  Transcribe  │  History  │  Models │
│  Settings  │  About                         │
├─────────────────────────────────────────────┤
│              Presentation Module            │
│  Navigation  │  Theme  │  Shared Components │
├─────────────────────────────────────────────┤
│                Domain Module                │
│  Repositories  │  Use Cases  │  Models      │
├─────────────────────────────────────────────┤
│                 Data Module                 │
│  Room DB  │  DataStore  │  Ktor  │  Engine  │
├─────────────────────────────────────────────┤
│            Platform Modules                 │
│  Android (Overlay, Permissions, Service)    │
│  iOS (Widgets, Shortcuts, Extensions)       │
└─────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin Multiplatform |
| UI | Compose Multiplatform |
| Architecture | Clean Architecture |
| DI | Koin |
| Database | Room |
| Preferences | DataStore |
| Networking | Ktor |
| Logging | Napier |
| Speech Engine | Whisper.cpp, Moonshine, Vosk |

---

## Project Structure

```
kurio/
├── core/                    # Design system, utilities, shared models
├── domain/                  # Business logic, use cases, repository interfaces
├── data/                    # Database, networking, repository implementations
├── presentation/            # Shared UI components, navigation, theming
├── feature-home/            # Home screen
├── feature-transcription/   # Transcription recording and results
├── feature-history/         # Transcription history management
├── feature-models/          # Model download and management
├── feature-settings/        # App settings and preferences
├── platform-android/        # Android-specific: overlay, permissions, notifications
├── platform-ios/            # iOS-specific: widgets, shortcuts
├── app-android/             # Android app entry point
└── app-ios/                 # iOS app entry point
```

---

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Gradle 8.7+
- Kotlin 2.0+

### Android Build

```bash
git clone https://github.com/dhanu/kurio.git
cd kurio
./gradlew :app-android:assembleDebug
```

### iOS Build

Open the project in Xcode via the generated framework:

```bash
./gradlew :app-ios:linkDebugFrameworkIosArm64
```

> Note: iOS support requires an Apple Silicon Mac with Xcode 15+.

---

## Model Downloads

Models are downloaded from within the app after installation. Available models:

| Model | Size | Language | Speed | Accuracy |
|-------|------|----------|-------|----------|
| Whisper Tiny English | ~39 MB | English | ★★★★★ | ★★★ |
| Whisper Tiny Multi | ~75 MB | Multi | ★★★★ | ★★★★ |
| Whisper Base English | ~74 MB | English | ★★★ | ★★★★ |
| Whisper Base Multi | ~145 MB | Multi | ★★ | ★★★★★ |
| Moonshine Tiny* | ~25 MB | English | ★★★★★ | ★★★ |
| Moonshine Base* | ~50 MB | English | ★★★★ | ★★★★ |
| Vosk Small English* | ~20 MB | English | ★★★★★ | ★★ |

*\*Experimental models*

---

## Build Configuration

Configure the app via `app-android/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        applicationId = "com.dhanu.kurio"
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

---

## License

See [LICENSE](LICENSE).

---

## Privacy

Kurio does not collect, store, or transmit your speech data. All transcription is processed locally on your device using on-device machine learning models. No internet connection is required for core functionality.
