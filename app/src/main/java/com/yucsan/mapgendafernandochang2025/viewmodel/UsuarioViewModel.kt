package com.yucsan.mapgendafernandochang2025.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity
import com.yucsan.mapgendafernandochang2025.mapper.toDTO
import com.yucsan.mapgendafernandochang2025.mapper.toEntity
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.yucsan.mapgendafernandochang2025.mapper.toEntity

class UsuarioViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario

    @OptIn(UnstableApi::class)
    fun cargarUsuario() {
        viewModelScope.launch {
            _usuario.value = repository.obtenerUsuario()
            Log.d("CARGARUSU", "Usuario cargado desde Room: ${_usuario.value?.fotoPerfilUri}")
        }
    }

    @OptIn(UnstableApi::class)
    fun refrescarUsuarioDesdeApi(id: UUID) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.obtenerUsuarioPorId(id.toString())
                if (response.isSuccessful) {
                    response.body()?.let { usuarioDTO ->
                        val actualizado = usuarioDTO.toEntity()
                        repository.guardarUsuario(actualizado)
                        _usuario.value = actualizado
                        Log.d("REFRESH", "Usuario actualizado desde API: ${actualizado.fotoPerfilUri}")
                    }
                } else {
                    Log.e("REFRESH", "Error de API: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("REFRESH", "Error al refrescar usuario", e)
            }
        }
    }

    fun establecerUsuarioActivo(id: String) {
        repository.establecerUsuarioActivo(id)
    }

    suspend fun obtenerUsuario(): UsuarioEntity? {
        return repository.obtenerUsuario()
    }

    suspend fun obtenerIdUsuario(): String? {
        return repository.obtenerIdUsuario()
    }

    fun actualizarFoto(uri: String) {
        viewModelScope.launch {
            repository.actualizarFoto(uri)
            _usuario.value = repository.obtenerUsuario() // refresca en UI
        }
    }

    @OptIn(UnstableApi::class)
    fun guardarUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            repository.guardarUsuario(usuario)
            Log.d("GUARDAR", "Usuario guardado con URI: ${usuario.fotoPerfilUri}")
            _usuario.value = usuario

            // 🟢 2. Intenta sincronizar con backend
            try {
                val dto = usuario.toDTO()
                RetrofitInstance.api.actualizarUsuario(usuario.id, dto)
                Log.d("SYNC_BACKEND", "Perfil actualizado en backend.")
            } catch (e: Exception) {
                Log.e("SYNC_BACKEND", "Error al sincronizar con backend", e)
            }

        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repository.cerrarSesion()
            _usuario.value = null
        }
    }

    @OptIn(UnstableApi::class)
    fun sincronizarUsuarioConBackend(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.obtenerUsuarioPorId(id)
                if (response.isSuccessful) {
                    val usuarioActualizado = response.body()?.toEntity()
                    usuarioActualizado?.let { guardarUsuario(it) }
                }
            } catch (e: Exception) {
                Log.e("SYNC_USUARIO", "Error al sincronizar usuario", e)
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun desactivarCuenta(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.desactivarCuenta(id)
                if (response.isSuccessful) {
                    cerrarSesion()
                    onSuccess()
                } else {
                    onError("Error al desactivar cuenta: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.localizedMessage}")
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun reactivarCuenta(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.reactivarCuenta(id)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error al reactivar cuenta: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.localizedMessage}")
            }
        }
    }

    fun buscarYReactivarPorEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Supón que tienes un endpoint GET /usuarios/email/{email}
                val response = RetrofitInstance.api.obtenerUsuarioPorEmail(email)
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    reactivarCuenta(usuario.id.toString(), onSuccess, onError)
                } else {
                    onError("Usuario no encontrado")
                }
            } catch (e: Exception) {
                onError("Error: ${e.localizedMessage}")
            }
        }
    }

}