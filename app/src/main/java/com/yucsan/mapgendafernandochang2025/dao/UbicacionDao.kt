package com.yucsan.mapgendafernandochang2025.dao

import androidx.room.*
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal

import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUbicacion(ubicacion: UbicacionLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(ubicaciones: List<UbicacionLocal>)


    @Delete
    suspend fun eliminarUbicacion(ubicacion: UbicacionLocal)

    @Query("DELETE FROM ubicaciones WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("UPDATE ubicaciones SET tipo = :nuevoTipo WHERE id = :id")
    suspend fun actualizarTipo(id: Int, nuevoTipo: String)

    @Query("SELECT * FROM ubicaciones ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<UbicacionLocal>>

    @Query("SELECT * FROM ubicaciones WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerPorUsuarioId(usuarioId: String): Flow<List<UbicacionLocal>>

    @Query("SELECT * FROM ubicaciones WHERE nombre LIKE '%' || :query || '%' OR tipo LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    fun buscarPorNombreOTipo(query: String): Flow<List<UbicacionLocal>>

    @Query("UPDATE ubicaciones SET nombre = :nuevoNombre, tipo = :nuevoTipo WHERE id = :id")
    suspend fun actualizarUbicacionCompleta(id: Int, nuevoNombre: String, nuevoTipo: String)

    @Insert
    suspend fun insertarUbicacionYRetornarId(ubicacion: UbicacionLocal): Long

    @Query("DELETE FROM ubicaciones")
    suspend fun eliminarTodas()

}
