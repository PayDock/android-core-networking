package com.paydock.core.network

import io.ktor.http.URLProtocol

/**
 * Configuration class for network requests.
 *
 * This class holds all the necessary settings to configure the network client, including base URL,
 * SSL pinning, debug mode, protocol, timeouts, retry mechanism.
 *
 * @property baseUrl The base URL for all network requests. This should be a valid URL without trailing slash.
 *                   Example: "https://api.example.com"
 * @property sslPins A list of SHA-256 hashes of the expected certificate's public key. Used for SSL pinning.
 *                   If null, SSL pinning is disabled.
 *                   Example: `listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
 *                   "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")`.
 * @property protocol The URL protocol to use (e.g., HTTP or HTTPS).
 * @property requestTimeout The timeout duration (in seconds) for establishing a connection.
 *                         If the connection cannot be established within this time, a timeout error occurs.
 *                         Should be a positive value.
 * @property responseTimeout The timeout duration (in seconds) for receiving a response from the server.
 *                          If the entire response is not received within this time, a timeout error occurs.
 *                          Should be a positive value.
 * @property maxRetries The maximum number of times a request should be retried in case of failure.
 *                      Should be a non-negative value.
 * @property retryInterval The time (in milliseconds) to wait before retrying a failed request.
 *                         Should be a positive value.
 * @property isDebug  Indicates whether the application is in debug mode.
 *                   This might be used to enable more verbose logging or different configurations.
 */
internal data class NetworkConfig(
    val baseUrl: String,
    val sslPins: List<String>?,
    val protocol: URLProtocol,
    val requestTimeout: Double,
    val responseTimeout: Double,
    val maxRetries: Int,
    val retryInterval: Long,
    val isDebug: Boolean,
)
