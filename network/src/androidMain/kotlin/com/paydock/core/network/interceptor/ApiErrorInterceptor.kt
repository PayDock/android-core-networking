package com.paydock.core.network.interceptor

import com.paydock.core.network.dto.error.toApiError
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import com.paydock.core.network.extensions.convertToApiErrorResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Intercepts API errors and converts them into [ApiException] by decoding the Error response body.
 * In case serialization fails, the response will be delivered as is.
 */
internal class ApiErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Check if the response is successful
        if (response.isSuccessful) {
            return response
        }

        // Extract error body from the response
        val errorBody = response.body?.string()

        // If the error body is present, attempt to convert to specific error response structures
        if (!errorBody.isNullOrBlank()) {
            // Depending on the error structure, convert to appropriate data class
            val apiErrorResponse = convertToApiErrorResponse(errorBody)

            // Process the error data class accordingly and throw the appropriate ApiException
            throw when {
                apiErrorResponse != null -> apiErrorResponse.toApiError()
                else -> UnknownApiException(status = response.code, errorBody = errorBody)
            }
        } else {
            // Your existing error handling logic if deserialization fails
            val newErrorBody = errorBody?.toResponseBody("application/json".toMediaType())
            return response.newBuilder().body(newErrorBody).build()
        }
    }
}
