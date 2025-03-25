package com.paydock.core.network.utils

import com.paydock.core.network.dto.error.ErrorMessage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom serializer for a list of `ErrorMessage` objects.
 *
 * This serializer handles the deserialization of a list of `ErrorMessage` objects from JSON.
 * It supports various formats:
 *   - A single `JsonObject` representing a `GatewayError`.
 *   - A single `JsonPrimitive` (string) representing a `StringMessage`.
 *   - A `JsonArray` containing a mix of `JsonObject` (GatewayError) and `JsonPrimitive`(StringMessage).
 *   - An empty or invalid `JsonArray`, in which case it returns an empty list.
 *
 * Serialization is not implemented, as this serializer is primarily intended for deserializing
 * error responses from an external source.
 *
 * @see ErrorMessage
 * @see ErrorMessageSerializer
 */
object ErrorMessageListSerializer : KSerializer<List<ErrorMessage>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ErrorMessageList")

    override fun serialize(encoder: Encoder, value: List<ErrorMessage>) {
        // Not needed for this scenario, as we only need custom deserialization.
        throw NotImplementedError("Serialization is not required for ErrorMessageListSerializer")
    }

    override fun deserialize(decoder: Decoder): List<ErrorMessage> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This decoder is not JsonDecoder")
        val jsonElement = jsonDecoder.decodeJsonElement()

        return when {
            jsonElement is JsonObject -> {
                // Handle single object (GatewayError)
                listOf(jsonDecoder.json.decodeFromJsonElement(ErrorMessage.GatewayError.serializer(), jsonElement))
            }

            jsonElement is JsonPrimitive -> {
                // Handle single string (StringMessage)
                listOf(ErrorMessage.StringMessage(jsonElement.jsonPrimitive.content))
            }

            jsonElement.jsonArray.isNotEmpty() -> {
                // Handle list of elements (mixed or only strings)
                jsonDecoder.json.decodeFromJsonElement(ListSerializer(ErrorMessageSerializer), jsonElement)
            }

            else -> emptyList() // Return an empty list if messages array is empty or invalid
        }
    }
}