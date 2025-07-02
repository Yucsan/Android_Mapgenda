package com.yucsan.mapgendafernandochang2025.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository

class LugarRutaOfflineViewModelFactory(
    private val app: Application,
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LugarRutaOfflineViewModel::class.java)) {
            return LugarRutaOfflineViewModel(app, usuarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
