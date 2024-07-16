package com.paydock.core.network.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage
import io.ktor.utils.io.errors.IOException

/**
 * Represents the error that occurred while communicating with a REST API.
 *
 * @property error The underlying error response that caused this exception.
 * @constructor Creates an ApiException with the specified error response.
 *              The displayable message is derived from the error response.
 */
data class ApiException(
    val error: ApiErrorResponse
) : IOException(error.displayableMessage)