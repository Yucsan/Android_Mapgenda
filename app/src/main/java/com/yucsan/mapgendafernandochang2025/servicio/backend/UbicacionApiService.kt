package com.yucsan.mapgendafernandochang2025.servicio.backend

import com.yucsan.mapgendafernandochang2025.dto.UbicacionDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UbicacionApiService {
    @POST("ubicaciones")
    suspend fun subirUbicacion(@Body dto: UbicacionDTO): Response<UbicacionDTO>

    @GET("ubicaciones/usuario")
    suspend fun obtenerUbicaciones(): List<UbicacionDTO>
}