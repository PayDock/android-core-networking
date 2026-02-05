# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Network Library ProGuard Rules

# Keep all public classes and methods in the network module
-keep public class com.paydock.core.network.** { public *; }

# Keep WebSocket classes and methods
-keep class com.paydock.core.network.websocket.** { *; }

# Keep exception classes for proper error handling
-keep class com.paydock.core.network.exceptions.** { *; }

# Keep DTOs and serialization classes
-keep class com.paydock.core.network.dto.** { *; }

# Ktor client rules
-keep class io.ktor.client.** { *; }
-keep class io.ktor.http.** { *; }
-keep class io.ktor.util.** { *; }
-keepattributes *Annotation*, InnerClasses, Signature, Exception

# OkHttp rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Coroutines rules
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Serialization rules  
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.paydock.core.network.**$$serializer { *; }
-keepclassmembers class com.paydock.core.network.** {
    *** Companion;
}
-keepclasseswithmembers class com.paydock.core.network.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# SSL and networking rules
-keep class javax.net.ssl.** { *; }
-keep class java.security.** { *; }
-dontwarn javax.net.ssl.**