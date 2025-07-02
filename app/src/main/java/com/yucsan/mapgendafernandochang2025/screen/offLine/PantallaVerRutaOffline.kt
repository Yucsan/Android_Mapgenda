package com.yucsan.mapgendafernandochang2025.screen.offLine


import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.components.MapaOfflineView
import com.yucsan.mapgendafernandochang2025.util.decodificarPolyline
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModelFactory
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugaresOrdenados
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import kotlinx.coroutines.launch


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVerRutaOffline(
    rutaId: Long,
    rutaViewModel: RutaViewModel,
    navController: NavController
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    var rutaConLugares by remember { mutableStateOf<RutaConLugaresOrdenados?>(null) }

    LaunchedEffect(rutaId) {
        rutaViewModel.recargarRutaSeleccionada(rutaId) {
            rutaConLugares = it
        }
    }

    rutaConLugares?.let { ruta ->
        val lugares = ruta.lugares
        val polylineCodificada = ruta.ruta.polylineCodificada

        val puntos = remember(polylineCodificada) {
            polylineCodificada?.let { decodificarPolyline(it) }
        }

        val app = contexto.applicationContext as Application

        val lugarRutaOfflineViewModel: LugarRutaOfflineViewModel = viewModel(
            factory = LugarRutaOfflineViewModelFactory(app, rutaViewModel.obtenerUsuarioRepository())
        )


        val mapViewModel = remember { MapViewModel() }

        val listaVacia = remember { mutableStateListOf<LugarLocal>() }
        val markerZona = remember { mutableStateOf<Marker?>(null) }
        val colorRuta = MaterialTheme.colorScheme.primary.toArgb()

        Log.d("RUTA_OFFLINE", "Polyline almacenada: ${ruta.ruta.polylineCodificada}")
        
// ✅ Este LaunchedEffect asegura que la línea se dibuje siempre que el mapa esté listo
        LaunchedEffect(googleMap, puntos) {
            if (googleMap != null && puntos != null) {
                googleMap?.addPolyline(
                    PolylineOptions()
                        .addAll(puntos)
                        .color(colorRuta)
                        .width(10f)
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            MapaOfflineView(
                lugares = lugares,
                filtrosActivos = emptyList(),
                distancia = 0f,
                mapViewModel = mapViewModel,
                lugarRutaOfflineViewModel = lugarRutaOfflineViewModel,
                onUbicacionSeleccionada = {},
                onLugarSeleccionado = {},
                onZonaBaseSeleccionada = {},
                isModoAgregarLugar = false,
                isModoCrearRuta = false,
                isSoloLectura = true,
                lugaresSeleccionadosParaRuta = listaVacia,
                onAgregarLugarClick = {},
                markerZonaBase = markerZona,
                modifier = Modifier.fillMaxSize(),
                ubicacionCentro = null,
                ubicaciones = emptyList(),
                onMapReady = { map ->
                    googleMap = map
                    puntos?.let {
                        map.addPolyline(
                            PolylineOptions()
                                .addAll(it)
                                .color(colorRuta)
                                .width(10f)
                        )
                    }
                    if (lugares.isNotEmpty()) {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lugares[0].latitud, lugares[0].longitud), 13f
                            )
                        )
                    }
                }
            )

            TopAppBar(
                title = {
                    Text( ruta.ruta.nombre,
                        style =  MaterialTheme.typography.headlineMedium ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    }
}
