package com.yucsan.mapgendafernandochang2025.util.Auth

import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

sealed class AuthState {

    object Loading : AuthState()
    object NoAutenticado : AuthState()

    data class Autenticado(
        val usuario: UsuarioEntity,
        val token: String
    ) : AuthState()
}
