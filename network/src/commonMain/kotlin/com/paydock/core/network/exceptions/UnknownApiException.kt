package com.paydock.core.network.exceptions

import io.ktor.utils.io.errors.IOException

/**
 * Serializable data class representing an unknown API error response.
 *
 * @property status The HTTP status code.
 * @property errorMessage Default serialization error message.
 * @property errorBody The API error body.
 */
data class UnknownApiException(
    val status: Int,
    val errorMessage: String = "Unexpected error model - unable to decode JSON",
    val errorBody: String?
) : IOException(errorMessage)
