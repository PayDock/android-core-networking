package com.paydock.core.network.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for WebSocket client operations.
 *
 * This interface provides methods for establishing WebSocket connections,
 * sending/receiving messages, and monitoring connection state.
 */
interface WebSocketClient {

    /**
     * The current connection state of the WebSocket.
     */
    val state: StateFlow<WebSocketState>

    /**
     * Connects to the WebSocket server.
     *
     * @throws com.paydock.core.network.exceptions.WebSocketException.ConnectionFailedException if connection fails.
     */
    suspend fun connect()

    /**
     * Disconnects from the WebSocket server.
     *
     * @param code The close code (default: 1000 - Normal Closure).
     * @param reason The close reason (default: "Client closing").
     */
    suspend fun disconnect(code: Short = 1000, reason: String = "Client closing")

    /**
     * Sends a text message through the WebSocket.
     *
     * @param message The text message to send.
     * @throws com.paydock.core.network.exceptions.WebSocketException.NotConnectedException if not connected.
     * @throws com.paydock.core.network.exceptions.WebSocketException.SendFailedException if send fails.
     */
    suspend fun send(message: String)

    /**
     * Sends a binary message through the WebSocket.
     *
     * @param data The binary data to send.
     * @throws com.paydock.core.network.exceptions.WebSocketException.NotConnectedException if not connected.
     * @throws com.paydock.core.network.exceptions.WebSocketException.SendFailedException if send fails.
     */
    suspend fun send(data: ByteArray)

    /**
     * Observes incoming messages from the WebSocket.
     *
     * @return A [Flow] of [WebSocketMessage] representing incoming messages.
     */
    fun observeMessages(): Flow<WebSocketMessage>

    /**
     * Observes connection events (connect, disconnect, errors).
     *
     * @return A [Flow] of [WebSocketEvent] representing connection events.
     */
    fun observeEvents(): Flow<WebSocketEvent>
}

/**
 * Sealed class representing WebSocket events.
 */
sealed class WebSocketEvent {
    /**
     * WebSocket connection established successfully.
     */
    data object Connected : WebSocketEvent()

    /**
     * WebSocket disconnected.
     *
     * @property code The close code.
     * @property reason The close reason.
     */
    data class Disconnected(val code: Short, val reason: String) : WebSocketEvent()

    /**
     * WebSocket error occurred.
     *
     * @property error The error that occurred.
     */
    data class Error(val error: Throwable) : WebSocketEvent()

    /**
     * WebSocket is reconnecting.
     *
     * @property attempt The current reconnection attempt number.
     */
    data class Reconnecting(val attempt: Int) : WebSocketEvent()
}
