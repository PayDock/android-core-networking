package com.paydock.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NetworkClientBuilderTest {

    @Test
    fun `test HttpClient creation with default settings`() = runBlockingTest {
        val mockEngine = MockEngine.create {
            addHandler { _ ->
                respond(
                    content = "Hello, World",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                )
            }
        } as MockEngine
        val httpClient = NetworkClientBuilder.create()
            .setBaseUrl("example.com")
            .setMockEngine(mockEngine)
            .build()

        assertNotNull(httpClient)
        val response: HttpResponse = httpClient.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, World", response.bodyAsText())
    }
}
