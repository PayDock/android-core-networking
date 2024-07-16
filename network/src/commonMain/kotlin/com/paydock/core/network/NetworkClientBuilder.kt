package com.paydock.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.URLProtocol
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Abstract builder class for constructing an instance of [HttpClient] with various configurations.
 * This class provides methods for setting common network configurations such as base URL, SSL pins,
 * debug mode, request timeout, and response timeout.
 * Specific platform implementations should extend this class to add platform-specific configurations.
 */
abstract class NetworkClientBuilder {
    // Common network configuration properties
    protected var baseUrl: String? = null
    protected var sslPins: List<String> = listOf()
    protected var isDebug: Boolean = false
    protected var requestTimeout: Double = 60.0
    protected var responseTimeout: Double = 60.0
    protected var protocol: URLProtocol = URLProtocol.HTTPS

    // This is purely meant for test purposes
    protected var mockEngine: HttpClientEngine? = null

    /**
     * Sets the base URL for the network client.
     *
     * @param baseUrl The base URL to be set.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setBaseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }

    /**
     * Sets the SSL pins for the network client.
     *
     * @param sslPins The list of SSL pins to be set.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setSslPins(sslPins: List<String>) = apply { this.sslPins = sslPins }

    /**
     * Sets the debug mode for the network client.
     *
     * @param isDebug The debug mode flag.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setDebug(isDebug: Boolean) = apply { this.isDebug = isDebug }

    /**
     * Sets the request timeout for the network client.
     *
     * @param timeout The request timeout in seconds.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setRequestTimeout(timeout: Double) = apply { this.requestTimeout = timeout }

    /**
     * Sets the response timeout for the network client.
     *
     * @param timeout The response timeout in seconds.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setResponseTimeout(timeout: Double) = apply { this.responseTimeout = timeout }

    /**
     * Sets the protocol for the network client.
     *
     * @param protocol The protocol to be set (e.g., HTTP, HTTPS).
     * @return The current instance of NetworkClientBuilder.
     */
    fun setProtocol(protocol: URLProtocol) = apply { this.protocol = protocol }

    /**
     * Sets the HTTP client engine for the network client.
     *
     * @param engine The HTTP client engine to be set.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setMockEngine(engine: MockEngine): NetworkClientBuilder = apply { this.mockEngine = engine }

    /**
     * Builds and returns an instance of [HttpClient] with the configured settings.
     *
     * @return The configured [HttpClient] instance.
     * @throws IllegalArgumentException if the base URL is not set.
     */
    abstract fun build(): HttpClient

    companion object {

        /**
         * Creates an instance of the platform-specific [NetworkClientBuilder].
         *
         * @return A platform-specific implementation of [NetworkClientBuilder].
         */
        fun create(): NetworkClientBuilder = createNetworkClientBuilder()

        /**
         * Lazily initialized JSON serializer with configured options.
         */
        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json by lazy {
            Json {
                encodeDefaults = true
                explicitNulls = false
                prettyPrint = true
                isLenient = false
                ignoreUnknownKeys = true
                allowSpecialFloatingPointValues = true
                useArrayPolymorphism = false
            }
        }

        /**
         * Retrieves the configured JSON serializer instance.
         *
         * @return The configured [Json] instance.
         */
        fun getNetworkJson(): Json = json
    }
}

/**
 * Creates an instance of the platform-specific [NetworkClientBuilder].
 * This function is expected to be implemented separately for each platform (e.g., Android, iOS).
 *
 * @return A platform-specific implementation of [NetworkClientBuilder].
 */
expect fun createNetworkClientBuilder(): NetworkClientBuilder