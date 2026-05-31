# Architecture

Kurio follows **Clean Architecture** with **Kotlin Multiplatform** to share business logic across Android and iOS.

---

## Layer Overview

### Core (`:core`)

The foundation layer containing:
- **Design System**: Colors, typography, shapes, theme
- **Shared Models**: Transcription results, speech models, settings, DTOs
- **Utilities**: Time formatting, storage formatting, ID generation
- **Extensions**: Common Kotlin extensions

**Dependencies**: None (pure Kotlin)

### Domain (`:domain`)

The business logic layer containing:
- **Repository Interfaces**: Contracts for data operations
- **Use Cases**: Single-responsibility business operations
- **Domain Models**: Core business entities

**Dependencies**: `:core`

### Data (`:data`)

The data layer containing:
- **Room Database**: Local persistence with entities and DAOs
- **DataStore**: Preferences and settings storage
- **Ktor Client**: Remote API communication
- **Repository Implementations**: Concrete implementations of domain interfaces
- **Speech Engine**: Whisper.cpp integration via JNI/cinterop

**Dependencies**: `:core`, `:domain`

### Presentation (`:presentation`)

Shared UI layer:
- **Navigation**: Screen definitions and navigation graph
- **Theme**: Theme access helpers
- **Components**: Reusable UI components (buttons, cards, dialogs, text fields)

**Dependencies**: `:core`, `:domain`

### Feature Modules (`:feature-*`)

Feature-specific modules, each containing:
- **ViewModel**: State management and business logic coordination
- **Screen**: Composable UI
- **DI**: Koin module registration

Features:
- `:feature-home` — Dashboard with quick actions, stats, storage info
- `:feature-transcription` — Recording, processing, results display
- `:feature-history` — History list with search and management
- `:feature-models` — Model registry, download management
- `:feature-settings` — Preferences, storage, updates

**Dependencies**: `:core`, `:domain`, `:presentation`

### Platform Modules

Platform-specific implementations:
- `:platform-android` — Overlay service, permissions, notifications
- `:platform-ios` — Widget stubs, app delegate

### App Modules

Application entry points:
- `:app-android` — Android Application, MainActivity, DI setup
- `:app-ios` — iOS app entry point

---

## Data Flow

```
UI (Compose) → ViewModel → UseCase → Repository (interface)
                                          ↓
                              Repository Implementation
                                          ↓
                              ┌─────────────────────┐
                              │  Room DB  │  Ktor    │
                              │  DataStore│  Engine  │
                              └─────────────────────┘
```

1. UI observes `StateFlow` from ViewModel
2. ViewModel executes Use Cases
3. Use Cases call Repository interfaces
4. Repository implementations delegate to local DB, remote API, or speech engine

---

## Dependency Injection

Koin is used for DI across all modules:

```kotlin
// Module registration
startKoin {
    modules(coreModule, domainModule, dataModule, ...)
}

// Injection in composables
val viewModel: HomeViewModel = koinInject()
```

---

## Key Architectural Decisions

1. **Offline-First**: All core functionality works without internet
2. **Repository Pattern**: Abstracts data sources behind interfaces
3. **Unidirectional Data Flow**: UI → Event → State → UI
4. **Feature Modularization**: Each feature is an independent module
5. **Platform Abstraction**: Platform-specific code lives in `:platform-*`

---

## Performance Targets

| Operation | Target |
|-----------|--------|
| Cold Start | <2s |
| Recording Start | <500ms |
| History Open | <500ms |
| Settings Open | <300ms |
| Memory Usage | <300MB |
