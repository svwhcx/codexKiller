package com.svwh.tools.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Timeout : AppError
    data class Http(val code: Int, val message: String?) : AppError
    data class Serialization(val message: String?) : AppError
    data class Unknown(val throwable: Throwable) : AppError
}
