<!-- Build & Quality -->
[![Pipeline](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-lib-networking-android/badges/main/pipeline.svg)](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-lib-networking-android/-/pipelines?ref=main)
[![Coverage](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-lib-networking-android/badges/main/coverage.svg?job=test_Android)](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-lib-networking-android/-/pipelines?ref=main)

<!-- Distribution & Release -->
[![Maven Central](https://img.shields.io/maven-central/v/com.paydock.core/network.svg)](https://central.sonatype.com/artifact/com.paydock.core/network)
[![GitHub release](https://img.shields.io/github/v/release/PayDock/android-core-networking.svg?label=GitHub%20release)](https://github.com/PayDock/android-core-networking/releases)

<!-- Platform Support -->
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-KMP-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Android](https://img.shields.io/badge/Android-Supported-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![iOS](https://img.shields.io/badge/iOS-Supported-000000?logo=apple&logoColor=white)](https://developer.apple.com/)

<!-- Technical Specifications -->
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![minSdk](https://img.shields.io/badge/minSdk-24-blue)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![compileSdk](https://img.shields.io/badge/compileSdk-34-3DDC84)](https://developer.android.com/studio/releases/platforms)
[![iOS Target](https://img.shields.io/badge/iOS%20target-18.0-000000)](https://kotlinlang.org/docs/native-cocoapods.html)

# Network Module for Kotlin Multiplatform (KMP)

A versatile and robust networking module for Kotlin Multiplatform (KMP) projects, utilizing Ktor with platform-specific engines. This module supports dynamic HTTP client engine creation, including success and failure mock engines, interceptor-based OkHttp for Android, and SSL pinning configurations. It ensures seamless integration and consistent networking functionality across Android and iOS, making it ideal for both production and testing environments.

## Features

- **Cross-Platform Support**: Compatible with both Android and iOS.
- **Ktor Integration**: Utilizes Ktor for HTTP client functionality.
- **Platform-Specific Engines**: OkHttp for Android and Darwin for iOS.
- **SSL Pinning**: Ensures secure connections with configurable SSL pins.
- **Custom Interceptors**: Easily add custom interceptors for Android.
- **Flexible Configuration**: Dynamic HTTP client engine creation.
- **Production and Testing**: Suitable for both production and testing environments.

## Compatibility

**Current Version (1.4.0+):**
- Kotlin: 2.2.20
- Android Gradle Plugin: 8.13.0
- Gradle: 8.14.3
- Android: minSdk 24, compileSdk 36
- iOS: deployment target 18.0

> 💡 **For compatibility with previous versions**, see the [CHANGELOG.md](CHANGELOG.md) for version-specific requirements. We recommend staying within 1-2 minor versions of the latest release for optimal compatibility and security updates.

## Installation

### Maven Central (Recommended)

1. Ensure `mavenCentral()` is in your repositories (usually default):

```kotlin
repositories {
    mavenCentral()
}
```

2. Add the dependency:

- Kotlin Multiplatform shared module:

```kotlin
dependencies {
    implementation("com.paydock.core:network:<version>")
}
```

- Android-only app/module:

```kotlin
dependencies {
    implementation("com.paydock.core:network-android:<version>")
}
```

Latest versions:
- Network (multiplatform): https://central.sonatype.com/artifact/com.paydock.core/network
- Network (Android artifact): https://central.sonatype.com/artifact/com.paydock.core/network-android

Note: No ProGuard/R8 rules are required for this library.

### Adding the Module as a Submodule

1. Add the submodule to your repository:

   ```sh
   git submodule add https://github.com/PayDock/android-core-networking.git
   ```
2. Include the submodule in your project's `settings.gradle.kts`:

   ```groovy
   include(":network")
   project(":network").projectDir = file("path/to/network")
   ```
   
3. Add the network module as a dependency in your module's build.gradle.kts:

   ```kotlin
   dependencies {
       implementation(project(":network"))
   }
   ```

## Usage

### Building an HTTP Client

Use the `NetworkClientBuilder` to build an instance of `HttpClient`:

```kotlin
val httpClient = NetworkClientBuilder.create()
    .setBaseUrl("https://example.com")
    .setSslPins(listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))
    .setDebug(BuildConfig.DEBUG)
    .build()
```

For Android-specific customizations, you can cast the builder to `AndroidNetworkClientBuilder`:

```kotlin
val httpClient = NetworkClientBuilder.create()
    .setBaseUrl("example.com")
    .setSslPins(listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")) // optional
    .setDebug(BuildConfig.DEBUG)
    // Android OKHttp Interceptor
   .addInterceptor(CustomInterceptor())
   .build()
```

### Making Network Requests

Once you have an instance of `HttpClient`, you can make network requests as usual with Ktor:

```kotlin
runBlocking {
    val response: HttpResponse = httpClient.get("endpoint")
    println(response.status)
}
```


### Android: Add authentication header

```kotlin
import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.addInterceptor
import com.paydock.core.network.interceptor.AuthInterceptor

val httpClient = NetworkClientBuilder.create()
    .setBaseUrl("api.paydock.com")
    .addInterceptor(AuthInterceptor("<public_key>"))
    .build()
```

### Error handling

`HttpClient` is configured with `expectSuccess = true` and maps API errors to `ApiException`.

```kotlin
import com.paydock.core.network.exceptions.ApiException
import io.ktor.client.request.get

try {
    val response = httpClient.get("/v1/example")
    // handle success
} catch (e: ApiException) {
    // Displayable message derived from server error
    val userMessage = e.message
    // Or access full error payload
    val error = e.error
}
```

### Timeouts and retries

```kotlin
val httpClient = NetworkClientBuilder.create()
    .setBaseUrl("api.paydock.com")
    .setRequestTimeout(30.0)
    .setResponseTimeout(30.0)
    .setMaxRetries(2)
    .setRetryInterval(2_000)
    .build()
```

### Testing with a mock engine

```kotlin
import com.paydock.core.network.NetworkClientBuilder
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

val mockEngine = MockEngine { _ ->
    respond(
        content = """{"type":"ok","data":{}}""",
        status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", "application/json")
    )
}

val httpClient = NetworkClientBuilder.create()
    .setBaseUrl("example.com")
    .setMockEngine(mockEngine)
    .build()
```

## Changelog

See `CHANGELOG.md` for release notes and migration details.

 
