package com.yucsan.mapgendafernandochang2025.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal

import com.yucsan.mapgendafernandochang2025.repository.RutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository

import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugaresOrdenados
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

class RutaViewModel(

    private val repository: RutaRepository,
    private val usuarioRepository: UsuarioRepository

) : ViewModel() {

    private val _rutas = MutableStateFlow<List<RutaConLugares>>(emptyList())
    val rutas: StateFlow<List<RutaConLugares>> = _rutas.asStateFlow()

    private val _usuarioId = MutableStateFlow<String?>(null)
    val usuarioId: StateFlow<String?> = _usuarioId.asStateFlow()

    private val _rutasPorUsuario = MutableStateFlow<List<RutaConLugares>>(emptyList())
    val rutasPorUsuario: StateFlow<List<RutaConLugares>> = _rutasPorUsuario.asStateFlow()


    fun obtenerUsuarioRepository(): UsuarioRepository = usuarioRepository





    init {

// Obtener y observar usuario activo
        viewModelScope.launch {
            usuarioRepository.usuarioActivo
                .filterNotNull()
                .distinctUntilChanged()
                .collect {
                    _usuarioId.value = it
                    Log.d("RutaViewModel", "👤 Usuario activo: $it")
                }
        }

        // Observar rutas del usuario
        viewModelScope.launch {
            usuarioId
                .filterNotNull()
                .flatMapLatest { id -> repository.obtenerRutasConLugaresPorUsuario(id) }
                .collect { _rutasPorUsuario.value = it }
        }


        observarRutas()
    }

    private fun observarRutas() {
        viewModelScope.launch {
            repository.obtenerRutasConLugares().collect { lista ->
                _rutas.value = lista
            }
        }
    }

    fun crearRuta(nombre: String, categoria: String?, ubicacionId: Long?, lugares: List<LugarLocal>, polylineCodificada: String? = null) {
        viewModelScope.launch {
            Log.d("RutaViewModel", "🔧 creandoRuta(nombre=$nombre, ubicacionId=$ubicacionId, lugares=${lugares.size}, polyline=${polylineCodificada?.take(20)}...)")

            val idUsuario = usuarioRepository.obtenerIdUsuario()
            usuarioRepository.establecerUsuarioActivo(idUsuario!!)

            try {
                repository.crearRutaConLugares(nombre, categoria, ubicacionId, lugares, polylineCodificada, idUsuario)
                Log.d("RutaViewModel", "✅ Ruta guardada exitosamente")
            } catch (e: Exception) {
                Log.e("RutaViewModel", "❌ Error al guardar ruta: ${e.message}", e)
            }
        }
    }


    fun eliminarRuta(rutaId: Long) {
        viewModelScope.launch {
            repository.eliminarRuta(rutaId)
        }
    }

    fun actualizarRuta(ruta: RutaEntity) {
        viewModelScope.launch {
            repository.actualizarRuta(ruta)
        }
    }

    fun agregarLugares(rutaId: Long, lugares: List<LugarLocal>) {
        viewModelScope.launch {
            repository.agregarLugaresARuta(rutaId, lugares)
        }
    }

    fun eliminarLugar(rutaId: Long, lugarId: String) {
        viewModelScope.launch {
            repository.eliminarLugarDeRuta(rutaId, lugarId)
        }
    }

    fun eliminarTodasLasRutas(context: Context? = null) {
        viewModelScope.launch {
            try {
                repository.eliminarTodasLasRutas()
                Log.d("RutaViewModel", "🗑️ Todas las rutas eliminadas correctamente")

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "🗑️ Todas las rutas eliminadas", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("RutaViewModel", "❌ Error al eliminar todas las rutas: ${e.message}", e)
                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "❌ Error al eliminar rutas", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }



    fun actualizarOrdenLugares(rutaId: Long, lugares: List<LugarLocal>) {
        Log.d("RutaViewModel", "\uD83D\uDCE6 Actualizando orden para rutaId=$rutaId con ${lugares.size} lugares")

        lugares.forEachIndexed { index, lugar ->
            Log.d("RutaViewModel", "\uD83D\uDD2D Pos $index: ${lugar.nombre} (${lugar.id})")
        }

        viewModelScope.launch {
            repository.actualizarOrdenLugares(rutaId, lugares)
        }
    }

    fun recargarRutaSeleccionada(rutaId: Long, onResultado: (RutaConLugaresOrdenados) -> Unit) {
        viewModelScope.launch {
            val resultado = repository.obtenerRutaConLugaresOrdenados(rutaId)
            onResultado(resultado)
        }
    }

    fun descargarRutasDesdeBackend(context: Context) {
        viewModelScope.launch {
            val usuarioId = usuarioRepository.obtenerUsuario()?.id
            val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)

            if (usuarioId != null && token != null) {
                try {
                    repository.descargarRutasDesdeBackend(usuarioId, token)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "⬇️ Rutas descargadas correctamente", Toast.LENGTH_SHORT).show()
                        Log.d("RutaViewModel", "⬇️ Rutas descargadas correctamente")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Error al descargar rutas: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("RutaViewModel", "❌ Error al descargar rutas: ${e.message}", e)
                    }
                }
            }
        }
    }

    fun subirRutasLocalesAlBackend(context: Context) {
        viewModelScope.launch {
            val usuarioId = usuarioRepository.obtenerUsuario()?.id
            val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)
            if (token != null) {
                Log.d("TOKEN_DEBUG", "Token usado para subida: ${token}")
            }


            if (usuarioId != null && token != null) {
                try {
                    repository.subirTodasLasRutasLocalesAlBackend(usuarioId, token)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "🚀 Rutas subidas al backend", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Falló subida: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.d("RutaViewModel", "❌ Error al subir rutas: ${e.message}", e)
                    }
                }
            }
        }
    }


}




























