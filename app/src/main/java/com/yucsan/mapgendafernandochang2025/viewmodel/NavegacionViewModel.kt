package com.yucsan.mapgendafernandochang2025.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.directions.DirectionsService
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.directions.PasoRuta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavegacionViewModel(application: Application) : AndroidViewModel(application) {

    private val directionsService = DirectionsService()
    private val directionsApi = directionsService.api

    private val _pasosRuta = MutableStateFlow<List<PasoRuta>>(emptyList())
    val pasosRuta: StateFlow<List<PasoRuta>> = _pasosRuta.asStateFlow()

    private val _ubicacionActual = MutableStateFlow<Pair<Double, Double>?>(null)
    val ubicacionActual: StateFlow<Pair<Double, Double>?> = _ubicacionActual.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    private val _indicePasoActual = MutableStateFlow(0)
    val indicePasoActual: StateFlow<Int> = _indicePasoActual.asStateFlow()

    fun actualizarPasosRuta(nuevos: List<PasoRuta>) {
        _pasosRuta.value = nuevos
        _indicePasoActual.value = 0
        Log.d("NavegacionVM", "📝 Pasos actualizados (${nuevos.size})")
    }

    fun obtenerRutaConPasos(
        origen: Pair<Double, Double>,
        destino: Pair<Double, Double>,
        modo: String = "walking",
        apiKey: String,
        onRuta: (List<LatLng>, List<PasoRuta>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val originStr = "${origen.first},${origen.second}"
                val destStr = "${destino.first},${destino.second}"
                val respuesta = directionsApi.obtenerRuta(
                    origin = originStr,
                    destination = destStr,
                    mode = modo,
                    language = "es",
                    apiKey = apiKey
                )

                val polyline = respuesta.routes.firstOrNull()
                    ?.overview_polyline?.points
                    ?.let { decodePolyline(it) }
                    ?: emptyList()

                val pasos = respuesta.routes.firstOrNull()
                    ?.legs?.flatMap { it.steps }
                    ?.mapNotNull { step ->
                        val lat = step.start_location?.lat ?: return@mapNotNull null
                        val lng = step.start_location.lng
                        val texto = step.html_instructions?.replace(Regex("<.*?>"), "") ?: ""
                        PasoRuta(instruccion = texto, lat = lat, lng = lng)
                    } ?: emptyList()

                onRuta(polyline, pasos)
            } catch (e: Exception) {
                Log.e("NavegacionVM", "❌ Error obteniendo ruta con pasos", e)
                onRuta(emptyList(), emptyList())
            }
        }
    }

    fun obtenerRutaHaciaDestino(
        origen: Pair<Double, Double>,
        destino: Pair<Double, Double>,
        modo: String = "walking",
        apiKey: String,
        onResultado: (List<LatLng>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val originStr = "${origen.first},${origen.second}"
                val destStr = "${destino.first},${destino.second}"

                val respuesta = directionsApi.obtenerRuta(
                    origin = originStr,
                    destination = destStr,
                    mode = modo,
                    apiKey = apiKey
                )

                val puntos = respuesta.routes.firstOrNull()
                    ?.overview_polyline?.points
                    ?.let { decodePolyline(it) }
                    ?: emptyList()

                onResultado(puntos)
            } catch (e: Exception) {
                Log.e("NavegacionVM", "❌ Error ruta simple", e)
                onResultado(emptyList())
            }
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val point = LatLng(lat / 1E5, lng / 1E5)
            poly.add(point)
        }
        return poly
    }

    // Ubicación en tiempo real
    @SuppressLint("MissingPermission")
    fun iniciarUbicacionTiempoReal(context: Context) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            Log.w("NavVM", "⛔ Sin permiso de ubicación")
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(10f)
            .build()

        fusedClient.requestLocationUpdates(
            request,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    _ubicacionActual.value = loc.latitude to loc.longitude
                    Log.d("NavVM", "📍 Nueva ubicación: ${loc.latitude}, ${loc.longitude}")
                }
            },
            Looper.getMainLooper()
        )
    }

    // Verificación de cercanía al paso actual
    fun verificarProximidadYAvanzar(
        toleranciaMetros: Float = 50f,
        onNuevoPaso: (PasoRuta) -> Unit,
        onFueraDeRuta: (() -> Unit)? = null,
        onRutaFinalizada: (() -> Unit)? = null
    ) {
        val pasos = _pasosRuta.value
        val ubicacion = _ubicacionActual.value ?: return
        val actual = _indicePasoActual.value

        if (actual >= pasos.size) return

        val maxPasosAdelante = 5
        val siguientePasos = pasos.drop(actual).take(maxPasosAdelante)

        val pasoMasCercano = siguientePasos
            .mapIndexed { index, paso ->
                val distancia = calcularDistanciaMetros(ubicacion, paso.lat to paso.lng)
                Triple(index + actual, paso, distancia)
            }
            .minByOrNull { it.third }

        pasoMasCercano?.let { (nuevoIndice, paso, distancia) ->
            if (distancia < toleranciaMetros && nuevoIndice > actual) {
                Log.d("NavVM", "📣 Saltando al paso ${nuevoIndice + 1}: ${paso.instruccion}")
                _indicePasoActual.value = nuevoIndice + 1
                onNuevoPaso(paso)
            } else if (distancia < toleranciaMetros && nuevoIndice == actual) {
                Log.d("NavVM", "📣 Cerca del paso actual ${actual + 1}: ${paso.instruccion}")
                _indicePasoActual.value = actual + 1
                onNuevoPaso(paso)
            } else if (distancia > 150f) {
                Log.w("NavVM", "⚠️ Fuera de ruta: distancia mínima encontrada $distancia m")
                onFueraDeRuta?.invoke()
            }
        }
        if (_indicePasoActual.value >= _pasosRuta.value.size) {
            Log.i("NavVM", "✅ Ruta completada")
            onRutaFinalizada?.invoke()
        }

    }



    fun calcularDistanciaMetros(a: Pair<Double, Double>, b: Pair<Double, Double>): Float {
        val resultado = FloatArray(1)
        android.location.Location.distanceBetween(
            a.first, a.second, b.first, b.second, resultado
        )
        return resultado[0]
    }
}
