package com.paydock.core.network.websocket

/**
 * Configuration for WebSocket connection.
 *
 * @property url The WebSocket URL to connect to.
 * @property headers Optional headers to send with the initial handshake.
 * @property pingIntervalMs Interval in milliseconds to send ping frames (0 to disable).
 * @property timeoutMs Timeout in milliseconds for connection and operations.
 * @property maxFrameSize Maximum size of a WebSocket frame in bytes.
 * @property reconnectOnFailure Whether to automatically reconnect on connection failure.
 * @property maxReconnectAttempts Maximum number of reconnection attempts (0 for unlimited).
 * @property reconnectDelayMs Delay in milliseconds between reconnection attempts.
 */
data class WebSocketConfig(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val pingIntervalMs: Long = 30_000, // 30 seconds default
    val timeoutMs: Long = 30_000,
    val maxFrameSize: Long = 1024 * 1024, // 1MB default
    val reconnectOnFailure: Boolean = false,
    val maxReconnectAttempts: Int = 3,
    val reconnectDelayMs: Long = 5_000 // 5 seconds default
)
