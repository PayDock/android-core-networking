package com.paydock.core.network.interceptor

import com.paydock.core.network.dto.error.toApiError
import com.paydock.core.network.exceptions.ApiParseException
import com.paydock.core.network.extensions.convertToApiErrorResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

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
                else -> ApiParseException(status = response.code, errorBody = errorBody)
            }
        } else {
            // Handle case where errorBody is null or blank - create empty error response
            val defaultErrorBody = "{}".toResponseBody("application/json".toMediaType())
            return response.newBuilder().body(defaultErrorBody).build()
        }
    }
}
