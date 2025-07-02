package com.yucsan.mapgendafernandochang2025.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import java.io.File
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.yucsan.aventurafernandochang2025.room.DatabaseProvider
import com.yucsan.mapgendafernandochang2025.dto.toEntity


import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.repository.LugarRepository
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places.PlacesService
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import com.yucsan.mapgendafernandochang2025.util.CategoriaMapper
import com.yucsan.mapgendafernandochang2025.util.categoriasPersonalizadas
import kotlinx.coroutines.withContext
import com.yucsan.mapgendafernandochang2025.util.haversineDistance
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.util.config.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import okhttp3.Request




class LugarViewModel(
    application: Application,
    private val authViewModel: AuthViewModel,
    private val usuarioViewModel: UsuarioViewModel,
    private val ubicacionViewModel: UbicacionViewModel
) : AndroidViewModel(application) {


//lugares por ZONA
    private val _lugaresPorZona = MutableStateFlow<Map<String, List<LugarLocal>>>(emptyMap())
    val lugaresPorZona: StateFlow<Map<String, List<LugarLocal>>> = _lugaresPorZona

    private val _centrosPorZona = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap())
    val centrosPorZona: StateFlow<Map<String, Pair<Double, Double>>> = _centrosPorZona


    // RUTAS
    private val _lugaresSeleccionadosParaRuta = mutableStateListOf<LugarLocal>()
    val lugaresSeleccionadosParaRuta: List<LugarLocal> get() = _lugaresSeleccionadosParaRuta
//---

    private val _ultimaCategoriaAgregada = MutableStateFlow<String?>(null)
    val ultimaCategoriaAgregada: StateFlow<String?> = _ultimaCategoriaAgregada

    private val _ultimaSubcategoriaAgregada = MutableStateFlow<String?>(null)
    val ultimaSubcategoriaAgregada: StateFlow<String?> = _ultimaSubcategoriaAgregada
