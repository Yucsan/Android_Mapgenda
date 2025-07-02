package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel


import kotlinx.coroutines.launch

@Composable
fun MapaSeleccionUbicacionView(
    onUbicacionSeleccionada: (LatLng) -> Unit,
    ubicacionCentro: LatLng? = null,
    ubicaciones: List<UbicacionLocal>,
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var markerSeleccionado by remember { mutableStateOf<Marker?>(null) }

    AndroidView(factory = {
        MapView(it).apply {
            onCreate(null)
            onResume()
            MapsInitializer.initialize(it)
            getMapAsync { map ->
                googleMap = map
                onMapReady(map)

                map.uiSettings.isMyLocationButtonEnabled = false

                // Centrar cámara
                if (ubicacionCentro != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCentro, 15f))
                } else {
                    mapViewModel.camaraGuardada?.let {
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
                    }
                }

                map.setOnMapClickListener { latLng ->
                    markerSeleccionado?.remove()
                    markerSeleccionado = map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Ubicación seleccionada")
                    )
                    onUbicacionSeleccionada(latLng)
                    toast(context, "📍 Ubicación seleccionada: ${latLng.latitude.format(6)}, ${latLng.longitude.format(6)}")
                }
            }
        }
    }, modifier = modifier)
}

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
