package com.paydock.core.network.extensions

import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.dto.error.ApiErrorResponse
import kotlinx.serialization.encodeToString

/**
 * Inline function to convert a JSON string to a data class of type [R].
 *
 * @return An instance of the data class [R] created from the JSON string.
 */
inline fun <reified R : Any> String.convertToDataClass(): R =
    NetworkClientBuilder.getNetworkJson().decodeFromString(this)

/**
 * Inline function to convert a JSON string to a data class of type [R].
 *
 * @return An instance of the data class [R] created from the JSON string.
 */
inline fun <reified R : Any> R.convertToJsonString(): String? =
    NetworkClientBuilder.getNetworkJson().encodeToString(this).let { it.ifBlank { return null } }

/**
 * Attempts to convert the error response to an [ApiErrorResponse].
 */
fun convertToApiErrorResponse(errorResponse: String): ApiErrorResponse? =
    runCatching<ApiErrorResponse> {
        errorResponse.convertToDataClass()
    }.getOrNull()