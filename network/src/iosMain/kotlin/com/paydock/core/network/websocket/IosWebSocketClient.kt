package com.paydock.core.network.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

/**
 * iOS-specific implementation of WebSocketClient.
 *
 * This class uses the Ktor WebSocket client with Darwin engine for iOS.
 *
 * @param client The Ktor HttpClient configured for iOS (Darwin).
 * @param config The WebSocket configuration.
 * @param scope The CoroutineScope for managing coroutines.
 */
class IosWebSocketClient(
    client: HttpClient,
    config: WebSocketConfig,
    scope: CoroutineScope
) : KtorWebSocketClient(client, config, scope)

/**
 * Creates an iOS-specific WebSocketClient instance.
 *
 * @param client The HttpClient to use.
 * @param config The WebSocketConfig.
 * @param scope The CoroutineScope for managing coroutines.
 * @return An IosWebSocketClient instance.
 */
actual fun createWebSocketClient(
    client: HttpClient,
    config: WebSocketConfig,
    scope: CoroutineScope
): WebSocketClient {
    return IosWebSocketClient(client, config, scope)
}
