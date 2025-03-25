package com.paydock.core.network.dto.error

import com.paydock.core.network.utils.ErrorMessageSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an error message that can be either a simple string or a more complex
 * structure containing gateway-specific details. This sealed class allows for
 * different types of error messages to be handled uniformly.
 *
 * It is annotated with `@Serializable` using a custom serializer `ErrorMessageSerializer`,
 * which means instances of this class can be easily serialized and deserialized.
 */
@Serializable(with = ErrorMessageSerializer::class)
sealed class ErrorMessage {
    /**
     * Represents a simple error message containing a string.
     *
     * This class is used to encapsulate an error message as a string. It is designed to be
     * easily serialized and deserialized, making it suitable for sending error information
     * across different systems or storing it in various data formats.
     *
     * @property message The string message representing the error.
     *
     * @constructor Creates a new StringMessage instance.
     * @param message The string message to be stored.
     */
    @Serializable
    data class StringMessage(val message: String) : ErrorMessage()

    /**
     * Represents an error that occurred during communication with a payment gateway.
     *
     * This class encapsulates various details about the error, including gateway-specific information
     * and general status codes and descriptions. It extends the `ErrorMessage` class,
     * suggesting it's part of a broader error handling mechanism.
     *
     * @property gatewaySpecificCode A code specific to the payment gateway, if available. This can be
     *                               useful for debugging or mapping to more specific error conditions.
     *                               May be null if the gateway did not provide a specific code.
     * @property gatewaySpecificDescription A human-readable description of the error as provided by the
     *                                     payment gateway. This can be helpful for understanding the
     *                                     nature of the error from the gateway's perspective. May be null
     *                                     if the gateway did not provide a specific description.
     * @property description A general description of the error. This may be a simplified or
     *                       standardized description provided by the system handling the gateway
     *                       communication. May be null if no general description is applicable.
     * @property statusCode A general status code indicating the type of error. This can be used for
     *                      categorizing errors and triggering specific error handling logic.
     *                       May be null if the system didn't assign a status code.
     * @property statusCodeDescription A human-readable description of the general status code. This
     *                                provides more context for the `statusCode`. May be null if no
     *                                description of the status code is available.
     */
    @Serializable
    data class GatewayError(
        @SerialName("gateway_specific_code") val gatewaySpecificCode: String? = null,
        @SerialName("gateway_specific_description") val gatewaySpecificDescription: String? = null,
        val description: String? = null,
        @SerialName("status_code") val statusCode: String? = null,
        @SerialName("status_code_description") val statusCodeDescription: String? = null
    ) : ErrorMessage()
}