package com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places

import android.util.Log
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.delay

class PlacesService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(PlacesApiService::class.java)

    suspend fun obtenerLugaresCercanos(
        lat: Double,
        lng: Double,
        apiKey: String,
        type: String,
        maxDistancia: Float,
        usarRankByDistance: Boolean = false // 🔁 Toggle
    ): List<LugarLocal> {
        val location = "$lat,$lng"
        val todos = mutableListOf<LugarLocal>()
        var nextToken: String? = null

        do {
            val response = if (nextToken == null) {
                if (usarRankByDistance) {
                    api.getNearbyPlacesByDistance(
                        location = location,
                        type = type,
                        apiKey = apiKey
                    )
                } else {
                    api.getNearbyPlacesByDistance(
                        location = location,
                        type = type,
                        apiKey = apiKey
                    )
                }
            } else {
                delay(2000)
                api.getNextPage(
                    pageToken = nextToken,
                    apiKey = apiKey
                )
            }

            if (response.status != "OK") {
                Log.e("PlacesService", "⚠️ Error en respuesta: ${response.status}")
                break
            }

            Log.d("PlacesService", "🔄 Página descargada: ${response.results.size} lugares")
            nextToken = response.next_page_token

            val nuevos = response.results.map {
                Log.d("PlacesService", "🔎 Resultado: ${it.name} con types: ${it.types}")

                val photoRef = it.photos?.firstOrNull()?.photo_reference

                LugarLocal(
                    id = it.place_id ?: "${it.name}-${it.geometry.location.lat}-${it.geometry.location.lng}",
                    nombre = it.name,
                    direccion = it.vicinity ?: "Sin dirección",
                    latitud = it.geometry.location.lat,
                    longitud = it.geometry.location.lng,
                    categoriaGeneral = type,
                    subcategoria = type,
                    tipos = it.types,
                    rating = it.rating,
                    precio = it.price_level,
                    abiertoAhora = it.opening_hours?.open_now,
                    estado = it.business_status,
                    photoReference = photoRef,
                    businessStatus = it.business_status,
                    userRatingsTotal = it.user_ratings_total
                )
            }

            todos += nuevos

        } while (nextToken != null)

        Log.d("PlacesService", "📦 Total lugares obtenidos: ${todos.size}")
        return todos.distinctBy { it.id }
    }


    //busqueda por nombres
    suspend fun buscarLugaresPorTexto(query: String, apiKey: String): List<LugarLocal> {
        val response = api.textSearch(query = query, apiKey = apiKey)

        if (response.status != "OK") {
            Log.e("PlacesService", "⚠️ TextSearch fallo: ${response.status}")
            return emptyList()
        }

        return response.results.map {
            LugarLocal(
                id = it.place_id,
                nombre = it.name,
                direccion = it.formatted_address,
                latitud = it.geometry.location.lat,
                longitud = it.geometry.location.lng,
                categoriaGeneral = it.types.firstOrNull() ?: "otro",
                subcategoria = it.types.firstOrNull() ?: "otro",
                tipos = it.types,
                rating = it.rating,
                precio = it.price_level,
                abiertoAhora = it.opening_hours?.open_now,
                estado = it.business_status,
                photoReference = it.photos?.firstOrNull()?.photo_reference,
                businessStatus = it.business_status,
                userRatingsTotal = it.user_ratings_total
            )
        }
    }

}

