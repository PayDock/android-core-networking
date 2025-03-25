package com.paydock.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * An abstract builder class for creating and configuring [HttpClient] instances.
 *
 * This class provides a fluent interface for setting various options related to network requests,
 * such as base URL, SSL pins, debug mode, timeouts, retry mechanisms, and more.
 * It also allows for the configuration of a mock engine for testing purposes.
 *
 * The [NetworkClientBuilder] is designed to be extended by platform-specific implementations
 * (e.g., Android, iOS) that will provide the concrete logic for building the [HttpClient] instance.
 *
 * @property baseUrl The base URL for the network client. Defaults to null.
 * @property sslPins The list of SSL pins for the network client. Defaults to an empty list.
 * @property isDebug The debug mode flag for the network client. Defaults to false.
 * @property protocol The URL protocol to use (e.g., HTTP, HTTPS). Defaults to HTTPS.
 * @property requestTimeout The request timeout in seconds. Defaults to 60.0.
 * @property responseTimeout The response timeout in seconds. Defaults to 60.0.
 * @property maxRetries The maximum number of retry attempts for an operation. Defaults to 0.
 * @property retryInterval The retry interval in milliseconds. Defaults to 3000L.
 * @property mockEngine The mock HTTP client engine to use for testing. Defaults to null.
 */
abstract class NetworkClientBuilder(
    protected var baseUrl: String? = null,
    protected var sslPins: List<String> = emptyList(),
    protected var isDebug: Boolean = false,
    protected var protocol: URLProtocol = URLProtocol.HTTPS,
    protected var requestTimeout: Double = 60.0,
    protected var responseTimeout: Double = 60.0,
    protected var maxRetries: Int = 0,
    protected var retryInterval: Long = 3000L,
    protected var mockEngine: HttpClientEngine? = null
) {
    /**
     * Sets the base URL for the network client.
     *
     * @param baseUrl The base URL to be set.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setBaseUrl(baseUrl: String) = apply {
        require(runCatching { Url(baseUrl) }.isSuccess) {
            "Invalid base URL: $baseUrl"
        }
        this.baseUrl = baseUrl
    }

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
    fun setRequestTimeout(timeout: Double) = apply {
        require(timeout >= 0) { "request timeout must be non-negative" }
        this.requestTimeout = timeout
    }

    /**
     * Sets the response timeout for the network client.
     *
     * @param timeout The response timeout in seconds.
     * @return The current instance of NetworkClientBuilder.
     */
    fun setResponseTimeout(timeout: Double) = apply {
        require(timeout >= 0) { "response timeout must be non-negative" }
        this.responseTimeout = timeout
    }

    /**
     * Sets the protocol for the network client.
     *
     * @param protocol The protocol to be set (e.g., HTTP, HTTPS).
     * @return The current instance of NetworkClientBuilder.
     */
    fun setProtocol(protocol: URLProtocol) = apply { this.protocol = protocol }

    /**
     * Sets the maximum number of retry attempts for an operation.
     *
     * This function configures the maximum number of times an operation will be retried in case of failure.
     * A value of 0 means no retries will be attempted. A value of 1 means one retry will be attempted,
     * for a total of two attempts (the initial attempt and one retry).
     *
     * @param maxRetries The maximum number of retries to attempt. Must be a non-negative integer.
     * @return This object, allowing for method chaining.
     * @throws IllegalArgumentException if `maxRetries` is negative.
     */
    fun setMaxRetries(maxRetries: Int) = apply {
        require(maxRetries >= 0) { "maxRetries must be non-negative" }
        this.maxRetries = maxRetries
    }

    /**
     * Sets the retry interval for the operation.
     *
     * This method allows you to specify the time (in milliseconds) to wait before
     * attempting to retry the operation after a failure.
     *
     * @param retryInterval The retry interval in milliseconds. Must be a non-negative value.
     * @return This object, allowing for method chaining.
     * @throws IllegalArgumentException if the provided retryInterval is negative.
     */
    fun setRetryInterval(retryInterval: Long) = apply {
        require(retryInterval >= 0) { "retryInterval must be non-negative" }
        this.retryInterval = retryInterval
    }

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