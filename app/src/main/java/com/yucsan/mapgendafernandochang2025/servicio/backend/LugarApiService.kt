package com.yucsan.mapgendafernandochang2025.servicio.backend

import com.yucsan.mapgendafernandochang2025.dto.LugarDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LugarApiService {
    @POST("lugares")
    suspend fun crearLugar(@Body dto: LugarDTO): Response<LugarDTO>

    @POST("lugares/batch")
    suspend fun subirLugares(@Body lugares: List<LugarDTO>): Response<Unit>

    @GET("lugares/usuario/{usuarioId}")
    suspend fun obtenerLugaresDelUsuario(@Path("usuarioId") usuarioId: String): List<LugarDTO>

}
