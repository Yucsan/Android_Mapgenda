package com.yucsan.mapgendafernandochang2025.repository


import com.yucsan.mapgendafernandochang2025.dao.UsuarioDao
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class UsuarioRepository(private val usuarioDao: UsuarioDao) {


    private val _usuarioActivo = MutableStateFlow<String?>(null)
    val usuarioActivo: StateFlow<String?> = _usuarioActivo.asStateFlow()

    suspend fun inicializarUsuarioActivo() {
        withContext(Dispatchers.IO) {
            val id = usuarioDao.obtenerIdUsuario()
            _usuarioActivo.value = id
        }
    }


    fun establecerUsuarioActivo(id: String) {
        _usuarioActivo.value = id
    }

    suspend fun guardarUsuario(usuario: UsuarioEntity) {
        withContext(Dispatchers.IO) {
            usuarioDao.insertarUsuario(usuario)
        }
    }

    suspend fun actualizarFoto(uri: String) {
        withContext(Dispatchers.IO) {
            val usuarioActual = usuarioDao.obtenerUsuario()
            if (usuarioActual != null) {
                val actualizado = usuarioActual.copy(fotoPerfilUri = uri)
                usuarioDao.insertarUsuario(actualizado)
            }
        }
    }

    suspend fun obtenerUsuario(): UsuarioEntity? {
        return withContext(Dispatchers.IO) {
            usuarioDao.obtenerUsuario()
        }
    }

    suspend fun obtenerIdUsuario(): String? {
      return withContext(Dispatchers.IO) {
            usuarioDao.obtenerIdUsuario()
        }
    }

    suspend fun obtenerUsuarioSincronizado(): UsuarioEntity? {
        return withContext(Dispatchers.IO) {
            usuarioDao.obtenerUsuarioSincronizado()
        }
    }


    suspend fun cerrarSesion() {
        withContext(Dispatchers.IO) {
            usuarioDao.eliminarUsuario()
        }
    }
}