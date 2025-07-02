package com.yucsan.mapgendafernandochang2025.dto

import java.util.UUID

data class UsuarioDTO(
    val id: UUID,
    val nombre: String?,
    val apellido: String?,
    val email: String,
    val telefono: String?,
    val rol: String?,
    val pais: String?,
    val ciudad: String?,
    val direccion: String?,
    val descripcion: String?,
    val fotoPerfilUrl: String? = null,
    val verificado: Boolean
)