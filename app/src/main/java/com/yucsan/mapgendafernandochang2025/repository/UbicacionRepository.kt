package com.yucsan.mapgendafernandochang2025.repository



import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.dao.UbicacionDao
import com.yucsan.mapgendafernandochang2025.dto.toDTO
import com.yucsan.mapgendafernandochang2025.dto.toEntity
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UbicacionRepository(private val dao: UbicacionDao) {

    suspend fun insertarUbicacion(ubicacion: UbicacionLocal) {
        dao.insertarUbicacion(ubicacion)
    }

    suspend fun insertarTodas(ubicaciones: List<UbicacionLocal>) {
        dao.insertarTodas(ubicaciones)
    }


    suspend fun eliminarUbicacion(ubicacion: UbicacionLocal) {
        dao.eliminarUbicacion(ubicacion)
    }

    suspend fun eliminarPorId(id: Int) {
        dao.eliminarPorId(id)
    }

    suspend fun actualizarTipo(id: Int, nuevoTipo: String) {
        dao.actualizarTipo(id, nuevoTipo)
    }

    fun obtenerTodas(): Flow<List<UbicacionLocal>> {
        return dao.obtenerTodas()
    }

    fun obtenerUbicacionesPorUsuarioId(usuarioId: String): Flow<List<UbicacionLocal>> {
        return dao.obtenerPorUsuarioId(usuarioId)
    }

    fun buscar(query: String): Flow<List<UbicacionLocal>> {
        return dao.buscarPorNombreOTipo(query)
    }

    suspend fun actualizarUbicacionCompleta(id: Int, nuevoNombre: String, nuevoTipo: String) {
        dao.actualizarUbicacionCompleta(id, nuevoNombre, nuevoTipo)
    }

    suspend fun insertarUbicacionYRetornarId(ubicacion: UbicacionLocal): Long {
        return dao.insertarUbicacionYRetornarId(ubicacion)
    }

    suspend fun eliminarTodas() {
        dao.eliminarTodas()
    }


    //  ****************    funciones BACKEND
    @OptIn(UnstableApi::class)
    suspend fun sincronizarUbicacionesConBackend(usuarioId: String, token: String) {
        val ubicaciones = obtenerTodas().first()

        Log.d("SYNC_UBICACION", "Sincronizando ${ubicaciones.size} ubicaciones con el backend")

        RetrofitInstance.setTokenProvider { token }

        for (ubicacion in ubicaciones) {
            try {
                val dto = ubicacion.toDTO(usuarioId)
                Log.d("DTO_DEBUG", "Subiendo: $dto")

                val response = RetrofitInstance.ubicacionApi.subirUbicacion(dto)
                if (response.isSuccessful) {
                    Log.d("SYNC_UBICACION", "✅ Subida: ${dto.nombre}")
                } else {
                    Log.e("SYNC_UBICACION", "❌ Falló subida: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SYNC_UBICACION", "❌ Error red: ${e.localizedMessage}")
            }
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun descargarUbicacionesDesdeBackend(usuarioId: String, token: String) {
        RetrofitInstance.setTokenProvider { token }

        try {
            val listaDTO = RetrofitInstance.ubicacionApi.obtenerUbicaciones()
            val listaLocal = listaDTO.map { it.toEntity() }

            // Limpia la base local antes de insertar si quieres evitar duplicados
            insertarTodas(listaLocal)

            Log.d("SYNC_DESCARGA", "⬇️ Descargadas ${listaLocal.size} ubicaciones del backend")

        } catch (e: Exception) {
            Log.e("SYNC_DESCARGA", "❌ Error al descargar: ${e.message}")
        }
    }

}
