# Changelog

## [1.5.0] - 2025-02-05

### Added
- **WebSocket Support**: Full-featured WebSocket client with auto-reconnect capabilities
  - Cross-platform WebSocket support for Android and iOS
  - Flow-based message streaming and state monitoring
  - Configurable ping/pong handling and timeouts
  - Automatic reconnection with customizable retry logic
  - Comprehensive WebSocket exception handling with specific error types
- **Enhanced Error Handling**: New WebSocketException hierarchy for better error management
- **Swift Package Manager Support**: Complete migration from CocoaPods to SPM for iOS distribution
  - XCFramework generation and packaging
  - GitHub-based SPM distribution
  - Improved iOS integration workflow

### Changed
- **Breaking Change - iOS**: Import statement changed from `import network` to `import PaydockNetworking`
- **Platform Updates**: Updated iOS deployment target from 16.0 → 18.0
- **Build System**: Updated Android compileSdk from 34 → 36 (minSdk remains 24)
- **Dependency Management**: Improved API vs implementation dependency separation
- **Documentation**: Enhanced README with comprehensive WebSocket usage examples

### Removed
- **CocoaPods Support**: Removed CocoaPods configuration in favor of Swift Package Manager

### Migration Guide
- **iOS Projects**: Update import statements from `import network` to `import PaydockNetworking`
- **iOS Package Manager**: Switch from CocoaPods to Swift Package Manager using GitHub repository
- **Existing HTTP Clients**: No changes required - all existing functionality remains compatible

## [1.4.0] - 2025-09-25

### Changed
- **Major Dependency Updates:**
  - Updated Kotlin: 2.0.21 → 2.2.20
  - Updated Ktor: 2.3.13 → 3.3.0 (major version upgrade)
  - Updated OkHttp: 4.12.0 → 5.1.0 (major version upgrade)
  - Updated Android Gradle Plugin: 8.5.2 → 8.13.0
  - Updated KotlinX Coroutines: 1.9.0 → 1.10.2
  - Updated Detekt: 1.23.7 → 1.23.8
  - Updated Mockk: 1.13.16 → 1.14.5
  - Updated SLF4J: 2.0.16 → 2.0.17
  - Updated JReleaser: 1.19.0 → 1.20.0

- **Platform Updates:**
  - Updated iOS deployment target: 16.0 → 18.0
  - Updated Android compileSdk: 34 → 36 (minSdk remains 24)

- **Testing Dependencies:**
  - Updated Ktor testing dependencies to expose to integrating apps
  - Added Kover test coverage plugin (0.9.2)

### Fixed
- Updated rexml version to address security warnings
- Cleaned up deprecated exceptions

### Added
- Enhanced Maven Central publishing pipeline
- Improved code quality and testing pipelines
- Updated unit test reporting to GitLab

## [1.3.0] - 2025-03-25

### Added
- `HttpRequestRetry` configuration to Http Engines
- Configuration for retry logic ie. `maxRetries` & `retryInterval`
- Additional error details message list mappings
- Custom serialization for `ErrorMessage` types

### Changed
- Renamed `UnknownApiException` to `ApiParseException`
- Error details to use generic type `ErrorMessage` for `messages`

## [1.2.0] - 2025-02-21

### Changed
- Updated gradle and dependency versions

## [1.1.0] - 2024-10-20

### Changed
- SSL Pinning manager to be optional

## [1.0.1] - 2024-07-30

### Added

- Additional pipeline stages (release automation)

## [1.0.0] - 2024-07-16

### Added

- Initial KMP Network Library release
- Builder pattern to create Http Client
- iOS and Android Http Clients