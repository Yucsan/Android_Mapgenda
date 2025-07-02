package com.yucsan.mapgendafernandochang2025.entidad

import com.yucsan.mapgendafernandochang2025.dto.UsuarioDTO

data class LoginResponse(
    val token: String,
    val usuario: UsuarioDTO
)
