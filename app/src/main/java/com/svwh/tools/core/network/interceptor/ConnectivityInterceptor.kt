package com.svwh.tools.core.network.interceptor

import com.svwh.tools.core.network.NetworkMonitor
import java.io.IOException
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptor @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkMonitor.isOnline()) {
            throw IOException("Network is unavailable")
        }

        return chain.proceed(chain.request())
    }
}
