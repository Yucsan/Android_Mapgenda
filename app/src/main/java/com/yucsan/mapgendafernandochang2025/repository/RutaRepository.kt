package com.yucsan.mapgendafernandochang2025.repository


import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.dao.RutaDao
import com.yucsan.mapgendafernandochang2025.dto.RutaDTO
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugaresOrdenados
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaEntity
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaLugarCrossRef
import kotlinx.coroutines.flow.Flow

class RutaRepository(private val dao: RutaDao) {

    @OptIn(UnstableApi::class)
    suspend fun crearRutaConLugares(
        nombre: String,
        categoria: String?,
        ubicacionId: Long?,
        lugares: List<LugarLocal>,
        polylineCodificada: String? = null,
        usuarioId: String? = null
    ) {
        Log.d("RutaRepository", "🗂️ Guardando ruta y lugares asociados")
        try {

        val rutaId = dao.insertarRuta(
            RutaEntity(
                nombre = nombre,
                categoria = categoria,
                ubicacionId = ubicacionId,
                polylineCodificada = polylineCodificada,
                fechaDeCreacion = System.currentTimeMillis(),
                usuarioId = usuarioId
            )
        )

        val refs = lugares.mapIndexed { index, it ->
            RutaLugarCrossRef(
                rutaId = rutaId,
                lugarId = it.id,
                orden = index
            )
        }
        dao.insertarReferencias(refs)

        } catch (e: Exception) {
            Log.e("RutaRepository", "❌ Error guardando ruta: ${e.message}", e)
            throw e
        }
    }

    fun obtenerRutasConLugares(): Flow<List<RutaConLugares>> {
        return dao.obtenerRutasConLugares()
    }

    suspend fun eliminarRuta(rutaId: Long) {
        dao.eliminarRutaConRelaciones(rutaId)
    }

    suspend fun actualizarRuta(ruta: RutaEntity) {
        dao.actualizarRuta(ruta)
    }

    suspend fun actualizarOrdenLugares(rutaId: Long, lugaresOrdenados: List<LugarLocal>) {
        // Borrar referencias anteriores
        dao.eliminarReferenciasPorRuta(rutaId)

        // Insertar nuevas referencias con orden actualizado
        val nuevasReferencias = lugaresOrdenados.mapIndexed { index, lugar ->
            RutaLugarCrossRef(rutaId = rutaId, lugarId = lugar.id, orden = index)
        }
        dao.insertarReferencias(nuevasReferencias)
    }

    suspend fun obtenerRutaConLugaresOrdenados(rutaId: Long): RutaConLugaresOrdenados {
        val ruta = dao.obtenerRutaPorId(rutaId)
        val lugaresConOrden = dao.obtenerLugaresOrdenadosPorRuta(rutaId)
        return RutaConLugaresOrdenados(
            ruta = ruta,
            lugares = lugaresConOrden.map { it.lugar }
        )
    }

    suspend fun agregarLugaresARuta(rutaId: Long, lugares: List<LugarLocal>) {
        val refs = lugares.mapIndexed { index, it ->
            RutaLugarCrossRef(
                rutaId = rutaId,
                lugarId = it.id,
                orden = index
            )
        }
        dao.agregarLugaresARuta(refs)
    }

    suspend fun eliminarLugarDeRuta(rutaId: Long, lugarId: String) {
        dao.eliminarLugarDeRuta(rutaId, lugarId)
    }

    @OptIn(UnstableApi::class)
    suspend fun subirRutaAlBackend(ruta: RutaEntity, lugares: List<LugarLocal>, usuarioId: String, token: String) {
        RetrofitInstance.setTokenProvider { token }

        val dto = RutaDTO(
            nombre = ruta.nombre,
            origenLat = lugares.firstOrNull()?.latitud,
            origenLng = lugares.firstOrNull()?.longitud,
            destinoLat = lugares.lastOrNull()?.latitud,
            destinoLng = lugares.lastOrNull()?.longitud,
            modoTransporte = "driving",
            lugaresIntermedios = null, // opcional si quieres serializar
            polylineCodificada = ruta.polylineCodificada,
            categoria = ruta.categoria,
            ubicacionId = ruta.ubicacionId,
            lugarIdsOrdenados = lugares.map { it.id },
            usuarioId = usuarioId
        )

        val response = RetrofitInstance.rutaApi.crearRuta(dto)
        if (!response.isSuccessful) {
            throw Exception("Error al subir ruta: ${response.code()} ${response.message()}")
        }
    }


    @OptIn(UnstableApi::class)
    suspend fun descargarRutasDesdeBackend(usuarioId: String, token: String) {
        RetrofitInstance.setTokenProvider { token }
        Log.d("RutaRepository", "🔄 Descargando rutas desde el backend para el usuario $usuarioId")
        val rutasDTO = RetrofitInstance.rutaApi.obtenerRutasPorUsuario()
        for (dto in rutasDTO) {

            // Verificamos si la ruta ya existe por nombre y ubicación

            val existente = dao.obtenerRutaPorNombre(dto.nombre)

            val rutaId = if (existente == null) {
                val nuevaRuta = RutaEntity(
                    nombre = dto.nombre,
                    categoria = dto.categoria,
                    ubicacionId = dto.ubicacionId,
                    polylineCodificada = dto.polylineCodificada,
                    fechaDeCreacion = System.currentTimeMillis()
                )
                dao.insertarRuta(nuevaRuta)
            } else {
                existente.id // ya existe, usamos el ID original
            }

            val refs = dto.lugarIdsOrdenados.mapIndexed { index, id ->
                RutaLugarCrossRef(rutaId = rutaId, lugarId = id, orden = index)
            }

            dao.insertarReferencias(refs)
        }
    }



    @OptIn(UnstableApi::class)
    suspend fun subirTodasLasRutasLocalesAlBackend(usuarioId: String, token: String) {
        RetrofitInstance.setTokenProvider { token }

        val rutasLocales = dao.obtenerRutasConLugaresOnce() // necesitas este método DAO (lo definimos abajo)

        for (rutaConLugares in rutasLocales) {
            val ruta = rutaConLugares.ruta
            val lugares = rutaConLugares.lugares

            val dto = RutaDTO(
                nombre = ruta.nombre,
                origenLat = lugares.firstOrNull()?.latitud,
                origenLng = lugares.firstOrNull()?.longitud,
                destinoLat = lugares.lastOrNull()?.latitud,
                destinoLng = lugares.lastOrNull()?.longitud,
                modoTransporte = "driving",
                lugaresIntermedios = null,
                polylineCodificada = ruta.polylineCodificada,
                categoria = ruta.categoria,
                ubicacionId = ruta.ubicacionId,
                lugarIdsOrdenados = lugares.map { it.id },
                usuarioId = usuarioId
            )

            val response = RetrofitInstance.rutaApi.crearRuta(dto)
            if (!response.isSuccessful) {
                throw Exception("❌ Error al subir ruta '${ruta.nombre}': ${response.code()} ${response.message()}")
            }
        }
    }

    fun obtenerRutasConLugaresPorUsuario(usuarioId: String): Flow<List<RutaConLugares>> {
        return dao.obtenerRutasConLugaresPorUsuario(usuarioId)
    }

    suspend fun eliminarTodasLasRutas() {
        dao.eliminarTodasLasRutasConRelaciones()
    }



}
