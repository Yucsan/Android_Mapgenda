package com.yucsan.mapgendafernandochang2025.viewmodel


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.launch


class MapViewModel : ViewModel() {
    var camaraGuardada by mutableStateOf<CameraPosition?>(null)
        private set

    fun guardarCamara(position: CameraPosition) {
        camaraGuardada = position
        Log.d("MapViewModel", "guardarCamara: $position")
    }

    fun limpiarCamara() {
        camaraGuardada = null
    }

    fun initializeMap(map: GoogleMap) {
        viewModelScope.launch {
            try {
                // Configurar el mapa
                map.uiSettings.apply {
                    isZoomControlsEnabled = true
                    isMyLocationButtonEnabled = true
                    isCompassEnabled = true
                }
                
                // Si hay una cámara guardada, restaurarla
                camaraGuardada?.let {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
                }
                
                Log.d("MapViewModel", "✅ Mapa inicializado correctamente")
            } catch (e: Exception) {
                Log.e("MapViewModel", "❌ Error al inicializar el mapa", e)
            }
        }
    }
}
