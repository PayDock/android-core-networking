package com.paydock.core.network.websocket

import com.paydock.core.network.exceptions.WebSocketException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Base implementation of [WebSocketClient] using Ktor.
 *
 * This abstract class provides the core WebSocket functionality that is
 * shared across all platforms (Android, iOS).
 *
 * @property client The Ktor [HttpClient] to use for WebSocket connections.
 * @property config The [WebSocketConfig] for connection settings.
 * @property scope The [CoroutineScope] for managing coroutines.
 */
abstract class KtorWebSocketClient(
    protected val client: HttpClient,
    protected val config: WebSocketConfig,
    protected val scope: CoroutineScope
) : WebSocketClient {

    private val _state = MutableStateFlow(WebSocketState.DISCONNECTED)
    override val state: StateFlow<WebSocketState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<WebSocketMessage>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _events = MutableSharedFlow<WebSocketEvent>(
        replay = 0,
        extraBufferCapacity = 50,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var session: DefaultClientWebSocketSession? = null
    private var reconnectAttempts = 0
    private var connectionJob: Job? = null
    private var pingJob: Job? = null

    override suspend fun connect() {
        if (_state.value == WebSocketState.CONNECTED || _state.value == WebSocketState.CONNECTING) {
            return
        }

        _state.value = WebSocketState.CONNECTING
        reconnectAttempts = 0

        try {
            establishConnection()
        } catch (e: Exception) {
            _state.value = WebSocketState.FAILED
            _events.emit(
                WebSocketEvent.Error(
                    WebSocketException.ConnectionFailedException(config.url, cause = e)
                )
            )

            if (config.reconnectOnFailure) {
                handleReconnect()
            } else {
                throw WebSocketException.ConnectionFailedException(config.url, cause = e)
            }
        }
    }

    private suspend fun establishConnection() {
        connectionJob = scope.launch {
            try {
                client.webSocket(
                    urlString = config.url,
                    request = {
                        // Add custom headers
                        config.headers.forEach { (key, value) ->
                            headers.append(key, value)
                        }
                    }
                ) {
                    session = this
                    _state.value = WebSocketState.CONNECTED
                    _events.emit(WebSocketEvent.Connected)
                    reconnectAttempts = 0

                    // Start ping job if configured
                    if (config.pingIntervalMs > 0) {
                        startPingJob()
                    }

                    // Listen for incoming frames
                    listenForMessages()
                }
            } catch (e: CancellationException) {
                // Intentional cancellation
                throw e
            } catch (e: Exception) {
                handleConnectionError(e)
            } finally {
                cleanupConnection()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.listenForMessages() {
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        _messages.emit(WebSocketMessage.Text(text))
                    }
                    is Frame.Binary -> {
                        val data = frame.data
                        _messages.emit(WebSocketMessage.Binary(data))
                    }
                    is Frame.Close -> {
                        val code = frame.readReason()?.code ?: 1000
                        val reason = frame.readReason()?.message ?: "Connection closed"
                        _events.emit(WebSocketEvent.Disconnected(code, reason))
                        _state.value = WebSocketState.DISCONNECTED
                    }
                    is Frame.Ping, is Frame.Pong -> {
                        // Handled automatically by Ktor
                    }
                    else -> {
                        // Ignore any other frame types
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _events.emit(
                WebSocketEvent.Error(
                    WebSocketException.ReceiveFailedException(cause = e)
                )
            )
        }
    }

    private fun startPingJob() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive && _state.value == WebSocketState.CONNECTED) {
                delay(config.pingIntervalMs)
                try {
                    session?.flush()
                } catch (e: Exception) {
                    // Ping failed, connection might be dead
                    _events.emit(WebSocketEvent.Error(e))
                }
            }
        }
    }

    private suspend fun handleConnectionError(error: Exception) {
        _state.value = WebSocketState.FAILED
        _events.emit(WebSocketEvent.Error(error))

        if (config.reconnectOnFailure) {
            handleReconnect()
        }
    }

    private suspend fun handleReconnect() {
        if (config.maxReconnectAttempts in 1..reconnectAttempts) {
            _state.value = WebSocketState.DISCONNECTED
            return
        }

        reconnectAttempts++
        _events.emit(WebSocketEvent.Reconnecting(reconnectAttempts))
        delay(config.reconnectDelayMs)

        try {
            establishConnection()
        } catch (e: Exception) {
            // Will be handled by establishConnection
        }
    }

    private suspend fun cleanupConnection() {
        pingJob?.cancelAndJoin()
        pingJob = null
        session = null

        if (_state.value != WebSocketState.DISCONNECTED) {
            _state.value = WebSocketState.DISCONNECTED
        }
    }

    override suspend fun disconnect(code: Short, reason: String) {
        if (_state.value == WebSocketState.DISCONNECTED || _state.value == WebSocketState.CLOSING) {
            return
        }

        _state.value = WebSocketState.CLOSING

        try {
            session?.close(CloseReason(code, reason))
            connectionJob?.cancelAndJoin()
        } finally {
            cleanupConnection()
            _events.emit(WebSocketEvent.Disconnected(code, reason))
        }
    }

    override suspend fun send(message: String) {
        val currentSession = session
            ?: throw WebSocketException.NotConnectedException()

        if (_state.value != WebSocketState.CONNECTED) {
            throw WebSocketException.NotConnectedException()
        }

        try {
            currentSession.send(Frame.Text(message))
        } catch (e: Exception) {
            throw WebSocketException.SendFailedException(cause = e)
        }
    }

    override suspend fun send(data: ByteArray) {
        val currentSession = session
            ?: throw WebSocketException.NotConnectedException()

        if (_state.value != WebSocketState.CONNECTED) {
            throw WebSocketException.NotConnectedException()
        }

        try {
            currentSession.send(Frame.Binary(true, data))
        } catch (e: Exception) {
            throw WebSocketException.SendFailedException(cause = e)
        }
    }

    override fun observeMessages(): Flow<WebSocketMessage> = _messages.asSharedFlow()

    override fun observeEvents(): Flow<WebSocketEvent> = _events.asSharedFlow()
}
