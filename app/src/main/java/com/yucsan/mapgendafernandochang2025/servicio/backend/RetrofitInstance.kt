package com.yucsan.mapgendafernandochang2025.servicio.backend

import com.yucsan.mapgendafernandochang2025.util.Auth.AuthInterceptor
import com.yucsan.mapgendafernandochang2025.util.config.ApiConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val BASE_URL = ApiConfig.BASE_URL

    private var tokenProvider: () -> String? = { null } // inyectado desde fuera

    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { tokenProvider() }) // 👈 usa el token dinámico
        .build()

    val api: UsuarioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApiService::class.java)
    }

    val lugarApi: LugarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // 👈 incluye interceptor con token
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LugarApiService::class.java)
    }

    val ubicacionApi: UbicacionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UbicacionApiService::class.java)
    }

    val rutaApi: RutaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RutaApiService::class.java)
    }


}