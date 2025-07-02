package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.components

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal

import com.yucsan.mapgendafernandochang2025.screen.offLine.componentes.setupMapOffline
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel

import kotlinx.coroutines.launch

@Composable
fun MapaOfflineRutaBaseView(
    lugares: List<LugarLocal>,
    filtrosActivos: List<String>,
    distancia: Float,
    mapViewModel: MapViewModel,
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    onUbicacionSeleccionada: (LatLng) -> Unit,
    onLugarSeleccionado: (LugarLocal) -> Unit,
    onZonaBaseSeleccionada: (LatLng) -> Unit,
    isModoAgregarLugar: Boolean,
    isModoCrearRuta: Boolean,
    lugaresSeleccionadosParaRuta: SnapshotStateList<LugarLocal>,
    onAgregarLugarClick: (LatLng) -> Unit,
    markerZonaBase: MutableState<Marker?>,
    modifier: Modifier = Modifier,
    onMapReady: (GoogleMap) -> Unit,
    ubicacionInicial: LatLng? = null // NUEVO parámetro
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lugaresFiltrados = remember(lugares, filtrosActivos) {
        lugares.filter { filtrosActivos.isEmpty() || filtrosActivos.contains(it.subcategoria) }
    }

    val modoAgregarLugar by rememberUpdatedState(isModoAgregarLugar)
    val modoCrearRuta by rememberUpdatedState(isModoCrearRuta)

    AndroidView(factory = {
        MapView(it).apply {
            onCreate(null)
            onResume()
            MapsInitializer.initialize(it)

            getMapAsync { map ->
                scope.launch {
                    setupMapOffline(context, map, lugaresFiltrados, distancia)
                }

                map.setOnMapClickListener { latLng ->
                    if (!modoAgregarLugar && !modoCrearRuta) {
                        markerZonaBase.value?.remove()
                        markerZonaBase.value = map.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Zona seleccionada")
                        )
                        onZonaBaseSeleccionada(latLng)
                        toast(context, "📍 Zona base seleccionada: ${latLng.latitude.format(6)}, ${latLng.longitude.format(6)}")
                    }

                    if (modoAgregarLugar) {
                        onAgregarLugarClick(latLng)
                    }
                }

                map.setOnMarkerClickListener { marker ->
                    val lugar = lugares.find { it.nombre == marker.title }
                    if (lugar != null) {
                        if (modoCrearRuta) {
                            if (!lugaresSeleccionadosParaRuta.contains(lugar)) {
                                lugaresSeleccionadosParaRuta.add(lugar)
                            }
                        } else {
                            onLugarSeleccionado(lugar)
                        }
                    }
                    true
                }

                // NUEVO: Prioriza ubicacionInicial
                if (ubicacionInicial != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 15f))
                } else {
                    mapViewModel.camaraGuardada?.let {
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
                    }
                }

                onMapReady(map)
            }
        }
    }, modifier = modifier)
}

fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)
