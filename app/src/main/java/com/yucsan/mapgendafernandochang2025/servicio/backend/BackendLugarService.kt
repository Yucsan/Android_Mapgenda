package com.yucsan.mapgendafernandochang2025.servicio.backend

import android.content.Context
import android.util.Log
import com.yucsan.mapgendafernandochang2025.dto.LugarDTO
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.util.config.ApiConfig

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class BackendLugarService {


   /* private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()*/

    //private val api = retrofit.create(LugarApiService::class.java)



    suspend fun subirLugar(lugar: LugarLocal) {

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val api = retrofit.create(LugarApiService::class.java)

        try {
            // ⚠️ Validar campos críticos
            val tipoSeguro = lugar.categoriaGeneral?.takeIf { it.isNotBlank() } ?: "otro"
            val direccionSegura = lugar.direccion.ifBlank { "Sin dirección" }

            val dto = LugarDTO(
                id = lugar.id,
                nombre = lugar.nombre,
                latitud = lugar.latitud,
                longitud = lugar.longitud,
                direccion = direccionSegura,
                tipo = tipoSeguro,
                calificacion = lugar.rating?.toDouble() ?: 0.0,
                fotoUrl = lugar.photoReference ?: "",
                abiertoAhora = lugar.abiertoAhora ?: false,
                duracionEstimadaMinutos = 0 // Si no lo usas, puede ir 0
            )

            Log.d("SYNC_BACKEND", "📤 Enviando a backend: ${dto.nombre} - Tipo: ${dto.tipo}")

            val response = api.crearLugar(dto)
            if (response.isSuccessful) {
                Log.d("SYNC_BACKEND", "✅ Lugar enviado: ${dto.nombre}")
            } else {
                Log.e("SYNC_BACKEND", "❌ Error con ${dto.nombre}: HTTP ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("SYNC_BACKEND", "❌ Excepción con ${lugar.nombre}: ${e.localizedMessage}")
        }
    }

    /*
        suspend fun sincronizarLugares(lugares: List<LugarLocal>) {
            lugares.forEach { subirLugar(it) }
        }
        */

    suspend fun sincronizarLugares(lugares: List<LugarLocal>) {
        Log.d("SYNC_BACKEND", "🔄 Iniciando sincronización de ${lugares.size} lugares")

        lugares.forEach { lugar ->
            Log.d("SYNC_BACKEND", "⏫ Preparando lugar: ${lugar.nombre} (${lugar.id})")

            if (lugar.categoriaGeneral.isNullOrBlank()) {
                Log.e("SYNC_BACKEND", "❌ ERROR: Lugar con tipo (categoriaGeneral) nulo o vacío: ${lugar.nombre} (${lugar.id})")
                return@forEach
            }

            if (lugar.nombre.isNullOrBlank() || lugar.id.isNullOrBlank()) {
                Log.e("SYNC_BACKEND", "❌ ERROR: Falta nombre o ID en: ${lugar}")
                return@forEach
            }

            // Extra: también puedes validar lat/lng o dirección si quieres
            subirLugar(lugar)
        }

        Log.d("SYNC_BACKEND", "✅ Sincronización finalizada")
    }



    suspend fun subirLugaresEnLote(lugares: List<LugarLocal>,  usuarioId: String) {
        try {
            Log.d("SYNC_BACKEND", "⏫ Subiendo ${lugares.size} lugares en lote...")

            // ✅ Usa RetrofitInstance con AuthInterceptor (ya configurado con token)
            val api = RetrofitInstance.lugarApi

            // ✨ Convertir LugarLocal → LugarDTO
            val dtos = lugares.map { lugar ->
                LugarDTO(
                    id = lugar.id,
                    nombre = lugar.nombre,
                    latitud = lugar.latitud,
                    longitud = lugar.longitud,
                    direccion = lugar.direccion.ifBlank { "Sin dirección" },
                    tipo = lugar.categoriaGeneral?.takeIf { it.isNotBlank() } ?: "otro",
                    calificacion = lugar.rating?.toDouble() ?: 0.0,
                    fotoUrl = lugar.photoReference ?: "",
                    abiertoAhora = lugar.abiertoAhora ?: false,
                    duracionEstimadaMinutos = 0,
                    usuarioId = usuarioId
                )
            }

            // 📡 Enviar al backend
            val response = api.subirLugares(dtos)

            if (response.isSuccessful) {
                Log.d("SYNC_BACKEND", "✅ Lote subido con éxito")
            } else {
                Log.e(
                    "SYNC_BACKEND",
                    "❌ Fallo subiendo lote: ${response.code()} - ${response.errorBody()?.string()}"
                )
            }

        } catch (e: Exception) {
            Log.e("SYNC_BACKEND", "❌ Error al subir lote", e)
        }
    }







}