//---

    private val _ultimoLugarAgregadoId = MutableStateFlow<String?>(null)
    val ultimoLugarAgregadoId: StateFlow<String?> = _ultimoLugarAgregadoId

    private val _lugaresFiltrados = MutableStateFlow<List<LugarLocal>>(emptyList())
    val lugaresFiltrados: StateFlow<List<LugarLocal>> = _lugaresFiltrados.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val placesService = PlacesService()
    private val dao = DatabaseProvider.getDatabase(application).lugarDao()
    private val repository = LugarRepository(dao)

    private val _categoriasSeleccionadas = MutableStateFlow<List<String>>(emptyList())
    val categoriasSeleccionadas: StateFlow<List<String>> = _categoriasSeleccionadas.asStateFlow()
    private val _ubicacion = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _radio = MutableStateFlow(2000f)

    private val _lugares = MutableStateFlow<List<LugarLocal>>(emptyList())
    val lugares: StateFlow<List<LugarLocal>> = _lugares.asStateFlow()

    private val _todosLosLugares = MutableStateFlow<List<LugarLocal>>(emptyList())
    val todosLosLugares: StateFlow<List<LugarLocal>> = _todosLosLugares.asStateFlow()

    val distanciaSeleccionada: StateFlow<Float> get() = _radio.asStateFlow()
    val ubicacion: StateFlow<Pair<Double, Double>?> get() = _ubicacion.asStateFlow()

    // conteo de lugares por categoria
    private val _conteoPorCategoria = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorCategoria: StateFlow<Map<String, Int>> = _conteoPorCategoria

    // 🔒 SharedPreferences para control de descarga
    private fun marcarDescargaBaseComoCompleta(context: Context) {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("descarga_base_ok", true).apply()
    }

    private fun fueDescargaBaseCompletada(context: Context): Boolean {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("descarga_base_ok", false)
    }

    private val _conteoPorSubcategoria = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorSubcategoria: StateFlow<Map<String, Int>> = _conteoPorSubcategoria

    private val _conteoPorSubcategoriaFiltrado = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorSubcategoriaFiltrado: StateFlow<Map<String, Int>> = _conteoPorSubcategoriaFiltrado

    private val _filtrosActivos = MutableStateFlow<List<String>>(emptyList())
    val filtrosActivos: StateFlow<List<String>> = _filtrosActivos


    // desacargas personalizadas flags
    private val _eventoDescargaPersonalizada = MutableStateFlow(false)
    val eventoDescargaPersonalizada: StateFlow<Boolean> = _eventoDescargaPersonalizada

    // Nueva propiedad para la ubicación seleccionada
    private val _ubicacionSeleccionada = MutableStateFlow<UbicacionLocal?>(null)
    val ubicacionSeleccionada: StateFlow<UbicacionLocal?> = _ubicacionSeleccionada.asStateFlow()

    private val _categoriasExpandibles = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val categoriasExpandibles: StateFlow<Map<String, Boolean>> = _categoriasExpandibles

    fun actualizarFiltrosActivos(subcategorias: Set<String>) {
        _filtrosActivos.value = subcategorias.toList()
    }

    fun cargarConteoSubcategorias() {
        viewModelScope.launch {
            _conteoPorSubcategoria.value = repository.contarLugaresPorSubcategoria()
            
            // También actualizar el conteo filtrado si hay ubicación
            _ubicacion.value?.let { (lat, lng) ->
                val conteo = repository.contarLugaresPorSubcategoriaFiltrando(
                    latitud = lat,
                    longitud = lng,
                    radio = _radio.value
                )
                _conteoPorSubcategoriaFiltrado.value = conteo
            }
        }
    }

    fun cargarConteoSubcategoriasSoloDelUsuario() {
        viewModelScope.launch {
            val usuarioId = obtenerUsuarioId()
            val ubicacion = _ubicacion.value

            if (usuarioId.isNullOrBlank() || ubicacion == null) {
                Log.w("ConteoUsuario", "⚠️ Falta usuario o ubicación para conteo personalizado")
                return@launch
            }

            try {
                val conteo = repository.contarLugaresPorSubcategoriaFiltrandoDelUsuario(
                    usuarioId = usuarioId,
                    latitud = ubicacion.first,
                    longitud = ubicacion.second,
                    radio = _radio.value
                )
                _conteoPorSubcategoriaFiltrado.value = conteo
                Log.d("ConteoUsuario", "✅ Conteo personalizado actualizado (${conteo.size})")
            } catch (e: Exception) {
                Log.e("ConteoUsuario", "❌ Error en conteo filtrado del usuario", e)
            }
        }
    }

    fun iniciarFiltradoSoloDelUsuario() {
        viewModelScope.launch {
            combine(
                _categoriasSeleccionadas,
                _ubicacion.filterNotNull(),
                _radio
            ) { categorias, ubicacion, radio ->
                Triple(categorias, ubicacion, radio)
            }.flatMapLatest { (categorias, ubicacion, radio) ->
                val usuarioId = obtenerUsuarioId()

                if (usuarioId.isNullOrBlank() || categorias.isEmpty()) {
                    Log.w("FiltroUsuario", "⚠️ Faltan datos para filtrar: usuario=$usuarioId, categorias=$categorias")
                    return@flatMapLatest flowOf(emptyList())
                }

                Log.d("FiltroUsuario", "🧭 Filtrando solo lugares del usuario $usuarioId")

                repository.obtenerPorSubcategoriasDelUsuarioFiltrando(
                    subcategorias = categorias,
                    usuarioId = usuarioId,
                    latitud = ubicacion.first,
                    longitud = ubicacion.second,
                    radio = radio
                )
            }.collect { lugares ->
                Log.d("FiltroUsuario", "✅ Lugares del usuario filtrados: ${lugares.size}")
                _lugares.value = lugares
            }
        }
    }



    fun obtenerUsuarioId(): String? {
        val estado = authViewModel.authState.value
        return if (estado is AuthState.Autenticado) {
            estado.usuario.id.toString()
        } else null
    }


    init {
        viewModelScope.launch {
            combine(
                _categoriasSeleccionadas,
                _ubicacion.filterNotNull(),
                _radio
            ) { categorias, ubicacion, radio ->

                Log.d("DEBUG_COMBINE", "🔄 Combine activado con:")
                Log.d("DEBUG_COMBINE", "📍 Ubicación: $ubicacion")
                Log.d("DEBUG_COMBINE", "🗂️ Categorías: $categorias")
                Log.d("DEBUG_COMBINE", "📏 Radio: $radio")

                Triple(categorias, ubicacion, radio)
            }.flatMapLatest { (categorias, ubicacion, radio) ->

                val usuarioId = obtenerUsuarioId()
                if (usuarioId.isNullOrBlank() || categorias.isEmpty()) {
                    Log.w("FiltroUsuario", "⚠️ Faltan datos: usuario=$usuarioId, categorias=$categorias")
                    return@flatMapLatest flowOf(emptyList())
                }

                Log.d("FiltroUsuario", "🧭 Filtrando solo lugares del usuario $usuarioId")

                repository.obtenerPorSubcategoriasDelUsuarioFiltrando(
                    subcategorias = categorias,
                    usuarioId = usuarioId,
                    latitud = ubicacion.first,
                    longitud = ubicacion.second,
                    radio = radio
                )
            }.collect {
                Log.d("LugarViewModel", "📥 Recibidos ${it.size} lugares desde DB")
                _lugares.value = it

                it.forEach { lugar ->
                    Log.d(
                        "lugares",
                        "📍${lugar.id} ${lugar.nombre} en ${lugar.latitud},${lugar.longitud}"
                    )
                }
            }

        }

        // Escucha cambios en filtros activos
        viewModelScope.launch {
            combine(_lugares, _filtrosActivos) { lugares, filtros ->
                Log.d("DEBUG_FILTER", "🧪 Filtros activos: $filtros")
                Log.d("DEBUG_FILTER", "📦 Lugares previos: ${lugares.size}")
                lugares.filter { it.subcategoria in filtros }
            }.collect {
                Log.d("DEBUG_FILTER", "✅ lugaresFiltrados emitido: ${it.size}")
                _lugaresFiltrados.value = it
            }
        }
    }

    private fun actualizarFiltradoInterno() {
        val filtros = _filtrosActivos.value

        Log.d("DEBUG_VIEWMODEL", "🎯 Filtros activos en ViewModel: $filtros")
        Log.d("DEBUG_VIEWMODEL", "📦 Total lugares actuales: ${_lugares.value.size}")

        _lugaresFiltrados.value = if (filtros.isEmpty()) {
            _lugares.value
        } else {
            _lugares.value.filter { lugar -> filtros.contains(lugar.subcategoria) }
        }
    }

    // AUTO UBICACION
    fun iniciarActualizacionUbicacion(context: Context) {
        if (_ubicacion.value != null) return // Ya tenemos ubicación
        val fused = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            try {
                val tienePermiso = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!tienePermiso) {
                    Log.w("LugarViewModel", "⛔ Sin permiso de ubicación")
                    return@launch
                }

                val location = fused.lastLocation.await()
                if (location != null) {
                    Log.d("LugarViewModel", "📍 Ubicación obtenida automáticamente")
                    _ubicacion.value = location.latitude to location.longitude
                } else {
                    Log.w("LugarViewModel", "⚠️ No se pudo obtener la ubicación")
                }
            } catch (e: SecurityException) {
                Log.e("LugarViewModel", "❌ Seguridad: sin permiso", e)
            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error al obtener ubicación", e)
            }
        }
    }

    fun observarTodosLosLugares() {
        viewModelScope.launch {
            repository.obtenerTodos().collect {
                _todosLosLugares.value = it
                Log.d("LugarViewModel", "📦 Todos los lugares: ${it.size}")
            }
        }
    }

    fun actualizarCategorias(nuevas: Set<String>) {
        Log.d("LugarViewModel", "✏️ Categorías actualizadas: $nuevas")

        // Solo actualiza si cambia el contenido (fuerza emisión manual si igual)
        if (_categoriasSeleccionadas.value.toSet() != nuevas) {
            _categoriasSeleccionadas.value = nuevas.toList()
        } else {
            // Forzar nueva emisión para reactivar el combine
            _categoriasSeleccionadas.value = emptyList()
            _categoriasSeleccionadas.value = nuevas.toList()
        }
    }


    fun actualizarUbicacion(latitud: Double, longitud: Double, ubicacion: UbicacionLocal? = null) {
        _ubicacion.value = latitud to longitud
        _ubicacionSeleccionada.value = ubicacion
        
        // Actualizar el conteo filtrado por ubicación
        viewModelScope.launch {
            val conteo = repository.contarLugaresPorSubcategoriaFiltrando(
                latitud = latitud,
                longitud = longitud,
                radio = _radio.value
            )
            _conteoPorSubcategoriaFiltrado.value = conteo
        }
    }

    fun actualizarRadio(nuevoRadio: Float) {
        Log.d("LugarViewModel", "📏 Radio actualizado: $nuevoRadio")
        _radio.value = nuevoRadio
    }

    fun limpiarLugares() {
        viewModelScope.launch {
            Log.d("LugarViewModel", "🧹 Limpiando lugares...")
            repository.limpiarTodo()
        }
    }


    fun generarPuntosAlrededor(
        centroLat: Double,
        centroLng: Double,
        cantidadPorLado: Int = 1,
        separacionGrados: Double = 0.02 // Aproximadamente 2.2 km
    ): List<Pair<Double, Double>> {
        val puntos = mutableListOf<Pair<Double, Double>>()

        for (latOffset in -cantidadPorLado..cantidadPorLado) {
            for (lngOffset in -cantidadPorLado..cantidadPorLado) {
                val nuevaLat = centroLat + (latOffset * separacionGrados)
                val nuevaLng = centroLng + (lngOffset * separacionGrados)
                puntos.add(nuevaLat to nuevaLng)
            }
        }
        return puntos
    }

    fun cargarConteoPorCategoria() {
        viewModelScope.launch {
            _conteoPorCategoria.value = repository.contarLugaresPorCategoria()
        }
    }


    fun fueBusquedaInicialHecha(context: Context): Boolean {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("busqueda_inicial_hecha", false)
    }

    fun marcarBusquedaInicial(context: Context) {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("busqueda_inicial_hecha", true).apply()
    }


