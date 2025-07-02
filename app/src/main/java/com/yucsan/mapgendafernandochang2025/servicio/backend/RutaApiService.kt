package com.yucsan.mapgendafernandochang2025.servicio.backend

import retrofit2.Response // ✅ CORRECTO

import com.yucsan.mapgendafernandochang2025.dto.RutaDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RutaApiService {

    @POST("rutas")
    suspend fun crearRuta(@Body dto: RutaDTO): Response<RutaDTO>

    @GET("rutas/usuario")
    suspend fun obtenerRutasPorUsuario(): List<RutaDTO>
}
