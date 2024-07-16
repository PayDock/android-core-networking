@file:Suppress("MatchingDeclarationName")
package com.paydock.core.network

import com.paydock.core.network.dto.error.toApiError
import com.paydock.core.network.exceptions.UnknownApiException
import com.paydock.core.network.extensions.convertToApiErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json

/**
 * Concrete builder class for constructing an instance of [HttpClient] on iOS with various configurations.
 */
internal class IOSNetworkClientBuilder : NetworkClientBuilder() {

    /**
     * Builds and returns an instance of [HttpClient] with the configured settings.
     * Configures the Darwin engine with the specified settings.
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
            engine { createHttpEngine(config) }
            HttpResponseValidator {
                validateResponse { response ->
                    // Check if the response is successful
                    if (response.status.isSuccess()) {
                        return@validateResponse
                    }
                    // Extract error body from the response
                    val errorBody: String? = response.body<Error?>()?.toString()
                    // If the error body is present, attempt to convert to specific error response structures
                    if (!errorBody.isNullOrBlank()) {
                        // Depending on the error structure, convert to appropriate data class
                        val apiErrorResponse = convertToApiErrorResponse(errorBody)
                        // Process the error data class accordingly and throw the appropriate ApiException
                        throw when {
                            apiErrorResponse != null -> apiErrorResponse.toApiError()
                            else -> UnknownApiException(status = response.status.value, errorBody = errorBody)
                        }
                    } else {
                        throw UnknownApiException(status = response.status.value, errorBody = errorBody)
                    }
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
     * Creates and configures the Darwin engine for the network client.
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
        return Darwin.create {
            handleChallenge(certificatePinner)
            configureSession {
                timeoutIntervalForRequest = config.requestTimeout
                timeoutIntervalForResource = config.responseTimeout
            }
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}

/**
 * Creates an instance of the iOS-specific [NetworkClientBuilder].
 *
 * @return An instance of [IOSNetworkClientBuilder].
 */
actual fun createNetworkClientBuilder(): NetworkClientBuilder = IOSNetworkClientBuilder()