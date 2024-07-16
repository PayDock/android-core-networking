package com.paydock.core.network.dto.error

import com.paydock.core.network.dto.Resource
import com.paydock.core.network.exceptions.ApiException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing a generic API error response.
 *
 * @property status The HTTP status code.
 * @property error The API error.
 * @property summary The error summary.
 */
@Serializable
data class ApiErrorResponse(
    val status: Int,
    val error: ApiError? = null,
    val resource: Resource<Unit>? = null,
    @SerialName("error_summary") val summary: ErrorSummary
)

/**
 * Extension property that provides a error summary message.
 *
 * @return The displayable error message.
 */
val ApiErrorResponse?.displayableMessage: String
    get() = this?.summary?.message ?: ""

/**
 * Converts the [ApiErrorResponse] to an [ApiException].
 */
fun ApiErrorResponse.toApiError() = ApiException(this)