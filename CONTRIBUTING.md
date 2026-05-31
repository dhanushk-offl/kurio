# Contributing

Contributions to Kurio are welcome. Please follow these guidelines.

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/dhanu/kurio.git
   cd kurio
   ```

2. Open in Android Studio with KMP plugin installed

3. Sync Gradle and verify the project builds:
   ```bash
   ./gradlew build
   ```

## Code Style

- Follow Kotlin Coding Conventions
- Use 4-space indentation
- Maximum line length: 120 characters
- No wildcard imports
- Use explicit visibility modifiers
- Prefer `val` over `var`
- Follow Clean Architecture layer separation

## Architecture Rules

- **Core**: No dependencies on other modules
- **Domain**: Depends only on `:core`
- **Data**: Depends on `:core` and `:domain`
- **Presentation**: Depends on `:core` and `:domain`
- **Features**: Depend on `:core`, `:domain`, and `:presentation`

## Commit Messages

Follow conventional commits:

```
feat: add Whisper Base model support
fix: correct audio buffer overflow
chore: update Room to 2.7.0
docs: add model download documentation
```

## Pull Request Process

1. Create a feature branch from `main`
2. Write tests for new functionality
3. Ensure all existing tests pass
4. Update documentation if needed
5. Submit PR with clear description

## Testing

```bash
# Run all tests
./gradlew allTests

# Run Android-specific tests
./gradlew :app-android:testDebugUnitTest
```

## Adding a New Model

1. Add model definition in `ModelRepositoryImpl.getDefaultModels()`
2. Add registry entry in data layer
3. Add download URL and checksum
4. Test download and transcription flow

## Localization

All user-facing strings must be defined in resource files:

- Android: `res/values/strings.xml`
- Strings should not be hardcoded in composables

## License

By contributing, you agree that your contributions will be licensed under the project's MIT license.
