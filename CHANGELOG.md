# Changelog

## [1.0.0] - 2026-05-31

### Added
- Initial release of Kurio
- Offline speech transcription using Whisper.cpp
- Model download and management system
- Transcription history with search functionality
- Android floating overlay assistant
- Premium beige minimal design with dark mode
- Settings with auto-copy, haptic feedback, and analytics toggle
- Storage management and cache clearing
- Remote update checking system
- Privacy-first architecture with no cloud dependency

### Models
- Whisper Tiny English (~39 MB)
- Whisper Tiny Multilingual (~75 MB)
- Whisper Base English (~74 MB)
- Whisper Base Multilingual (~145 MB)
- Moonshine Tiny (Experimental, ~25 MB)
- Moonshine Base (Experimental, ~50 MB)
- Vosk Small English (Experimental, ~20 MB)

### Known Issues
- iOS platform implementation requires Xcode project setup
- Native Whisper.cpp library requires device-specific compilation
- Moonshine and Vosk models are experimental pending native integration
