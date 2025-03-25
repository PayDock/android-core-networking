# Changelog

## [1.3.0] - 2025-03-25

# Added
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