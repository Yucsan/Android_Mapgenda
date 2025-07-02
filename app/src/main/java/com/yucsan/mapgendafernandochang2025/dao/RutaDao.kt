package com.yucsan.mapgendafernandochang2025.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yucsan.mapmapgendafernandochang2025.entidad.LugarConOrden
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaEntity
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaLugarCrossRef

import kotlinx.coroutines.flow.Flow

@Dao
interface RutaDao {

    // Insertar Ruta base
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRuta(ruta: RutaEntity): Long

    @Query("SELECT * FROM rutas WHERE nombre = :nombre AND ubicacionId = :ubicacionId LIMIT 1")
    suspend fun obtenerRutaPorNombreYUbicacion(nombre: String, ubicacionId: Long?): RutaEntity?

    @Query("SELECT * FROM rutas WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerRutaPorNombre(nombre: String): RutaEntity?


    // Insertar referencias entre Ruta y Lugares
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReferencias(rutaLugares: List<RutaLugarCrossRef>)


    // Obtener todas las rutas con sus lugares ordenada por fecha de creación
    @Transaction
    @Query("SELECT * FROM rutas ORDER BY fechaDeCreacion DESC")
    fun obtenerRutasConLugares(): Flow<List<RutaConLugares>>


    @Query("SELECT * FROM rutas WHERE id = :rutaId LIMIT 1")
    suspend fun obtenerRutaPorId(rutaId: Long): RutaEntity

    // Eliminar Ruta y sus relaciones
    @Transaction
    suspend fun eliminarRutaConRelaciones(rutaId: Long) {
        eliminarReferenciasPorRuta(rutaId)
        eliminarRutaPorId(rutaId)
    }

    @Query("DELETE FROM rutas WHERE id = :rutaId")
    suspend fun eliminarRutaPorId(rutaId: Long)

    @Query("DELETE FROM RutaLugarCrossRef WHERE rutaId = :rutaId")
    suspend fun eliminarReferenciasPorRuta(rutaId: Long)

    // Actualizar ruta (nombre, categoría o ubicación)
    @Update
    suspend fun actualizarRuta(ruta: RutaEntity)

    // Agregar lugares a una ruta ya existente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarLugaresARuta(refs: List<RutaLugarCrossRef>)

    // Eliminar lugar específico de una ruta
    @Query("DELETE FROM RutaLugarCrossRef WHERE rutaId = :rutaId AND lugarId = :lugarId")
    suspend fun eliminarLugarDeRuta(rutaId: Long, lugarId: String)

    @Transaction
    @Query("""
    SELECT l.*, rlc.orden
    FROM lugares l
    INNER JOIN RutaLugarCrossRef rlc ON l.id = rlc.lugarId
    WHERE rlc.rutaId = :rutaId
    ORDER BY rlc.orden ASC
""")
    suspend fun obtenerLugaresOrdenadosPorRuta(rutaId: Long): List<LugarConOrden>


    @Transaction
    @Query("SELECT * FROM rutas")
    suspend fun obtenerRutasConLugaresOnce(): List<RutaConLugares>

    @Transaction
    @Query("SELECT * FROM rutas WHERE usuarioId = :usuarioId ORDER BY fechaDeCreacion DESC")
    fun obtenerRutasConLugaresPorUsuario(usuarioId: String): Flow<List<RutaConLugares>>


    // borrar todas las rutas y sus relaciones
    @Transaction
    suspend fun eliminarTodasLasRutasConRelaciones() {
        eliminarTodasLasReferencias()
        eliminarTodasLasRutas()
    }

    @Query("DELETE FROM RutaLugarCrossRef")
    suspend fun eliminarTodasLasReferencias()

    @Query("DELETE FROM rutas")
    suspend fun eliminarTodasLasRutas()





}
