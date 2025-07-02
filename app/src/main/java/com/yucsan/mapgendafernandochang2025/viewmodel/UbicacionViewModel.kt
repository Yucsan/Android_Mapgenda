package com.yucsan.mapgendafernandochang2025.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.repository.UbicacionRepository
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.pow
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineStart
import androidx.annotation.OptIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class UbicacionViewModel @OptIn(UnstableApi::class) constructor
    (
    application: Application,
    private val repository: UbicacionRepository,
    private val usuarioRepository: UsuarioRepository
) : AndroidViewModel(application) {

    /*────────────────────────────────────────  NUEVO  ────────────────────────────────────────*/
    sealed interface EstadoUbicacion {
        object SinPermiso : EstadoUbicacion
        object EsperandoFix : EstadoUbicacion
        data class Disponible(val l: Location) : EstadoUbicacion
    }

    /** Flujo principal que la UI debe observar */
    private val _estado = MutableStateFlow<EstadoUbicacion>(EstadoUbicacion.SinPermiso)
    val estado: StateFlow<EstadoUbicacion> = _estado.asStateFlow()

    /** Alias legacy para el resto de tu código que esperaba Pair<Double,Double>? */
    val ubicacionActual: StateFlow<Pair<Double,Double>?> =
        estado.map { est ->
            (est as? EstadoUbicacion.Disponible)?.l?.let { it.latitude to it.longitude }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val fused = LocationServices.getFusedLocationProviderClient(application)
    /*──────────────────────────────────────────────────────────────────────────────────────────*/

    /*──────────────────────────  RESTO DE PROPIEDADES SIN CAMBIO  ───────────────────────────*/
    private val _ubicaciones = MutableStateFlow<List<UbicacionLocal>>(emptyList())
    val ubicaciones: StateFlow<List<UbicacionLocal>> = _ubicaciones.asStateFlow()

    private val _ubicacionesPorUsuario = MutableStateFlow<List<UbicacionLocal>>(emptyList())
    val ubicacionesPorUsuario: StateFlow<List<UbicacionLocal>> = _ubicacionesPorUsuario.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _usuarioId = MutableStateFlow<String?>(null)
    val usuarioId: StateFlow<String?> = _usuarioId.asStateFlow()


    /*──────────────────────────────────────────────────────────────────────────────────────────*/

    init {
        // 🔄 1. Escuchar el flujo de usuarioActivo del repositorio
        viewModelScope.launch {
            usuarioRepository.usuarioActivo
                .filterNotNull()
                .distinctUntilChanged()
                .collect {
                    _usuarioId.value = it
                    Log.d("USUARIO_ACTIVO_VM", "usuarioId actualizado desde repo: $it")
                }
        }

        // 🧠 2. Filtrar ubicaciones por usuario
        viewModelScope.launch {
            usuarioId
                .filterNotNull()
                .flatMapLatest { id -> repository.obtenerUbicacionesPorUsuarioId(id) }
                .collect { _ubicacionesPorUsuario.value = it }
        }

        // 🔎 3. Buscar todas o por texto
        viewModelScope.launch {
            query
                .flatMapLatest { txt ->
                    if (txt.isBlank()) repository.obtenerTodas()
                    else repository.buscar(txt)
                }
                .collect { _ubicaciones.value = it }
        }
    }


    /*────────────────────────  PERMISO + OBTENCIÓN DEL FIX  ────────────────────────*/
    fun onPermisoConcedido() {
        if (_estado.value !is EstadoUbicacion.SinPermiso) return
        _estado.value = EstadoUbicacion.EsperandoFix
        pedirPrimeraUbicacion()
    }

    fun onPermisoDenegado() {
        _estado.value = EstadoUbicacion.SinPermiso
    }

    @SuppressLint("MissingPermission")
    private fun pedirPrimeraUbicacion() = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
        val ctx = getApplication<Application>()

        // 1️⃣ lastLocation
        val last = fused.lastLocation.await()
        if (last != null) {
            _estado.value = EstadoUbicacion.Disponible(last)
            return@launch
        }

        // 2️⃣ getCurrentLocation con timeout 5 s
        val actual = try {
            withTimeoutOrNull(5.seconds) {
                suspendCancellableCoroutine<Location?> { cont ->
                    fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resume(null) }
                }
            }
        } catch (_: TimeoutCancellationException) { null }

        if (actual != null) {
            _estado.value = EstadoUbicacion.Disponible(actual)
            return@launch
        }

        // 3️⃣ fallback (centro de España)
        val fallback = Location("fallback").apply {
            latitude = 40.4168
            longitude = -3.7038
        }
        _estado.value = EstadoUbicacion.Disponible(fallback)

        Toast.makeText(ctx, "No se pudo obtener tu ubicación real (se usó un valor por defecto)", Toast.LENGTH_LONG).show()
    }
    /*───────────────────────────────────────────────────────────────────────────────*/

    /*─────────────  API PÚBLICA “compat” PARA EL CÓDIGO EXISTENTE  ────────────────*/
    @OptIn(UnstableApi::class) @SuppressLint("MissingPermission")
    fun iniciarUbicacionActivaDePrueba() {
        // Mantén este helper si algún flujo tuyo lo sigue llamando -> simplemente delega:
        onPermisoConcedido()
    }

    /** Ya no se usa; mantenla vacía para compatibilidad */
    fun iniciarActualizacionUbicacion() { /* obsoleto */ }
    /*───────────────────────────────────────────────────────────────────────────────*/


    /*────────────────────────────────  CRUD & SYNC (SIN CAMBIO)  ─────────────────────────────*/
    fun actualizarQuery(nuevoTexto: String) { _query.value = nuevoTexto }

    // ── guardar ────────────────────────────────────────────────────────────────
    @OptIn(UnstableApi::class)
    suspend fun guardarUbicacion(nombre: String, lat: Double, lng: Double, tipo: String): Boolean {


        val idUsuario = usuarioRepository.obtenerIdUsuario()
        usuarioRepository.establecerUsuarioActivo(idUsuario!!)

        Log.d("usuario_ID; ", "$idUsuario")

        val nueva = UbicacionLocal(
            nombre = nombre,
            latitud = lat,
            longitud = lng,
            tipo = tipo,
            usuarioId = idUsuario  // test
        )

        Log.d("GUARDADO", "Nueva ubicación insertada: $nueva")


        val existentes = repository.obtenerUbicacionesPorUsuarioId(idUsuario).first()

        val umbral = when (tipo.trim().lowercase()) {
            "provincia" -> 8_000.0; "país" -> 100_000.0; else -> 100.0
        }

        val existeCercana = existentes.any {
            it.tipo.trim().lowercase() == tipo.trim().lowercase() &&
                    calcularDistanciaEnMetros(it.latitud, it.longitud, lat, lng) <= umbral
        }


        return if (!existeCercana) {
            repository.insertarUbicacion(nueva); true
        } else {
            Log.w("UbicacionViewModel", "🚫 Ya existe una ubicación '$tipo' cercana.")
            false
        }
    }

    private fun calcularDistanciaEnMetros(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun eliminarUbicacion(ubicacion: UbicacionLocal) = viewModelScope.launch {
        repository.eliminarUbicacion(ubicacion)
    }
    fun actualizarTipo(id: Int, nuevoTipo: String) = viewModelScope.launch {
        repository.actualizarTipo(id, nuevoTipo)
    }
    fun actualizarUbicacionCompleta(id: Int, nombre: String, tipo: String) = viewModelScope.launch {
        repository.actualizarUbicacionCompleta(id, nombre, tipo)
    }

    suspend fun guardarYRetornarId(ubicacion: UbicacionLocal): Long =
        repository.insertarUbicacionYRetornarId(ubicacion)

    fun eliminarTodasUbicaciones() = viewModelScope.launch { repository.eliminarTodas() }

    // ── sync backend ───────────────────────────────────────────────────────────
    @OptIn(UnstableApi::class)
    fun sincronizarConApi(context: Context) = viewModelScope.launch {

        val usuarioId = usuarioRepository.obtenerUsuarioSincronizado()?.id
        Log.d("usuario", "Usuario ID: $usuarioId")

        val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("jwt_token", null)

        if (usuarioId != null && token != null) {
            try {
                repository.sincronizarUbicacionesConBackend(usuarioId, token)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "📡 Ubicaciones sincronizadas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Falló sincronización: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun descargarUbicaciones(context: Context) = viewModelScope.launch {
        val usuarioId = usuarioRepository.obtenerUsuario()?.id
        val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("jwt_token", null)

        if (usuarioId != null && token != null) {
            try {
                repository.descargarUbicacionesDesdeBackend(usuarioId, token)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "⬇️ Ubicaciones descargadas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Falló descarga: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    /*─────────────────────────────────────────────────────────────────────────────*/
}
