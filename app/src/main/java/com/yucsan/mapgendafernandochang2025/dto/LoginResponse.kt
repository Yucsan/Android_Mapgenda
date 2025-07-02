package com.yucsan.mapgendafernandochang2025.dto

import com.yucsan.mapgendafernandochang2025.dto.UsuarioDTO

data class LoginResponse(
    val usuario: UsuarioDTO,
    val token: String
)