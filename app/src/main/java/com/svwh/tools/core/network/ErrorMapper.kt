package com.svwh.tools.core.network

import com.svwh.tools.core.common.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

fun Throwable.toAppError(): AppError {
    return when (this) {
        is SocketTimeoutException -> AppError.Timeout
        is IOException -> AppError.NetworkUnavailable
        is HttpException -> AppError.Http(code(), message())
        is SerializationException -> AppError.Serialization(message)
        else -> AppError.Unknown(this)
    }
}
