package com.paydock.core.network.utils

import com.paydock.core.network.dto.error.ErrorMessage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * `ErrorMessageSerializer` is a custom [KSerializer] for the [ErrorMessage] sealed class.
 *
 * It handles the deserialization of [ErrorMessage] from JSON, supporting two possible structures:
 *
 * 1. **JsonObject (GatewayError):**  If the incoming JSON is a [JsonObject], it's assumed to represent
 * a [ErrorMessage.GatewayError].
 *    It delegates the deserialization to the [ErrorMessage.GatewayError.serializer()].
 *
 * 2. **JsonPrimitive (StringMessage):** If the incoming JSON is a [JsonPrimitive], it's assumed to
 * represent a simple string message [ErrorMessage.StringMessage].
 *    The content of the [JsonPrimitive] is extracted as the message string.
 *
 * **Serialization is not supported:** Attempting to serialize an [ErrorMessage] using this serializer
 * will result in a [NotImplementedError].
 *
 * **Usage:**
 * This serializer is typically used in conjunction with kotlinx.serialization to automatically handle
 * the deserialization of [ErrorMessage] instances
 * from JSON responses. It provides flexibility to handle different error formats.
 *
 * **Example Scenarios:**
 *
 * ```json
 * // Example of a GatewayError:
 * {
 *   "code": 500,
 *   "message": "Internal Server Error",
 *   "details": "Database connection failed"
 * }
 *
 * // Example of a StringMessage:
 * "Invalid input provided"
 * ```
 *
 * **Error Handling:**
 * - It throws a [SerializationException] if the decoder is not a [JsonDecoder].
 * - It throws a [SerializationException] if the decoded JSON element is neither a [JsonObject] nor a [JsonPrimitive].
 */
object ErrorMessageSerializer : KSerializer<ErrorMessage> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ErrorMessage")

    override fun serialize(encoder: Encoder, value: ErrorMessage) {
        // Not needed for this scenario, as we only need custom deserialization.
        throw NotImplementedError("Serialization is not required for ErrorMessageSerializer")
    }

    override fun deserialize(decoder: Decoder): ErrorMessage {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This decoder is not JsonDecoder")
        return when (val jsonElement = jsonDecoder.decodeJsonElement()) {
            is JsonObject -> {
                jsonDecoder.json.decodeFromJsonElement(
                    ErrorMessage.GatewayError.serializer(),
                    jsonElement
                )
            }

            is JsonPrimitive -> {
                ErrorMessage.StringMessage(jsonElement.jsonPrimitive.content)
            }

            else -> {
                throw SerializationException("Unexpected JSON element type for ErrorMessage: $jsonElement")
            }
        }
    }
}