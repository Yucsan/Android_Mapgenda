package com.yucsan.mapgendafernandochang2025.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val id: String, // UUID como String
    val nombre: String?,
    val apellido: String?,
    val email: String,
    val telefono: String?,
    val rol: String?,
    val pais: String?,
    val ciudad: String?,
    val direccion: String?,
    val descripcion: String?,
    val fotoPerfilUri: String? = null,
    val verificado: Boolean,
    val sincronizado: Boolean = false
)
