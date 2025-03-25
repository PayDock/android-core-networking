package com.paydock.core.network.exceptions

import io.ktor.utils.io.errors.IOException

/**
 * Represents an exception that occurs when parsing the API response fails.
 *
 * This exception is thrown when the API returns a response that could not be
 * successfully parsed into the expected data model. It typically indicates
 * a mismatch between the expected data structure and the actual response.
 *
 * @property status The HTTP status code of the API response that caused the exception.
 * @property errorMessage A human-readable error message describing the parsing issue.
 *                       Defaults to "Unexpected error model - unable to decode JSON".
 * @property errorBody The raw body of the API response that failed to be parsed, if available.
 *                     Can be null if the response body was empty or inaccessible.
 * @constructor Creates an ApiParseException with the specified status, error message, and error body.
 * @extends IOException This class inherits from IOException to indicate an input/output related error.
 */
data class ApiParseException(
    val status: Int,
    val errorMessage: String = "Unexpected error model - unable to decode JSON",
    val errorBody: String?
) : IOException(errorMessage)
