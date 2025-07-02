package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.components

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal


import kotlinx.coroutines.launch


import com.yucsan.mapgendafernandochang2025.ui.format
import com.yucsan.mapgendafernandochang2025.util.IconosMapa
import com.yucsan.mapgendafernandochang2025.screen.offLine.componentes.setupMapOffline
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel
import com.yucsan.mapmapgendafernandochang2025.entidad.toLugarLocalParaRuta


@Composable
fun MapaOfflineView(
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
    isSoloLectura: Boolean = false, // <-- NUEVO
    lugaresSeleccionadosParaRuta: SnapshotStateList<LugarLocal>,
    onAgregarLugarClick: (LatLng) -> Unit,
    markerZonaBase: MutableState<Marker?>,
    modifier: Modifier = Modifier,
    ubicacionCentro: LatLng? = null, // ← NUEVO
    ubicaciones: List<UbicacionLocal>,
    onMapReady: (GoogleMap) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lugaresFiltrados = remember(lugares, filtrosActivos) {
        if (filtrosActivos.isEmpty()) lugares
        else lugares.filter { filtrosActivos.contains(it.subcategoria) }
    }

    val modoAgregarLugar by rememberUpdatedState(isModoAgregarLugar)
    val modoCrearRuta by rememberUpdatedState(isModoCrearRuta)

    val googleMapRef = remember { mutableStateOf<GoogleMap?>(null) }

    LaunchedEffect(
        lugaresFiltrados,
        ubicaciones,
        lugaresSeleccionadosParaRuta.toList()
    ) {
        Log.d("MAPA_UBICACIONES", "🟡 Repintando mapa con lugares=${lugaresFiltrados.size}, ubicaciones=${ubicaciones.size}")

        googleMapRef.value?.let { map ->
            map.clear()
            setupMapOffline(
                context,
                map,
                lugaresFiltrados,
                distancia,
                ubicacionCentro,
                ubicaciones,
                lugaresSeleccionadosParaRuta,
                centrarCamara = false
            )
        }
    }


    AndroidView(factory = {
        MapView(it).apply {
            onCreate(null)
            onResume()
            MapsInitializer.initialize(it)
            getMapAsync { map ->
                googleMapRef.value = map
                map.uiSettings.isMyLocationButtonEnabled = false

                scope.launch {
                    setupMapOffline(context, map, lugaresFiltrados, distancia, ubicacionCentro)
                }


                // ✅ Mostrar botón de ubicación si hay permisos

                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = false
                } else {
                    map.uiSettings.isMyLocationButtonEnabled = false
                }

                if (ubicacionCentro != null && !modoCrearRuta) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCentro, 15f))
                    Log.d("MAPA_OFFLINE", "📍 Cámara centrada en: ${ubicacionCentro.latitude}, ${ubicacionCentro.longitude}")
                    lugarRutaOfflineViewModel.actualizarUbicacionManual( ubicacionCentro)
                } else {
                    mapViewModel.camaraGuardada?.let {
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
                    }
                }

                    map.setOnMapClickListener { latLng ->
                        if (isSoloLectura) return@setOnMapClickListener

                        if (!modoAgregarLugar && !modoCrearRuta) {
                            markerZonaBase.value?.remove()
                            markerZonaBase.value = map.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("Zona seleccionada")
                            )
                            onZonaBaseSeleccionada(latLng)
                            showToast(
                                context,
                                "📍 Zona base seleccionada: ${latLng.latitude.format(6)}, ${
                                    latLng.longitude.format(6)
                                }"
                            )
                        }
                        if (modoAgregarLugar) {
                            onAgregarLugarClick(latLng)
                        }
                    }

                map.setOnMarkerClickListener { marker ->
                    if (isSoloLectura) return@setOnMarkerClickListener false

                    val lugar = marker.tag as? LugarLocal
                    val ubicacion = marker.tag as? UbicacionLocal

                    if (modoCrearRuta) {
                        when {
                            lugar != null && lugaresSeleccionadosParaRuta.none { it.id == lugar.id } -> {
                                lugaresSeleccionadosParaRuta.add(lugar)
                                marker.setIcon(IconosMapa.seleccionado(context))
                                Log.d("MAPA_RUTA", "📍 Lugar agregado: ${lugar.nombre}")
                            }
                            ubicacion != null -> {
                                val lugarDesdeUbicacion = ubicacion.toLugarLocalParaRuta()
                                if (!lugaresSeleccionadosParaRuta.any { it.id == lugarDesdeUbicacion.id }) {
                                    lugaresSeleccionadosParaRuta.add(lugarDesdeUbicacion)
                                    marker.setIcon(IconosMapa.seleccionado(context))
                                    Log.d("MAPA_RUTA", "📌 Ubicación agregada: ${ubicacion.nombre}")
                                }
                            }
                        }
                    } else {
                        if (lugar != null) onLugarSeleccionado(lugar)
                    }

                    marker.showInfoWindow()
                    false
                }


                mapViewModel.camaraGuardada?.let {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
                }
                onMapReady(map)
            }
        }
    }, modifier = modifier)
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun Double.formatDecimales(digits: Int): String = "%.${digits}f".format(this)

