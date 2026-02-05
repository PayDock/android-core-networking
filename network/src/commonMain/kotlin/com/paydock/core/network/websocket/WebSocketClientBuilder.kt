package com.paydock.core.network.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.milliseconds

/**
 * Builder for creating [WebSocketClient] instances.
 *
 * This class provides a fluent interface for configuring and building WebSocket clients.
 */
class WebSocketClientBuilder {

    private var url: String? = null
    private var headers: Map<String, String> = emptyMap()
    private var pingIntervalMs: Long = 30_000
    private var timeoutMs: Long = 30_000
    private var maxFrameSize: Long = 1024 * 1024
    private var reconnectOnFailure: Boolean = false
    private var maxReconnectAttempts: Int = 3
    private var reconnectDelayMs: Long = 5_000
    private var httpClient: HttpClient? = null

    /**
     * Sets the WebSocket URL.
     *
     * @param url The WebSocket URL (must start with ws:// or wss://).
     * @return This builder instance.
     */
    fun setUrl(url: String) = apply {
        require(url.startsWith("ws://") || url.startsWith("wss://")) {
            "WebSocket URL must start with ws:// or wss://"
        }
        this.url = url
    }

    /**
     * Sets custom headers for the WebSocket handshake.
     *
     * @param headers Map of header names to values.
     * @return This builder instance.
     */
    fun setHeaders(headers: Map<String, String>) = apply {
        this.headers = headers
    }

    /**
     * Adds a single header to the WebSocket handshake.
     *
     * @param name The header name.
     * @param value The header value.
     * @return This builder instance.
     */
    fun addHeader(name: String, value: String) = apply {
        this.headers = this.headers + (name to value)
    }

    /**
     * Sets the ping interval in milliseconds.
     *
     * @param intervalMs The ping interval (0 to disable).
     * @return This builder instance.
     */
    fun setPingInterval(intervalMs: Long) = apply {
        require(intervalMs >= 0) { "Ping interval must be non-negative" }
        this.pingIntervalMs = intervalMs
    }

    /**
     * Sets the timeout for connection and operations.
     *
     * @param timeoutMs The timeout in milliseconds.
     * @return This builder instance.
     */
    fun setTimeout(timeoutMs: Long) = apply {
        require(timeoutMs > 0) { "Timeout must be positive" }
        this.timeoutMs = timeoutMs
    }

    /**
     * Sets the maximum frame size.
     *
     * @param maxFrameSize The maximum frame size in bytes.
     * @return This builder instance.
     */
    fun setMaxFrameSize(maxFrameSize: Long) = apply {
        require(maxFrameSize > 0) { "Max frame size must be positive" }
        this.maxFrameSize = maxFrameSize
    }

    /**
     * Enables or disables automatic reconnection on failure.
     *
     * @param enable Whether to enable reconnection.
     * @return This builder instance.
     */
    fun setReconnectOnFailure(enable: Boolean) = apply {
        this.reconnectOnFailure = enable
    }

    /**
     * Sets the maximum number of reconnection attempts.
     *
     * @param maxAttempts The maximum attempts (0 for unlimited).
     * @return This builder instance.
     */
    fun setMaxReconnectAttempts(maxAttempts: Int) = apply {
        require(maxAttempts >= 0) { "Max reconnect attempts must be non-negative" }
        this.maxReconnectAttempts = maxAttempts
    }

    /**
     * Sets the delay between reconnection attempts.
     *
     * @param delayMs The delay in milliseconds.
     * @return This builder instance.
     */
    fun setReconnectDelay(delayMs: Long) = apply {
        require(delayMs >= 0) { "Reconnect delay must be non-negative" }
        this.reconnectDelayMs = delayMs
    }

    /**
     * Sets a custom HttpClient to use for WebSocket connections.
     *
     * If not set, a default client will be created.
     *
     * @param client The HttpClient instance.
     * @return This builder instance.
     */
    fun setHttpClient(client: HttpClient) = apply {
        this.httpClient = client
    }

    /**
     * Builds the WebSocketClient instance.
     *
     * @param scope The CoroutineScope to use for the WebSocket operations.
     * @return A configured WebSocketClient instance.
     * @throws IllegalStateException if URL is not set.
     */
    fun build(scope: CoroutineScope): WebSocketClient {
        val wsUrl = url ?: throw IllegalStateException("WebSocket URL must be set")

        val config = WebSocketConfig(
            url = wsUrl,
            headers = headers,
            pingIntervalMs = pingIntervalMs,
            timeoutMs = timeoutMs,
            maxFrameSize = maxFrameSize,
            reconnectOnFailure = reconnectOnFailure,
            maxReconnectAttempts = maxReconnectAttempts,
            reconnectDelayMs = reconnectDelayMs
        )

        val client = httpClient ?: createDefaultHttpClient()

        return createWebSocketClient(client, config, scope)
    }

    private fun createDefaultHttpClient(): HttpClient {
        return HttpClient {
            install(WebSockets) {
                pingInterval = this@WebSocketClientBuilder.pingIntervalMs.milliseconds
                maxFrameSize = this@WebSocketClientBuilder.maxFrameSize
            }
        }
    }

    companion object {
        /**
         * Creates a new WebSocketClientBuilder instance.
         *
         * @return A new builder instance.
         */
        fun create(): WebSocketClientBuilder = WebSocketClientBuilder()
    }
}

/**
 * Creates a platform-specific WebSocketClient instance.
 *
 * This function is expected to be implemented separately for each platform (Android, iOS).
 *
 * @param client The HttpClient to use.
 * @param config The WebSocketConfig.
 * @param scope The CoroutineScope for managing coroutines.
 * @return A platform-specific WebSocketClient implementation.
 */
expect fun createWebSocketClient(
    client: HttpClient,
    config: WebSocketConfig,
    scope: CoroutineScope
): WebSocketClient
