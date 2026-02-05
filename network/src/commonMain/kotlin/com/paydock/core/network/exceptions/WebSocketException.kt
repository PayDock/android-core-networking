package com.paydock.core.network.exceptions

/**
 * Base exception for WebSocket-related errors.
 *
 * @property message The error message describing what went wrong.
 * @property cause The underlying cause of the exception, if any.
 */
sealed class WebSocketException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Exception thrown when WebSocket connection fails.
     *
     * @property url The WebSocket URL that failed to connect.
     * @property message The error message.
     * @property cause The underlying cause.
     */
    class ConnectionFailedException(
        val url: String,
        message: String = "Failed to connect to WebSocket: $url",
        cause: Throwable? = null
    ) : WebSocketException(message, cause)

    /**
     * Exception thrown when WebSocket connection is closed unexpectedly.
     *
     * @property code The close code.
     * @property reason The close reason.
     */
    class ConnectionClosedException(
        val code: Short,
        val reason: String,
        message: String = "WebSocket connection closed: [$code] $reason"
    ) : WebSocketException(message)

    /**
     * Exception thrown when sending a message fails.
     *
     * @property message The error message.
     * @property cause The underlying cause.
     */
    class SendFailedException(
        message: String = "Failed to send WebSocket message",
        cause: Throwable? = null
    ) : WebSocketException(message, cause)

    /**
     * Exception thrown when receiving a message fails.
     *
     * @property message The error message.
     * @property cause The underlying cause.
     */
    class ReceiveFailedException(
        message: String = "Failed to receive WebSocket message",
        cause: Throwable? = null
    ) : WebSocketException(message, cause)

    /**
     * Exception thrown when trying to use a WebSocket that is not connected.
     */
    class NotConnectedException(
        message: String = "WebSocket is not connected"
    ) : WebSocketException(message)

    /**
     * Exception thrown when a timeout occurs.
     *
     * @property timeoutMs The timeout duration in milliseconds.
     */
    class TimeoutException(
        val timeoutMs: Long,
        message: String = "WebSocket operation timed out after ${timeoutMs}ms"
    ) : WebSocketException(message)

    /**
     * Exception thrown when message parsing fails.
     *
     * @property rawMessage The raw message that failed to parse.
     * @property cause The underlying parse exception.
     */
    class ParseException(
        val rawMessage: String,
        message: String = "Failed to parse WebSocket message",
        cause: Throwable? = null
    ) : WebSocketException(message, cause)
}
