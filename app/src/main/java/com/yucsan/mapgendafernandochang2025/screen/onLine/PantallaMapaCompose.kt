package com.yucsan.mapgendafernandochang2025.screen.onLine

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.yucsan.mapgendafernandochang2025.R
import com.yucsan.mapgendafernandochang2025.ThemeViewModel
import com.yucsan.mapgendafernandochang2025.componentes.cajasTexto.InputDireccionBox
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.screens.mapa.alertas.AgregarLugarDialog
import com.yucsan.mapgendafernandochang2025.screens.mapa.alertas.DetalleLugarDialog
import com.yucsan.mapgendafernandochang2025.util.Secrets
import com.yucsan.mapgendafernandochang2025.util.MarcadorIconoUtils.generarIconoConTextoSobreImagen
import com.yucsan.mapgendafernandochang2025.util.MarcadorIconoUtils.getDrawableForCategoria
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import com.yucsan.mapgendafernandochang2025.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PantallaMapaCompose(
    viewModelLugar: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    mapViewModel: MapViewModel,
    navController: NavController,
    themeViewModel: ThemeViewModel,
    networkMonitor: NetworkMonitor,
    ubicacionViewModel: UbicacionViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val hayConexion by networkMonitor.isConnected.collectAsState()
    val lugares by viewModelLugar.lugares.collectAsState()
    val distancia by viewModelLugar.distanciaSeleccionada.collectAsState()
    val categoriasActivas by viewModelLugar.categoriasSeleccionadas.collectAsState()
    val cargando by viewModelLugar.cargando.collectAsState()
    val ubicacion by viewModelLugar.ubicacion.collectAsState()
    val filtrosActivos by viewModelLugar.filtrosActivos.collectAsState()

    val ubicacionTiempoReal by navegacionViewModel.ubicacionActual.collectAsState()
    val lugaresFiltrados by viewModelLugar.lugaresFiltrados.collectAsState()
    val centradoManualmente = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()
    var lugarSeleccionado by remember { mutableStateOf<LugarLocal?>(null) }
    var modoAgregarLugar by remember { mutableStateOf(false) }
    var latLngSeleccionado by remember { mutableStateOf<LatLng?>(null) }
    var nombreNuevoLugar by remember { mutableStateOf("") }
    var descripcionNuevoLugar by remember { mutableStateOf("") }
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    val ultimoLugarId by viewModelLugar.ultimoLugarAgregadoId.collectAsState()
    var mostrarHalo by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }

    val todos by viewModelLugar.todosLosLugares.collectAsState()
    val ubicacionActual by ubicacionViewModel.ubicacionActual.collectAsState()

    var isMapLoading by remember { mutableStateOf(true) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    val sharedPrefs = context.getSharedPreferences("map_prefs", 0)
    var mostrarBienvenida by remember { mutableStateOf(false) }

    var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }


    LaunchedEffect(Unit) {
        if (!sharedPrefs.getBoolean("bienvenida_mostrada", false)) {
            mostrarBienvenida = true
            sharedPrefs.edit().putBoolean("bienvenida_mostrada", true).apply()
        }
    }

    // Estado para controlar la inicialización del mapa
    LaunchedEffect(Unit) {
        delay(1000)
        isMapLoading = false
    }

    // Efecto para inicializar el mapa cuando esté listo
    LaunchedEffect(isMapLoading) {
        googleMap?.let { safeMap ->
            if (!isMapLoading) {
                mapViewModel.initializeMap(safeMap)
            }
        }
    }

    // Cuando llegue la ubicación, centra la cámara automáticamente
    LaunchedEffect(ubicacionActual) {
        ubicacionActual?.let { (lat, lng) ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(lat, lng), 14f
            )

        }
    }
    LaunchedEffect(Unit) {
        println("🔍 Total lugares locales: ${todos.size}")
    }


    LaunchedEffect(lugares) {
        if (lugares.isNotEmpty() && filtrosActivos.isEmpty()) {
            val subcategoriasDisponibles = lugares.mapNotNull { it.subcategoria }.distinct()
            Log.d("DEBUG_MAP", "⚠️ Filtros vacíos, aplicando automáticamente: $subcategoriasDisponibles")
            viewModelLugar.actualizarFiltrosActivos(subcategoriasDisponibles.toSet())
        }
    }

    LaunchedEffect(Unit) {
        if (viewModelLugar.lugares.value.isEmpty()) {
            if (viewModelLugar.ubicacion.value != null &&
                viewModelLugar.categoriasSeleccionadas.value.isNotEmpty()
            ) {
                Log.d("DEBUG_FIX", "⏱ Forzando actualización tras navegación")
                viewModelLugar.actualizarCategorias(viewModelLugar.categoriasSeleccionadas.value.toSet())
            }
        }
    }

    LaunchedEffect(Unit) {
        navegacionViewModel.iniciarUbicacionTiempoReal(context)
    }


    // Posicionar cámara en la ubicación
    LaunchedEffect(ubicacion) {
        ubicacion?.let { (lat, lng) ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(lat, lng), 14f
            )

        }
    }

    LaunchedEffect(ubicacionTiempoReal) {
        if (!centradoManualmente.value && lugaresFiltrados.isEmpty()) {
            ubicacionTiempoReal?.let { (lat, lng) ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 14f)
                )
                centradoManualmente.value = true
                Log.d("ZOOM_USUARIO", "📍 Cámara centrada en ubicación del usuario al iniciar")
            }
        }
    }

    LaunchedEffect(lugaresFiltrados) {
        if (lugaresFiltrados.isNotEmpty()) {
            val latitudes = lugaresFiltrados.map { it.latitud }
            val longitudes = lugaresFiltrados.map { it.longitud }
            val centro = LatLng(
                latitudes.average(),
                longitudes.average()
            )
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(centro, 13f) // o el zoom que te parezca más natural
            )

            Log.d("ZOOM_DEFAULT", "🎯 Centrado en lugares filtrados: $centro")
        }
    }

    LaunchedEffect(ultimoLugarId) {
        if (ultimoLugarId == null) return@LaunchedEffect

        // Usamos directamente los datos almacenados, sin depender del 'lugares'
        val subcategoria = viewModelLugar.ultimaSubcategoriaAgregada.value ?: "custom"
        val categoria = viewModelLugar.ultimaCategoriaAgregada.value ?: "custom"

        // Forzamos los filtros si el nuevo lugar no está en los filtrados
        val yaEstaFiltrado = lugaresFiltrados.any { it.id == ultimoLugarId }
        if (!yaEstaFiltrado) {
            viewModelLugar.actualizarCategorias(setOf(categoria))
            viewModelLugar.actualizarFiltrosActivos(setOf(subcategoria))
        }

        // Intentamos encontrarlo después de aplicar filtros
        delay(300) // Pequeña espera para que se actualicen los filtrados
        var nuevoLugar: LugarLocal? = null
        repeat(10) { intento ->
            nuevoLugar = lugares.find { it.id == ultimoLugarId }
            if (nuevoLugar != null) return@repeat
            delay(200) // espera corta entre intentos
        }

        if (nuevoLugar != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(nuevoLugar!!.latitud, nuevoLugar!!.longitud),
                    17f
                )
            )
            mostrarHalo = true
            delay(5000)
            mostrarHalo = false
        } else {
            Log.w("MAP_REFRESH", "⛔ Lugar con ID $ultimoLugarId aún no está en 'lugares' tras esperar.")
        }


        viewModelLugar.resetUltimoLugarAgregado()
    }


    LaunchedEffect(Unit) {
        if (!viewModelLugar.fueBusquedaInicialHecha(context)) {
            Log.d("AUTO_INIT", "🔁 Ejecutando búsqueda inicial 'custom'")

            viewModelLugar.actualizarCategorias(setOf("custom"))
            viewModelLugar.actualizarFiltrosActivos(setOf("custom"))
            viewModelLugar.actualizarRadio(3000f) // o el valor por defecto que tú uses

            viewModelLugar.marcarBusquedaInicial(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModelLugar.observarTodosLosLugares()
        viewModelLugar.iniciarActualizacionUbicacion(context) // asegúrate de pasar context
        delay(1000) // opcional, espera a que los datos estén listos
        viewModelLugar.agruparLugaresPorZonaGeografica2(radioKm = 8.0)
    }

    LaunchedEffect(ubicacionSeleccionada) {
        ubicacionSeleccionada?.let { latLng ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            )
            Log.d("ZOOM_INPUT", "🎯 Cámara centrada en: $latLng")
        }
    }



    Box(Modifier.fillMaxSize()) {
        if (isMapLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (mostrarBienvenida) {
        AlertDialog(
            onDismissRequest = { mostrarBienvenida = false },
            confirmButton = {
                TextButton(onClick = { mostrarBienvenida = false }) {
                    Text("Empezar")
                }
            },
            title = {
                Text("¡Bienvenido a Mapgenda!")
            },
            text = {
                Text("Usa el botón azul para centrarte, filtra lugares y explora tu zona. ¡Disfruta!")
            }
        )

    } else {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )

            if (modoAgregarLugar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                        .zIndex(2f)
                ) {
                    Surface(
                        color = Color(0xFFE0F7FA),
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Selecciona un nuevo lugar tocando en el mapa.",
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
                onMapLoaded = {
                    isMapLoading = false
                    Log.d("MAP_READY", "✅ Mapa completamente cargado")
                },
                onMapClick = { latLng ->
                    if (modoAgregarLugar) {
                        latLngSeleccionado = latLng
                        nombreNuevoLugar = ""
                        descripcionNuevoLugar = ""
                    }
                }
            ) {
                Log.d("DEBUG_MAP", "🗺 Pintando ${lugaresFiltrados.size} marcadores en el mapa")

                // Mostrar marcadores
                lugaresFiltrados.forEach { lugar ->
                    val drawableRes = getDrawableForCategoria(lugar.categoriaGeneral)
                    val iconoEscalado = crearIconoRedimensionado(context, drawableRes, 120, 120)
                    MarkerInfoWindow(
                        state = MarkerState(LatLng(lugar.latitud, lugar.longitud)),
                        icon =  generarIconoConTextoSobreImagen(context, lugar.nombre, drawableRes),
                        title = lugar.nombre,
                        onClick = {
                            lugarSeleccionado = lugar
                            true
                        }
                    )
                }

                ubicacionSeleccionada?.let { latLng ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Ubicación buscada"
                    )
                }


                if (mostrarHalo && ultimoLugarId != null) {
                    lugaresFiltrados.find { it.id == ultimoLugarId }?.let { lugar ->
                        Circle(
                            center = LatLng(lugar.latitud, lugar.longitud),
                            radius = 100.0,
                            strokeColor = Color.Magenta,
                            fillColor = Color.Magenta.copy(alpha = 0.3f),
                            strokeWidth = 4f
                        )
                    }
                }
            }

            if (!hayConexion) {
                Text(
                    text = "Modo sin conexión: funciones limitadas.",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            InputDireccionBox(
                apiKey = Secrets.GOOGLE_MAPS_API_KEY,
                googleMap = googleMap,
                onLugarSeleccionado = { lugar ->
                    ubicacionSeleccionada = LatLng(lugar.latitud, lugar.longitud)
                    android.util.Log.d(
                        "RUTA_OFFLINE",
                        "✅ Ubicación seleccionada por INPUT: ${lugar.latitud}, ${lugar.longitud}"
                    )
                }
            )

            // UI superpuesta (botones, loader, filtros, dialogos)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 100.dp)
                    .align(Alignment.BottomEnd),
            ) {

                SmallFloatingActionButton(
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        ubicacionTiempoReal?.let { (lat, lng) ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f)
                                )
                                centradoManualmente.value = true
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación")
                }




                if (!modoAgregarLugar) {
                    SmallFloatingActionButton(
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                        navController.navigate("filtro")
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar lugares")
                    }
                }

                SmallFloatingActionButton(
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        modoAgregarLugar = true

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Toca en el mapa para seleccionar el nuevo lugar.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                    Text("+")
                }

                SmallFloatingActionButton(
                    shape = CircleShape,
                    containerColor = Color(0xFFF5881F),
                    onClick = {
                        navController.navigate("mapaubi?modoSeleccionUbicacion=false")
                    }
                ) {
                    Text("Ruta")
                }

                // Nuevo botón flotante verde para seleccionar ubicación
                SmallFloatingActionButton(
                    onClick = {
                        navController.navigate("mapaSeleccionUbicacion")
                    },
                    containerColor = Color.Gray,
                    contentColor = Color.White,
                ) {
                    Text("ubi")
                }
            }

            // Botón pequeño inferior izquierdo
            SmallFloatingActionButton(
                onClick = { themeViewModel.toggleTheme() },
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 16.dp, bottom = 16.dp)
                    .align(Alignment.BottomStart),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                    contentDescription = "Cambiar tema",
                    tint = Color.White
                )
            }



            if (cargando) {
                LinearProgressIndicator(Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp))
            }

            lugarSeleccionado?.let { lugar ->
                DetalleLugarDialog(
                    lugar = lugar,
                    apiKey = Secrets.GOOGLE_MAPS_API_KEY,
                    viewModelLugar = viewModelLugar,
                    navegacionViewModel = navegacionViewModel,
                    onDismiss = {
                        lugarSeleccionado = null
                    },
                    ubicacionActual = ubicacionTiempoReal,
                    hayConexion = hayConexion,
                    authViewModel = authViewModel
                )
            }

            if (latLngSeleccionado != null && modoAgregarLugar) {
                AgregarLugarDialog(
                    latLngSeleccionado = latLngSeleccionado!!,
                    categoriasDisponibles = listOf("restaurant", "park", "museum", "cafe", "custom"),
                    onGuardar = { nuevoLugar ->
                        val categoriaPadre = nuevoLugar.categoriaGeneral?.takeIf { it.isNotBlank() } ?: "custom"
                        val subcategoria = nuevoLugar.subcategoria?.takeIf { it.isNotBlank() } ?: "custom"

                        val lugarConFallback = nuevoLugar.copy(
                            categoriaGeneral = categoriaPadre,
                            subcategoria = subcategoria
                        )

                        // Guardar con categorías seguras
                        viewModelLugar.agregarLugar(lugarConFallback)

                        // Forzar filtros para asegurar aparición inmediata
                        viewModelLugar.actualizarCategorias(setOf(categoriaPadre))
                        viewModelLugar.actualizarFiltrosActivos(setOf(subcategoria))

                        // 👇 Forzar recarga tras agregar
                        viewModelLugar.recargarLugares()


                        latLngSeleccionado = null
                        modoAgregarLugar = false
                    },
                    onDismiss = {
                        latLngSeleccionado = null
                        modoAgregarLugar = false
                    },
                    mapScope = scope,
                    googleMap = googleMap,
                    guardarCamara = { mapViewModel.guardarCamara(cameraPositionState.position) }
                )
            }
        }
    }
}

fun crearIconoRedimensionado(context: Context, drawableResId: Int, ancho: Int, alto: Int): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return BitmapDescriptorFactory.defaultMarker()
    val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, ancho, alto)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
