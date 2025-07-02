package com.yucsan.mapgendafernandochang2025.servicio.log

import com.yucsan.mapgendafernandochang2025.dto.UsuarioDTO
import com.yucsan.mapgendafernandochang2025.entidad.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("usuarios/login-google")
    suspend fun loginConGoogle(@Body body: Map<String, String>): Response<LoginResponse>

}