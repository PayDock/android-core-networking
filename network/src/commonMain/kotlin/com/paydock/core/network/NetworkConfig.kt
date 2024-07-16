package com.paydock.core.network

import io.ktor.http.URLProtocol

/**
 * Data class representing the configuration for the network module.
 *
 * @property baseUrl The base URL for the network requests.
 * @property sslPins A list of SSL pinning hashes to ensure secure connections.
 * @property isDebug A flag indicating whether the application is in debug mode.
 *                   When true, additional debug information may be logged.
 * @property protocol The URL protocol to be used (e.g., HTTP, HTTPS).
 * @property requestTimeout The timeout duration for network requests in seconds.
 * @property responseTimeout The timeout duration for network responses in seconds.
 */
internal data class NetworkConfig(
    val baseUrl: String,
    val sslPins: List<String>,
    val isDebug: Boolean,
    val protocol: URLProtocol,
    val requestTimeout: Double,
    val responseTimeout: Double
)
