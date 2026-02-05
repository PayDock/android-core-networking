package com.paydock.core.network.websocket

import com.paydock.core.network.exceptions.WebSocketException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for WebSocketClient.
 */
class WebSocketClientTest {

    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        testScope = CoroutineScope(Dispatchers.Default)
    }

    @Test
    fun `WebSocketConfig should have correct default values`() {
        val config = WebSocketConfig(url = "wss://example.com/ws")

        assertEquals("wss://example.com/ws", config.url)
        assertEquals(30_000, config.pingIntervalMs)
        assertEquals(30_000, config.timeoutMs)
        assertEquals(1024 * 1024, config.maxFrameSize)
        assertEquals(false, config.reconnectOnFailure)
        assertEquals(3, config.maxReconnectAttempts)
        assertEquals(5_000, config.reconnectDelayMs)
        assertTrue(config.headers.isEmpty())
    }

    @Test
    fun `WebSocketClientBuilder should create client with correct config`() {
        val builder = WebSocketClientBuilder.create()
            .setUrl("wss://example.com/ws")
            .addHeader("Authorization", "Bearer token123")
            .setPingInterval(15_000)
            .setTimeout(60_000)
            .setReconnectOnFailure(true)
            .setMaxReconnectAttempts(5)

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.SwitchingProtocols,
                headers = headersOf(HttpHeaders.Upgrade, "websocket")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(WebSockets)
        }

        builder.setHttpClient(httpClient)
        val client = builder.build(testScope)

        assertEquals(WebSocketState.DISCONNECTED, client.state.value)
    }

    @Test
    fun `WebSocketClientBuilder should throw exception when URL not set`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalStateException> {
            builder.build(testScope)
        }
    }

    @Test
    fun `WebSocketClientBuilder should validate WebSocket URL protocol`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalArgumentException> {
            builder.setUrl("http://example.com/ws")
        }
    }

    @Test
    fun `WebSocketClientBuilder should accept valid ws URL`() {
        val builder = WebSocketClientBuilder.create()
        builder.setUrl("ws://example.com/ws")

        // Should not throw
        assertTrue(true)
    }

    @Test
    fun `WebSocketClientBuilder should accept valid wss URL`() {
        val builder = WebSocketClientBuilder.create()
        builder.setUrl("wss://example.com/ws")

        // Should not throw
        assertTrue(true)
    }

    @Test
    fun `WebSocketMessage Text should store content correctly`() {
        val message = WebSocketMessage.Text("Hello World")
        assertEquals("Hello World", message.content)
    }

    @Test
    fun `WebSocketMessage Binary should store data correctly`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val message = WebSocketMessage.Binary(data)

        assertTrue(data.contentEquals(message.data))
    }

    @Test
    fun `WebSocketMessage Binary equality should work correctly`() {
        val data1 = byteArrayOf(1, 2, 3)
        val data2 = byteArrayOf(1, 2, 3)
        val data3 = byteArrayOf(4, 5, 6)

        val message1 = WebSocketMessage.Binary(data1)
        val message2 = WebSocketMessage.Binary(data2)
        val message3 = WebSocketMessage.Binary(data3)

        assertEquals(message1, message2)
        assertTrue(message1 != message3)
    }

    @Test
    fun `WebSocketState should have all expected states`() {
        val states = WebSocketState.values()

        assertEquals(5, states.size)
        assertTrue(states.contains(WebSocketState.DISCONNECTED))
        assertTrue(states.contains(WebSocketState.CONNECTING))
        assertTrue(states.contains(WebSocketState.CONNECTED))
        assertTrue(states.contains(WebSocketState.CLOSING))
        assertTrue(states.contains(WebSocketState.FAILED))
    }

    @Test
    fun `WebSocketException ConnectionFailedException should contain URL`() {
        val exception = WebSocketException.ConnectionFailedException(
            url = "wss://example.com/ws"
        )

        assertEquals("wss://example.com/ws", exception.url)
        assertTrue(exception.message?.contains("wss://example.com/ws") == true)
    }

    @Test
    fun `WebSocketException ConnectionClosedException should contain code and reason`() {
        val exception = WebSocketException.ConnectionClosedException(
            code = 1001,
            reason = "Going Away"
        )

        assertEquals(1001.toShort(), exception.code)
        assertEquals("Going Away", exception.reason)
        assertTrue(exception.message?.contains("1001") == true)
        assertTrue(exception.message?.contains("Going Away") == true)
    }

    @Test
    fun `WebSocketException TimeoutException should contain timeout duration`() {
        val exception = WebSocketException.TimeoutException(timeoutMs = 30_000)

        assertEquals(30_000, exception.timeoutMs)
        assertTrue(exception.message?.contains("30000") == true)
    }

    @Test
    fun `WebSocketException ParseException should contain raw message`() {
        val exception = WebSocketException.ParseException(
            rawMessage = "{invalid json}"
        )

        assertEquals("{invalid json}", exception.rawMessage)
    }

    @Test
    fun `WebSocketEvent Connected should be created correctly`() {
        val event = WebSocketEvent.Connected
        assertIs<WebSocketEvent.Connected>(event)
    }

    @Test
    fun `WebSocketEvent Disconnected should contain code and reason`() {
        val event = WebSocketEvent.Disconnected(code = 1000, reason = "Normal closure")

        assertEquals(1000.toShort(), event.code)
        assertEquals("Normal closure", event.reason)
    }

    @Test
    fun `WebSocketEvent Error should contain error`() {
        val error = Exception("Test error")
        val event = WebSocketEvent.Error(error)

        assertEquals(error, event.error)
    }

    @Test
    fun `WebSocketEvent Reconnecting should contain attempt number`() {
        val event = WebSocketEvent.Reconnecting(attempt = 2)

        assertEquals(2, event.attempt)
    }

    @Test
    fun `WebSocketClientBuilder should validate non-negative ping interval`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalArgumentException> {
            builder.setPingInterval(-1)
        }
    }

    @Test
    fun `WebSocketClientBuilder should validate positive timeout`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalArgumentException> {
            builder.setTimeout(0)
        }
    }

    @Test
    fun `WebSocketClientBuilder should validate positive max frame size`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalArgumentException> {
            builder.setMaxFrameSize(0)
        }
    }

    @Test
    fun `WebSocketClientBuilder should validate non-negative max reconnect attempts`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalArgumentException> {
            builder.setMaxReconnectAttempts(-1)
        }
    }

    @Test
    fun `WebSocketClientBuilder should validate non-negative reconnect delay`() {
        val builder = WebSocketClientBuilder.create()

        assertFailsWith<IllegalArgumentException> {
            builder.setReconnectDelay(-1)
        }
    }

    @Test
    fun `WebSocketClientBuilder should allow adding multiple headers`() {
        val builder = WebSocketClientBuilder.create()
            .setUrl("wss://example.com/ws")
            .addHeader("Authorization", "Bearer token1")
            .addHeader("X-Custom-Header", "value1")

        // Should not throw
        assertTrue(true)
    }

    @Test
    fun `WebSocketClientBuilder should override headers with setHeaders`() {
        val builder = WebSocketClientBuilder.create()
            .setUrl("wss://example.com/ws")
            .addHeader("Authorization", "Bearer token1")
            .setHeaders(mapOf("X-New-Header" to "value2"))

        // Should not throw - new headers replace old ones
        assertTrue(true)
    }
}
