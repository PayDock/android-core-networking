@file:Suppress("MatchingDeclarationName")
package com.paydock.core.network

import com.paydock.core.network.interceptor.ApiErrorInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Concrete builder class for constructing an instance of [HttpClient] on Android with various configurations,
 * including the ability to add custom interceptors.
 */
internal class AndroidNetworkClientBuilder : NetworkClientBuilder() {

    // List to hold custom interceptors to be added to the HTTP client
    private val interceptors = mutableListOf<Interceptor>()

    /**
     * Adds interceptors to the network client.
     *
     * @param interceptors Vararg of interceptors to be added.
     * @return The current instance of NetworkClientBuilder.
     */
    internal fun addInterceptor(vararg interceptors: Interceptor) = apply { this.interceptors.addAll(interceptors) }

    /**
     * Builds and returns an instance of [HttpClient] with the configured settings.
     * Configures the OkHttp engine with the specified interceptors and other settings.
     *
     * @return The configured [HttpClient] instance.
     * @throws IllegalArgumentException if the base URL is not set.
     */
    override fun build(): HttpClient {
        requireNotNull(baseUrl) { "Base URL must be set" }
        val config = NetworkConfig(baseUrl!!, sslPins, isDebug, protocol, requestTimeout, responseTimeout)

        val engine = mockEngine ?: createHttpEngine(config)
        return HttpClient(engine) {
            expectSuccess = true
            // Configure the BASE_URL and default headers using the defaultRequest builder
            defaultRequest {
                url {
                    protocol = config.protocol
                    host = config.baseUrl
                    contentType(ContentType.Application.Json)
                }
            }
            install(ContentNegotiation) {
                json(getNetworkJson())
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = if (config.isDebug) LogLevel.ALL else LogLevel.NONE
            }
        }
    }

    /**
     * Creates and configures the OkHttp engine for the network client.
     *
     * @param config The network configuration settings.
     * @return The configured [HttpClientEngine] instance.
     */
    private fun createHttpEngine(config: NetworkConfig): HttpClientEngine {
        val certificatePinner = CertificatePinner.Builder().apply {
            config.sslPins.forEach { pin ->
                add(config.baseUrl, pin)
            }
        }.build()
        return OkHttp.create {
            config {
                connectTimeout(config.requestTimeout.toLong(), TimeUnit.SECONDS)
                readTimeout(config.responseTimeout.toLong(), TimeUnit.SECONDS)
                writeTimeout(config.responseTimeout.toLong(), TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
                certificatePinner(certificatePinner)
                hostnameVerifier { hostname, _ -> hostname == config.baseUrl }
                addInterceptor(ApiErrorInterceptor())
                interceptors.forEach { addInterceptor(it) }
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = if (config.isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                    }
                )
            }
        }
    }
}

/**
 * Creates an instance of the Android-specific [NetworkClientBuilder].
 *
 * @return An instance of [AndroidNetworkClientBuilder].
 */
actual fun createNetworkClientBuilder(): NetworkClientBuilder = AndroidNetworkClientBuilder()

/**
 * Extension function to add an interceptor to a [NetworkClientBuilder].
 * If the builder is an instance of [AndroidNetworkClientBuilder], the interceptor is added to it.
 *
 * @param interceptor The interceptor to be added.
 * @return The current instance of [NetworkClientBuilder].
 */
fun NetworkClientBuilder.addInterceptor(interceptor: Interceptor): NetworkClientBuilder {
    if (this is AndroidNetworkClientBuilder) {
        this.addInterceptor(interceptor)
    }
    return this
}