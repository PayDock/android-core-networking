package com.paydock.core.network.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

/**
 * Android-specific implementation of WebSocketClient.
 *
 * This class uses the Ktor WebSocket client with OkHttp engine for Android.
 *
 * @param client The Ktor HttpClient configured for Android (OkHttp).
 * @param config The WebSocket configuration.
 * @param scope The CoroutineScope for managing coroutines.
 */
class AndroidWebSocketClient(
    client: HttpClient,
    config: WebSocketConfig,
    scope: CoroutineScope
) : KtorWebSocketClient(client, config, scope)

/**
 * Creates an Android-specific WebSocketClient instance.
 *
 * @param client The HttpClient to use.
 * @param config The WebSocketConfig.
 * @param scope The CoroutineScope for managing coroutines.
 * @return An AndroidWebSocketClient instance.
 */
actual fun createWebSocketClient(
    client: HttpClient,
    config: WebSocketConfig,
    scope: CoroutineScope
): WebSocketClient {
    return AndroidWebSocketClient(client, config, scope)
}
