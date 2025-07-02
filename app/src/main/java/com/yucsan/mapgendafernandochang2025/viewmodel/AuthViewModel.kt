package com.yucsan.mapgendafernandochang2025.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioViewModel: UsuarioViewModel
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /*‼️ nuevo */
    private var inicializado = false

    fun initAuth(context: Context) {
        if (inicializado) return           // ⬅️  se ignora la 2ª llamada
        inicializado = true

        Log.d("AUTH", "initAuth() → Loading")

        viewModelScope.launch {
            // Verificar si es primera instalación
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val esPrimeraInstalacion = !prefs.contains("primera_instalacion")
            
            if (esPrimeraInstalacion) {
                // Marcar que ya no es primera instalación
                prefs.edit().putBoolean("primera_instalacion", false).apply()
                // Forzar estado NoAutenticado
                _authState.value = AuthState.NoAutenticado
                return@launch
            }

            val usuario = usuarioViewModel.obtenerUsuario()
            val token   = obtenerToken(context)

            val nuevoEstado = if (usuario != null && token != null)
                AuthState.Autenticado(usuario, token)
            else
                AuthState.NoAutenticado

            _authState.value = nuevoEstado
            Log.d("AUTH", "initAuth() → $nuevoEstado")
        }
    }



    fun iniciarSesion(context: Context, usuario: UsuarioEntity, token: String) {
        viewModelScope.launch {
            val usuarioSincronizado = usuario.copy(sincronizado = true)
            usuarioViewModel.guardarUsuario(usuarioSincronizado)
            guardarToken(context, token)

            RetrofitInstance.setTokenProvider { obtenerToken(context) }

            _authState.value = AuthState.Autenticado(usuarioSincronizado, token)
            usuarioViewModel.sincronizarUsuarioConBackend(usuario.id) // Aseguramos que el usuario esté sincronizado
        }
    }

    fun cerrarSesion(context: Context) {
        viewModelScope.launch {
            usuarioViewModel.cerrarSesion()
            clearToken(context)
            _authState.value = AuthState.NoAutenticado
        }
    }

    private fun guardarToken(context: Context, token: String) {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("jwt_token", token)
            .apply()
    }

    private fun obtenerToken(context: Context): String? {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("jwt_token", null)
    }

    private fun clearToken(context: Context) {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("jwt_token")
            .apply()
    }

    fun getTokenSeguro(context: Context): String? {
        return obtenerToken(context)
    }

}