// ***********  FUNCIONALIDADES CRUD  *********** //

    private val _estadoGuardado = MutableLiveData<Boolean>()
    val estadoGuardado: LiveData<Boolean> get() = _estadoGuardado

    fun agregarLugar(lugar: LugarLocal) {
        viewModelScope.launch {

            val usuarioId = obtenerUsuarioId()

            if (usuarioId == null) {
                Log.e("AGREGAR_LUGAR", "❌ No se puede guardar: usuario no autenticado.")
                _estadoGuardado.value = false
                return@launch
            }

            val lugarConUsuario = lugar.copy(usuarioId = usuarioId) // agregamos usuarioId

            try {
                repository.insertarLugar(lugarConUsuario)
                _estadoGuardado.value = true
                _ultimoLugarAgregadoId.value = lugar.id

                _ultimoLugarAgregadoId.value = lugar.id
                _ultimaCategoriaAgregada.value = lugar.categoriaGeneral
                _ultimaSubcategoriaAgregada.value = lugar.subcategoria

                Log.d("AGREGAR_LUGAR", "✅ Lugar agregado: ${lugar.nombre} - ${lugar.subcategoria} @ ${lugar.latitud}, ${lugar.longitud}")

            } catch (e: Exception) {
                Log.e("AGREGAR_LUGAR", "❌ Error al guardar", e)
                _estadoGuardado.value = false
            }
        }
    }

    fun recargarLugares() {
        viewModelScope.launch {
            _cargando.value = true
            val nuevosLugares = repository.obtenerTodos().first()
            _lugares.value = nuevosLugares
            _cargando.value = false
        }
    }


    // justo después de _ultimoLugarAgregadoId
    fun resetUltimoLugarAgregado() {
        _ultimoLugarAgregadoId.value = null
    }



    fun insertarLugares(lugares: List<LugarLocal>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertarLugares(lugares)
            Log.d("INSERTADOS NUEVOS", "✅ Lugares insertados: ${lugares.size}")
        }
    }

    fun actualizarLugarManual(lugar: LugarLocal) {
        viewModelScope.launch {
            repository.actualizarLugar(lugar)
            Log.d("ACTUALIZA NUEVOS", "✅ Lugares EDITADOS: ${lugar.nombre} ${lugar.id}")
        }
    }

    fun eliminarLugar(id: String) {
        viewModelScope.launch {
            repository.eliminarLugarPorId(id)

            Log.d("eliminado", "Lugar eliminado: $id")
        }
    }


    fun actualizarFotoLugar(idLugar: String, nuevaUrl: String, jwt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("fotoUrl", nuevaUrl)
                }

                val body = RequestBody.create(
                    "application/json; charset=utf-8".toMediaType(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}lugares/$idLugar/actualizar-foto")
                    .put(body)
                    .addHeader("Authorization", "Bearer $jwt")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("LugarViewModel", "✅ Foto actualizada correctamente para lugar $idLugar")
                } else {
                    val error = response.body?.string()
                    Log.e("LugarViewModel", "⚠️ Error al actualizar foto: $error")
                }

            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Excepción al actualizar foto: ${e.message}", e)
            }
        }
    }

    fun actualizarLugarLocal(lugar: LugarLocal) {
        viewModelScope.launch {
            try {
                repository.actualizarLugar(lugar)
                Log.d("LugarViewModel", "✅ Lugar actualizado localmente: ${lugar.nombre}")
                // Forzar recarga de lugares
                recargarLugares()
            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error al actualizar lugar localmente: ${e.message}", e)
            }
        }
    }



    //**************** FUNCIONALIDADES BACKEND *****************//


    // subida
    fun sincronizarLugaresConApi() {
        viewModelScope.launch {
            val todos = dao.obtenerTodos().first()
            val usuarioId = obtenerUsuarioId()

            Log.d("SYNC_ANDROID", "📤 Lugares a sincronizar: ${todos.size}")
            todos.forEach {
                Log.d("SYNC_ANDROID", "📍 ${it.nombre} - ${it.latitud}, ${it.longitud}")
            }

            if (usuarioId != null) {
                repository.sincronizarConBackend(todos, usuarioId)
            }
        }
    }

    // descarga
    fun descargarLugaresDesdeBackend(context: Context) {
        viewModelScope.launch {
            try {
                val usuario = usuarioViewModel.obtenerUsuario()
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("jwt_token", null)

                if (usuario != null && token != null) {
                    RetrofitInstance.setTokenProvider { token }

                    val lugaresDesdeApi = RetrofitInstance.lugarApi.obtenerLugaresDelUsuario(usuario.id.toString())

                    // Guardar en la base de datos local
                    val lugaresLocal = lugaresDesdeApi.map { dto -> dto.toEntity() }
                    dao.insertarLugares(lugaresLocal)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Lugares descargados correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "⚠️ Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.d("LugarViewModel", "❌ Error al descargar lugares desde backend \"${e.message}\"")
                }
            }
        }
    }

    //FUNCIONES RUTAS ---

    // Agrega un lugar a la lista de selección
    fun agregarLugarARuta(lugar: LugarLocal) {
        if (!_lugaresSeleccionadosParaRuta.contains(lugar)) {
            _lugaresSeleccionadosParaRuta.add(lugar)
        }
    }

    // Elimina un lugar de la lista de selección
    fun eliminarLugarDeRuta(lugar: LugarLocal) {
        _lugaresSeleccionadosParaRuta.remove(lugar)
    }

    // Limpia la selección completa
    fun limpiarRuta() {
        _lugaresSeleccionadosParaRuta.clear()
    }

