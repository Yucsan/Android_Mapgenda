package com.yucsan.mapgendafernandochang2025.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModelFactory(
    private val usuarioViewModel: UsuarioViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(usuarioViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
