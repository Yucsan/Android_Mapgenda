package com.yucsan.mapgendafernandochang2025.dao

import androidx.room.*
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

import java.util.UUID

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario LIMIT 1")
    suspend fun obtenerUsuario(): UsuarioEntity?

    @Query("SELECT id FROM usuario LIMIT 1")
    suspend fun obtenerIdUsuario(): String?

    @Query("SELECT * FROM usuario WHERE sincronizado = 1 LIMIT 1")
    suspend fun obtenerUsuarioSincronizado(): UsuarioEntity?

    @Query("UPDATE usuario SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: String)

    @Query("DELETE FROM usuario")
    suspend fun eliminarUsuario()
}