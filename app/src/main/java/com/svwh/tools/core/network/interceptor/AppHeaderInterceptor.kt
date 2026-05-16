package com.svwh.tools.core.network.interceptor

import com.svwh.tools.BuildConfig
import java.util.UUID
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AppHeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("User-Agent", "STool/${BuildConfig.VERSION_NAME}")
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-Request-Id", UUID.randomUUID().toString())
            .build()

        return chain.proceed(request)
    }
}