// descargas personalizadas ********************************************** esta es la funcion que estamos usando

    fun descargarLugaresPorSubcategoriasPersonalizadas(
        context: Context,
        subcategorias: Set<String>,
        apiKey: String,
        cantidadPorLado: Int = 0,
        radioMetros: Float = 3000f
    ) {
        viewModelScope.launch {

            val usuarioId = obtenerUsuarioId()
            if (usuarioId == null) {
                Log.e("LugarViewModel", "❌ No se puede descargar: usuario no autenticado.")
                Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                _cargando.value = false
                return@launch
            }

            val ubicacionActual = _ubicacion.value
            Log.d("LugarViewModel", "📍 Centro de descarga: ${ubicacionActual?.first}, ${ubicacionActual?.second}")

            if (ubicacionActual == null) {
                Log.w("LugarViewModel", "❗ Ubicación no disponible para descarga personalizada.")
                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _cargando.value = true

            val puntos = generarPuntosAlrededor(
                centroLat = ubicacionActual.first,
                centroLng = ubicacionActual.second,
                cantidadPorLado = cantidadPorLado
            ).toMutableList()

            val todosDescargados = mutableListOf<LugarLocal>()
            val todosFiltrados = mutableListOf<LugarLocal>()
            val todosGuardados = mutableListOf<LugarLocal>()

            Log.d("LugarViewModel", "🧭 Iniciando descarga personalizada con ${subcategorias.size} subcategorías")

            try {
                for ((lat, lng) in puntos) {
                    for (subtipo in subcategorias) {
                        try {
                            val lugares = placesService.obtenerLugaresCercanos(
                                lat = lat,
                                lng = lng,
                                apiKey = apiKey,
                                type = subtipo,
                                maxDistancia = radioMetros
                            )

                            Log.d("LugarViewModel", "🔍 Subtipo: $subtipo → Resultado: ${lugares.size} lugares")

                            val mapeados = lugares.map { lugar ->
                                lugar.copy(
                                    subcategoria = subtipo,
                                    photoReference = lugar.photoReference,
                                    businessStatus = lugar.businessStatus,
                                    userRatingsTotal = lugar.userRatingsTotal,
                                    abiertoAhora = lugar.abiertoAhora,
                                    usuarioId = usuarioId
                                )
                            }

                            todosDescargados.addAll(mapeados)

                            val filtrados = mapeados.filter {
                                it.photoReference != null &&
                                        (it.rating ?: 0f) >= 3.0f &&
                                        (it.userRatingsTotal ?: 0) >= 5
                            }

                            todosFiltrados.addAll(filtrados)

                            val unicos = filtrados.distinctBy { "${it.nombre}-${it.latitud}-${it.longitud}" }
                            repository.insertarLugares(unicos)
                            todosGuardados.addAll(unicos)

                            Log.d("LugarViewModel", "✅ Insertados ${unicos.size} lugares para subtipo $subtipo")

                        } catch (e: Exception) {
                            Log.e("LugarViewModel", "❌ Error en subtipo $subtipo", e)
                        }
                    }
                }

                Toast.makeText(context, "✅ Lugares descargados correctamente", Toast.LENGTH_SHORT).show()

                _eventoDescargaPersonalizada.value = true


            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error general en descarga personalizada", e)
                Toast.makeText(context, "Error en descarga personalizada", Toast.LENGTH_SHORT).show()
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetearEventoDescargaPersonalizada() {
        _eventoDescargaPersonalizada.value = false
    }

    fun actualizarUbicacionManual(latLng: LatLng) {
        _ubicacion.value = latLng.latitude to latLng.longitude
    }


    fun agruparLugaresPorZona(cantidadPorLado: Int = 1, separacionGrados: Double = 0.02) {
        val ubicacionCentral = _ubicacion.value
        val todos = _todosLosLugares.value

        if (ubicacionCentral == null || todos.isEmpty()) {
            Log.w("ZONAS", "⛔ Sin ubicación o sin lugares. Ubicación: $ubicacionCentral, Lugares: ${todos.size}")
            return
        }

        val zonas = mutableMapOf<String, MutableList<LugarLocal>>()
        val centroLat = ubicacionCentral.first
        val centroLng = ubicacionCentral.second

        // Genera un mapa de zonas con coordenadas de cuadrícula
        val zonasOrdenadas = mutableListOf<Pair<Int, Int>>()
        for (i in -cantidadPorLado..cantidadPorLado) {
            for (j in -cantidadPorLado..cantidadPorLado) {
                zonasOrdenadas.add(i to j)
            }
        }

        val indexPorZona = zonasOrdenadas.mapIndexed { index, par -> par to "Zona ${index + 1}" }.toMap()

        Log.d("ZONAS", "📌 Index por zona:")
        indexPorZona.forEach { (offset, nombre) ->
            Log.d("ZONAS", "🔢 $nombre ← Offset $offset")
        }

        // Calcula centros
        val centros = indexPorZona.entries.associate { (offset, zonaNombre) ->
            val (latOffset, lngOffset) = offset
            val lat = centroLat + latOffset * separacionGrados
            val lng = centroLng + lngOffset * separacionGrados
            zonaNombre to (lat to lng)
        }

        _centrosPorZona.value = centros

        Log.d("ZONAS", "📍 Centros por zona:")
        centros.forEach { (zona, centro) ->
            Log.d("ZONAS", "🌐 $zona en ${centro.first}, ${centro.second}")
        }

        // Agrupa los lugares en la zona más cercana
        for (lugar in todos) {
            val latOffset = ((lugar.latitud - centroLat) / separacionGrados).toInt().coerceIn(-cantidadPorLado, cantidadPorLado)
            val lngOffset = ((lugar.longitud - centroLng) / separacionGrados).toInt().coerceIn(-cantidadPorLado, cantidadPorLado)
            val clave = indexPorZona[latOffset to lngOffset]

            if (clave == null) {
                Log.w("ZONAS", "⚠️ Lugar fuera de zona válida: ${lugar.nombre}")
                continue
            }

            zonas.getOrPut(clave) { mutableListOf() }.add(lugar)
            Log.d("ZONAS", "✅ ${lugar.nombre} → $clave")
        }

        _lugaresPorZona.value = zonas
        Log.d("ZONAS", "🎯 Total zonas con lugares: ${zonas.size}")
    }

    fun agruparLugaresPorZonaGeografica2(radioKm: Double = 8.0) {
        val lugaresPendientes = _todosLosLugares.value.toMutableList()
        val zonasAgrupadas = mutableMapOf<String, MutableList<LugarLocal>>()
        val centrosZona = mutableMapOf<String, Pair<Double, Double>>()

        var contadorZona = 1

        while (lugaresPendientes.isNotEmpty()) {
            val lugarBase = lugaresPendientes.removeAt(0)
            val centroLat = lugarBase.latitud
            val centroLon = lugarBase.longitud

            val nombreZona = "Zona $contadorZona"
            val lugaresEnZona = mutableListOf<LugarLocal>()
            lugaresEnZona.add(lugarBase)

            val iterator = lugaresPendientes.iterator()
            while (iterator.hasNext()) {
                val lugar = iterator.next()
                val distancia = haversineDistance(
                    centroLat, centroLon,
                    lugar.latitud, lugar.longitud
                )

                if (distancia <= radioKm) {
                    lugaresEnZona.add(lugar)
                    iterator.remove()
                }
            }

            zonasAgrupadas[nombreZona] = lugaresEnZona
            centrosZona[nombreZona] = centroLat to centroLon
            Log.d("ZONAS", "🔢 $nombreZona → Centro: ($centroLat, $centroLon) con ${lugaresEnZona.size} lugares")

            contadorZona++
        }

        _lugaresPorZona.value = zonasAgrupadas
        _centrosPorZona.value = centrosZona
    }


    fun actualizarCategoriasExpandibles(categorias: Map<String, Boolean>) {
        _categoriasExpandibles.value = categorias
    }

    fun resetearEstado() {
        _categoriasSeleccionadas.value = emptyList()
        _filtrosActivos.value = emptyList()
        _ubicacion.value = null
        _ubicacionSeleccionada.value = null
        _radio.value = 2000f
        _categoriasExpandibles.value = emptyMap()
        _lugaresFiltrados.value = emptyList()
    }




}