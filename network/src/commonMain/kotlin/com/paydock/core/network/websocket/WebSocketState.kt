package com.paydock.core.network.websocket

/**
 * Enum representing WebSocket connection states.
 */
enum class WebSocketState {
    /**
     * WebSocket is disconnected.
     */
    DISCONNECTED,

    /**
     * WebSocket is connecting.
     */
    CONNECTING,

    /**
     * WebSocket is connected and ready.
     */
    CONNECTED,

    /**
     * WebSocket is closing.
     */
    CLOSING,

    /**
     * WebSocket connection failed.
     */
    FAILED
}
