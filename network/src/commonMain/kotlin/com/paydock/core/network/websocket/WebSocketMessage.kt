package com.paydock.core.network.websocket

/**
 * Sealed class representing WebSocket message types.
 */
sealed class WebSocketMessage {

    /**
     * Text message.
     *
     * @property content The text content of the message.
     */
    data class Text(val content: String) : WebSocketMessage()

    /**
     * Binary message.
     *
     * @property data The binary data of the message.
     */
    data class Binary(val data: ByteArray) : WebSocketMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Binary

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}
