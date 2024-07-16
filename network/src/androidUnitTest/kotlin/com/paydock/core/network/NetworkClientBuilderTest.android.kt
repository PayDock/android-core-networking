@file:Suppress("Filename", "MatchingDeclarationName")
package com.paydock.core.network

import io.ktor.client.request.get
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidNetworkClientBuilderTest {

    @Test
    fun testHttpClientWithCustomInterceptors() = runBlockingTest {
        val interceptor = mockk<Interceptor>(relaxed = true)
        every { interceptor.intercept(any()) } answers { invocation ->
            val chain = invocation.invocation.args[0] as Interceptor.Chain
            val originalRequest = chain.request()
            val response = Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(originalRequest)
                .body("{}".toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
            response
        }

        val httpClient = NetworkClientBuilder.create()
            .setBaseUrl("example.com")
            .addInterceptor(interceptor)
            .build()

        assertNotNull(httpClient)
        httpClient.get("/")
        verify { interceptor.intercept(any()) }
    }
}
