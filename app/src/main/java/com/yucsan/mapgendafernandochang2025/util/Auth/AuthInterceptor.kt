package com.yucsan.mapgendafernandochang2025.util.Auth


import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    @OptIn(UnstableApi::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()

        val requestBuilder = original.newBuilder()

        if (!token.isNullOrEmpty()) {
            Log.d("AUTH_INTERCEPTOR", "🛡️ Enviando token en Authorization: Bearer ${token.take(15)}...") // No mostrar todo
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.w("AUTH_INTERCEPTOR", "⚠️ Token nulo o vacío, no se añadió Authorization")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

