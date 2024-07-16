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

## Installation

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

### Adding the library as a Dependency

1. Add the maven credentials for the Github package info to your settings.gradle.

   ```groovy
   dependencyResolutionManagement {
       maven {
           name = "GitHubPackages"
           url = uri("https://maven.pkg.github.com/Paydock/android-core-networking")
           credentials {
               username = "<username>"
               password = "<private_access_token>"
           }
       }
   }
   ```

2. Add the dependency to your app.

   ```kotlin
    dependencies {
        // Kotlin based network dependency (jar)
       implementation("com.paydock.core:network:<version>")
        // Android based network dependency (aar)
       implementation("com.paydock.core:network-android:<version>")
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
    .setSslPins(listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))
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



