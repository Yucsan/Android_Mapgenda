package com.yucsan.mapgendafernandochang2025.screen.offLine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal



import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes.EditarUbicacionDialog
import com.yucsan.mapgendafernandochang2025.componentes.cajasTexto.InputDireccionBox
import com.yucsan.mapgendafernandochang2025.screens.mapa.alertas.AgregarLugarDialog

import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.components.MapaOfflineView
import com.yucsan.mapgendafernandochang2025.util.Secrets
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel


import kotlinx.coroutines.launch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import com.google.android.gms.location.LocationServices
import com.yucsan.mapgendafernandochang2025.screen.offLine.componentes.PantallaGuiaDescargaOffline
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.directions.DirectionsService
import com.yucsan.mapgendafernandochang2025.util.MarcadorIconoUtils.generarIconoConTextoSobreImagen
import com.yucsan.mapgendafernandochang2025.util.MarcadorIconoUtils.getDrawableForCategoria
import com.yucsan.mapgendafernandochang2025.util.decodificarPolyline
import com.yucsan.mapgendafernandochang2025.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun PantallaMapaOffline(
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    lugarViewModel: LugarViewModel,
    mapViewModel: MapViewModel,
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController,
    rutaViewModel: RutaViewModel,
    modoAsistente: Boolean = false,
    modoSeleccionUbicacion: Boolean = false,
    modoCrearRuta: Boolean = false,
    usuarioViewModel: UsuarioViewModel,
    onUbicacionConfirmada: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val permisoConcedido = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!permisoConcedido && activity != null) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1002
        )
    }

    val pasoInicial = if (modoSeleccionUbicacion) 1 else 2
    var pasoActual by remember { mutableStateOf(pasoInicial) }

    var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }
    var latLngSeleccionado by remember { mutableStateOf<LatLng?>(null) }
    var modoAgregarLugar by remember { mutableStateOf(false) }
    var modoCrearRuta by remember { mutableStateOf(modoCrearRuta) }

    val mostrarDialogoGuardar = remember { mutableStateOf(false) }

    val categoriasDisponibles = listOf("restaurant", "park", "museum", "cafe", "custom")
    val lugaresSeleccionadosParaRuta = remember { mutableStateListOf<LugarLocal>() }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    val lugares by lugarRutaOfflineViewModel.lugaresOffline.collectAsState()
    val filtrosActivos by lugarRutaOfflineViewModel.filtrosActivos.collectAsState()
    val distancia by lugarRutaOfflineViewModel.distanciaSeleccionada.collectAsState()
    var markerZonaBase = remember { mutableStateOf<Marker?>(null) }
    val ubicaciones by ubicacionViewModel.ubicaciones.collectAsState()

    var modoTransporte by remember { mutableStateOf("bicycling") } // "driving" o "bicycling"

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )
    val scope = rememberCoroutineScope()
    val peekHeightTarget = if (lugaresSeleccionadosParaRuta.isNotEmpty()) 95.dp else 45.dp
    val animatedPeekHeight by animateDpAsState(
        targetValue = peekHeightTarget,
        animationSpec = tween(durationMillis = 300),
        label = "peekHeightAnimation"
    )
    val mostrarDialogoGuardarRuta = remember { mutableStateOf(false) }
    val guardandoUbicacion = remember { mutableStateOf(false) }
    val mostrarPantallaDescarga = remember { mutableStateOf(false) }

    var latParaGuia by remember { mutableStateOf(0.0) }
    var lngParaGuia by remember { mutableStateOf(0.0) }

    val directionsService = remember { DirectionsService() }

    val ubicacionFiltro by lugarRutaOfflineViewModel.ubicacion.collectAsState()


    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var expandedCategoria by remember { mutableStateOf(false) }

    val categoriasDisponiblesRutas = listOf(
        "personalizada", "cultural", "histórica", "gastronómica", "otros"
    )

    var usuarioId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        usuarioId = usuarioViewModel.obtenerIdUsuario()
    }

    LaunchedEffect(Unit) {
        if (ubicacionSeleccionada == null && ubicacionFiltro != null) {
            ubicacionSeleccionada = LatLng(ubicacionFiltro!!.first, ubicacionFiltro!!.second)
            Log.d("DEBUG_UBICACION", "📌 Se aplicó ubicación desde filtro: $ubicacionSeleccionada")
        }
    }

    LaunchedEffect(Unit) {
        lugarRutaOfflineViewModel.lugaresOffline.collect { lista ->
            Log.d("MAPA_LUGARES", "📍 lugaresOffline actualizados (${lista.size})")
            lista.forEach { l ->
                Log.d("MAPA_LUGARES", "✅ ${l.nombre} | ${l.subcategoria} | ${l.id}")
            }
        }
    }


    LaunchedEffect(lugaresSeleccionadosParaRuta.size) {
        if (lugaresSeleccionadosParaRuta.isNotEmpty()) {
            scope.launch {
                kotlinx.coroutines.delay(100) // 🕒 Da tiempo a que se monte el contenido
                scaffoldState.bottomSheetState.partialExpand()
            }
        } else {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
    }


    LaunchedEffect(modoSeleccionUbicacion, modoCrearRuta) {
        Log.d(
            "DEBUG_MAPA",
            "🧭 modoSeleccionUbicacion=$modoSeleccionUbicacion, modoCrearRuta=$modoCrearRuta"
        )

        if (modoSeleccionUbicacion) {
            ubicacionSeleccionada = null
            pasoActual = 1
            modoAgregarLugar = false
            modoCrearRuta = false
        } else {
            pasoActual = 2
            // si viene crear ruta directamente
            if (modoCrearRuta) {
                modoAgregarLugar = false
            }
        }
    }


    Log.d(
        "DEBUG_BTN",
        "🧭 pasoActual=$pasoActual, ubicacionSeleccionada=$ubicacionSeleccionada, mostrarDialogoGuardar=${mostrarDialogoGuardar.value}"
    )


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = animatedPeekHeight,
        sheetContainerColor = Color.Transparent,
        containerColor = Color.Transparent,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.DarkGray,
                height = 6.dp,
                width = 40.dp,
                shape = RoundedCornerShape(50)
            )
        },
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                // Fondo blur + color oscuro
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(30.dp)
                        .background(Color.DarkGray.copy(alpha = 0.9f))
                )

                // Contenido visible
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Lugares seleccionados (${lugaresSeleccionadosParaRuta.size}):",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        itemsIndexed(lugaresSeleccionadosParaRuta) { index, lugar ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF424242)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Título con límite
                                    Text(
                                        text = "${index + 1}. ${lugar.nombre}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f) // ocupa el espacio disponible
                                    )

                                    // Botones
                                    Row(
                                        modifier = Modifier.padding(start = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val iconSize = 20.dp
                                        val buttonSize = 32.dp

                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    lugaresSeleccionadosParaRuta.removeAt(index).also {
                                                        lugaresSeleccionadosParaRuta.add(index - 1, it)
                                                    }
                                                }
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier.size(buttonSize)
                                        ) {
                                            Icon(
                                                Icons.Default.ExpandLess,
                                                contentDescription = "Subir",
                                                tint = Color.White,
                                                modifier = Modifier.size(iconSize)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (index < lugaresSeleccionadosParaRuta.lastIndex) {
                                                    lugaresSeleccionadosParaRuta.removeAt(index).also {
                                                        lugaresSeleccionadosParaRuta.add(index + 1, it)
                                                    }
                                                }
                                            },
                                            enabled = index < lugaresSeleccionadosParaRuta.lastIndex,
                                            modifier = Modifier.size(buttonSize)
                                        ) {
                                            Icon(
                                                Icons.Default.ExpandMore,
                                                contentDescription = "Bajar",
                                                tint = Color.White,
                                                modifier = Modifier.size(iconSize)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                lugaresSeleccionadosParaRuta.removeAt(index)
                                            },
                                            modifier = Modifier.size(buttonSize)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(iconSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = 0.dp) // ⬅️ CLAVE para evitar separación con BottomNav
            ) {

                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

                    MapaOfflineView(
                        lugares = lugares,
                        filtrosActivos = filtrosActivos,
                        distancia = distancia,
                        mapViewModel = mapViewModel,
                        lugarRutaOfflineViewModel = lugarRutaOfflineViewModel,
                        onUbicacionSeleccionada = { ubicacionSeleccionada = it },
                        onLugarSeleccionado = {
                                lugar ->
                            ubicacionSeleccionada = LatLng(lugar.latitud, lugar.longitud)

                            // ✅ Solo mover la cámara si NO estás creando ruta
                            if (!modoCrearRuta) {
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(ubicacionSeleccionada!!, 15f)
                                )
                            }

                            Log.d("MAPA_OFFLINE", "📍 Lugar tocado: ${lugar.nombre}")
                        },
                        onZonaBaseSeleccionada = { ubicacionSeleccionada = it },
                        isModoAgregarLugar = modoAgregarLugar,
                        isModoCrearRuta = modoCrearRuta,
                        isSoloLectura = false,
                        lugaresSeleccionadosParaRuta = lugaresSeleccionadosParaRuta,
                        onAgregarLugarClick = {
                            latLngSeleccionado = it
                        },
                        markerZonaBase = markerZonaBase,
                        onMapReady = { map ->
                            googleMap = map
                            map.isMyLocationEnabled = false },
                        modifier = Modifier.fillMaxSize(),
                        ubicacionCentro = ubicacionSeleccionada,
                        ubicaciones = ubicaciones
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .zIndex(2f)
                    ) {
                        InputDireccionBox(
                            apiKey = Secrets.GOOGLE_MAPS_API_KEY,
                            googleMap = googleMap,
                            onLugarSeleccionado = { lugar ->
                                ubicacionSeleccionada = LatLng(lugar.latitud, lugar.longitud)
                                Log.d(
                                    "RUTA_OFFLINE",
                                    "✅ Ubicación seleccionada por INPUT: ${lugar.latitud}, ${lugar.longitud}"
                                )
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(start = 16.dp, top = 3.dp)
                                .zIndex(2f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Volver", style = MaterialTheme.typography.labelLarge)
                        }

                        if (pasoActual == 2 && modoCrearRuta) {
                                Button(
                                    onClick = { modoTransporte = "driving" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (modoTransporte == "driving") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray
                                    )
                                ) {
                                    Text("🚗 Coche")
                                }

                                Button(
                                    onClick = { modoTransporte = "bicycling" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (modoTransporte == "bicycling") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray
                                    )
                                ) {
                                    Text("🚴 Bici")
                                }
                            }
                        }

                        if (pasoActual == 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFB2DFDB), shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "1. Escribe una ubicación\n2. Pincha en el mapa\n3. Confirma para continuar",
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (pasoActual == 2 && !modoSeleccionUbicacion) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 50.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {

                            // Botón de filtro
                            SmallFloatingActionButton(
                                shape = CircleShape,
                                onClick = {
                                    navController.navigate("filtrooffline")
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filtrar lugares"
                                )
                            }

                            // Botón para crear lugar
                            SmallFloatingActionButton(
                                shape = CircleShape,
                                onClick = {
                                    modoAgregarLugar = true
                                    modoCrearRuta = false
                                    Toast.makeText(
                                        context,
                                        "Toca en el mapa para agregar un nuevo lugar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                containerColor = Color(0xFFF77C00),
                                contentColor = Color.White
                            ) {
                                Text("+")
                            }

                            // Botón para alternar modo crear ruta -------------------
                            SmallFloatingActionButton(
                                shape = CircleShape,
                                onClick = {
                                    modoCrearRuta = !modoCrearRuta
                                    modoAgregarLugar = false
                                    if (!modoCrearRuta) {
                                        lugaresSeleccionadosParaRuta.clear()
                                    }
                                },
                                containerColor = if (modoCrearRuta) Color.Red else Color.Gray,
                                contentColor = Color.White
                            ) {
                                Text("Ruta")
                            }

                            SmallFloatingActionButton(
                                modifier = Modifier,
                                onClick = {
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                                    fusedLocationClient.lastLocation
                                        .addOnSuccessListener { location ->
                                            if (location != null) {
                                                val currentLatLng = LatLng(location.latitude, location.longitude)
                                                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                                            } else {
                                                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error obteniendo la ubicación", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                            }

                            if (modoCrearRuta && lugaresSeleccionadosParaRuta.size >= 3) {
                                SmallFloatingActionButton(
                                    shape = CircleShape,
                                    onClick = {
                                        val uri = buildString {
                                            append("https://www.google.com/maps/dir/?api=1")
                                            append("&travelmode=$modoTransporte")
                                            append("&origin=${lugaresSeleccionadosParaRuta.first().latitud},${lugaresSeleccionadosParaRuta.first().longitud}")
                                            append("&destination=${lugaresSeleccionadosParaRuta.last().latitud},${lugaresSeleccionadosParaRuta.last().longitud}")
                                            if (lugaresSeleccionadosParaRuta.size > 2) {
                                                append("&waypoints=")
                                                append(
                                                    lugaresSeleccionadosParaRuta
                                                        .subList(1, lugaresSeleccionadosParaRuta.lastIndex)
                                                        .joinToString("|") { "${it.latitud},${it.longitud}" }
                                                )
                                            }
                                        }

                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(uri)
                                        ).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }

                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "No se pudo abrir Google Maps",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e(
                                                "INTENT_MAPS",
                                                "Error al abrir Maps: ${e.message}"
                                            )
                                        }
                                    },
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ) {
                                    Text("Ir")
                                }

                                SmallFloatingActionButton(
                                    shape = CircleShape,
                                    modifier= Modifier.padding(bottom = 50.dp),
                                    onClick = {
                                        mostrarDialogoGuardarRuta.value = true

                                        val seleccionada = ubicacionSeleccionada
                                        if (seleccionada == null) {
                                            Toast.makeText(context, "Primero selecciona una ubicación en el mapa o usa el buscador", Toast.LENGTH_SHORT).show()
                                            return@SmallFloatingActionButton
                                        }

                                        val ubicacionExistente = ubicaciones.minByOrNull { ubi ->
                                            val dLat = ubi.latitud - seleccionada.latitude
                                            val dLng = ubi.longitud - seleccionada.longitude
                                            dLat * dLat + dLng * dLng
                                        }

                                        // Si no existe una ubicación similar, la guardamos automáticamente
                                        if (ubicacionExistente == null && !guardandoUbicacion.value) {
                                            guardandoUbicacion.value = true
                                            scope.launch {
                                                ubicacionViewModel.guardarUbicacion(
                                                    nombre = "Ubicación desde mapa",
                                                    lat = ubicacionSeleccionada!!.latitude,
                                                    lng = ubicacionSeleccionada!!.longitude,
                                                    tipo = "filtro"
                                                )
                                                Toast.makeText(context, "Ubicación guardada automáticamente", Toast.LENGTH_SHORT).show()
                                                guardandoUbicacion.value = false
                                            }
                                            return@SmallFloatingActionButton
                                        }
                                        mostrarDialogoGuardarRuta.value = true
                                    },
                                    containerColor = Color(0xFF1976D2),
                                    contentColor = Color.White,
                                ) {
                                    Text("Guarda",
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }

                    if (latLngSeleccionado != null && modoAgregarLugar) {
                        AgregarLugarDialog(
                            latLngSeleccionado = latLngSeleccionado!!,
                            categoriasDisponibles = categoriasDisponibles,
                            onGuardar = { nuevoLugar ->
                                val categoriaPadre = nuevoLugar.categoriaGeneral?.takeIf { it.isNotBlank() } ?: "custom"
                                val subcategoria = nuevoLugar.subcategoria?.takeIf { it.isNotBlank() } ?: "custom"

                                val lugarConFallback = nuevoLugar.copy(
                                    categoriaGeneral = categoriaPadre,
                                    subcategoria = subcategoria
                                )

                                // Guardar el lugar
                                lugarViewModel.agregarLugar(lugarConFallback)

                                // 🔥 FORZAR RECARGA COMPLETA
                                scope.launch {
                                    // Pequeña espera para asegurar que se guardó
                                    kotlinx.coroutines.delay(300)

                                    // Obtener ubicación actual para reaplicar filtros
                                    val centroActual = ubicacionSeleccionada ?: LatLng(
                                        lugarConFallback.latitud,
                                        lugarConFallback.longitud
                                    )

                                    // Incluir la nueva subcategoría en los filtros activos
                                    val filtrosActualizados = lugarRutaOfflineViewModel.filtrosActivos.value.toMutableSet()
                                    filtrosActualizados.add(subcategoria)

                                    // Reaplicar filtros con el nuevo lugar incluido
                                    lugarRutaOfflineViewModel.aplicarFiltroManualConParametros(
                                        subcategorias = filtrosActualizados.toList(),
                                        centro = centroActual,
                                        radio = lugarRutaOfflineViewModel.distanciaSeleccionada.value
                                    )

                                    // Mensaje de confirmación
                                    Toast.makeText(context, "✅ Lugar agregado y disponible para rutas", Toast.LENGTH_SHORT).show()
                                }

                                latLngSeleccionado = null
                                modoAgregarLugar = false
                            },
                            onDismiss = {
                                latLngSeleccionado = null
                                modoAgregarLugar = false
                            },
                            mapScope = scope,
                            googleMap = googleMap,
                            guardarCamara = { camara -> mapViewModel.guardarCamara(camara) }
                        )
                    }


                    if (mostrarDialogoGuardar.value && ubicacionSeleccionada != null) {
                        EditarUbicacionDialog(
                            ubicacion = UbicacionLocal(
                                nombre = "",
                                latitud = ubicacionSeleccionada!!.latitude,
                                longitud = ubicacionSeleccionada!!.longitude,
                                tipo = "país",
                                usuarioId = usuarioId
                            ),
                            onDismiss = { mostrarDialogoGuardar.value = false },
                            onGuardar = { nombre, tipo ->
                                scope.launch {
                                    val guardado = ubicacionViewModel.guardarUbicacion(
                                        nombre = nombre,
                                        lat = ubicacionSeleccionada!!.latitude,
                                        lng = ubicacionSeleccionada!!.longitude,
                                        tipo = tipo
                                    )

                                    if (guardado) {
                                        Toast.makeText(context, "✅ Ubicación guardada", Toast.LENGTH_SHORT).show()
                                        lugarViewModel.actualizarUbicacionManual(ubicacionSeleccionada!!)
                                    } else {
                                        Toast.makeText(context, "⚠️ Ya existe una ubicación cercana", Toast.LENGTH_SHORT).show()
                                    }

                                    mostrarDialogoGuardar.value = false
                                    pasoActual = 2
                                }
                            },
                            onEliminar = { mostrarDialogoGuardar.value = false },
                            onSeleccionarRuta = {
                                pasoActual = 2
                                mostrarDialogoGuardar.value = false
                            }
                        )
                    }

                    if (mostrarDialogoGuardarRuta.value) {
                        var nombreRuta by remember { mutableStateOf("") }

                        AlertDialog(
                            onDismissRequest = { mostrarDialogoGuardarRuta.value = false },
                            title = { Text("Guardar nueva ruta") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = nombreRuta,
                                        onValueChange = { nombreRuta = it },
                                        label = { Text("Nombre de la ruta") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    ExposedDropdownMenuBox(
                                        expanded = expandedCategoria,
                                        onExpandedChange = { expandedCategoria = !expandedCategoria }
                                    ) {
                                        OutlinedTextField(
                                            readOnly = true,
                                            value = categoriaSeleccionada ?: "Selecciona categoría",
                                            onValueChange = {},
                                            label = { Text("Categoría") },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )
                                        // Categorias Rutas
                                        ExposedDropdownMenu(
                                            expanded = expandedCategoria,
                                            onDismissRequest = { expandedCategoria = false }
                                        ) {
                                            categoriasDisponiblesRutas.forEach { categoria ->
                                                DropdownMenuItem(
                                                    text = { Text(categoria) },
                                                    onClick = {
                                                        categoriaSeleccionada = categoria
                                                        expandedCategoria = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                }


                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (nombreRuta.isBlank()) {
                                        Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }

                                    if (lugaresSeleccionadosParaRuta.size < 3) {
                                        Toast.makeText(context, "Selecciona al menos 3 lugares", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }

                                    val primerLugar = lugaresSeleccionadosParaRuta.first()
                                    val ultimoLugar = lugaresSeleccionadosParaRuta.last()

                                    scope.launch {
                                        var polyline: String? = null
                                        try {
                                            val origin = "${primerLugar.latitud},${primerLugar.longitud}"
                                            val destination = "${ultimoLugar.latitud},${ultimoLugar.longitud}"
                                            val waypoints = if (lugaresSeleccionadosParaRuta.size > 2) {
                                                lugaresSeleccionadosParaRuta
                                                    .subList(1, lugaresSeleccionadosParaRuta.lastIndex)
                                                    .joinToString("|") { "${it.latitud},${it.longitud}" }
                                            } else ""

                                            val modoPrimario = modoTransporte
                                            var modoActual = modoPrimario

                                            val response = directionsService.api.obtenerRutaConWaypoints(
                                                origin = origin,
                                                destination = destination,
                                                waypoints = waypoints,
                                                mode = modoPrimario,
                                                apiKey = Secrets.GOOGLE_MAPS_API_KEY
                                            )

                                            polyline = response.routes.firstOrNull()?.overview_polyline?.points

                                            if (polyline == null && modoPrimario == "bicycling") {
                                                // Reintenta con DRIVING
                                                Log.w("RUTA_GUARDAR", "❌ No se pudo obtener ruta en bici, reintentando con coche...")
                                                modoActual = "driving"
                                                val retryResponse = directionsService.api.obtenerRutaConWaypoints(
                                                    origin = origin,
                                                    destination = destination,
                                                    waypoints = waypoints,
                                                    mode = modoActual,
                                                    apiKey = Secrets.GOOGLE_MAPS_API_KEY
                                                )
                                                polyline = retryResponse.routes.firstOrNull()?.overview_polyline?.points

                                                if (polyline != null) {
                                                    Toast.makeText(context, "⚠️ Ruta en bici no disponible. Se usó modo coche.", Toast.LENGTH_LONG).show()
                                                }
                                            }

                                            Log.d("RUTA_GUARDAR", "✅ Polyline con modo=$modoActual: ${polyline?.take(50)}")

                                        } catch (e: Exception) {
                                            Log.e("RUTA_GUARDAR", "Error al obtener ruta: ${e.message}")
                                        }


                                        rutaViewModel.crearRuta(
                                            nombre = nombreRuta,
                                            categoria = categoriaSeleccionada ?: "personalizada",
                                            ubicacionId = null, // ✅ SIN ubicación
                                            lugares = lugaresSeleccionadosParaRuta.toList(),
                                            polylineCodificada = polyline
                                        )

                                        mostrarDialogoGuardarRuta.value = false
                                        lugaresSeleccionadosParaRuta.clear()
                                        modoCrearRuta = false
                                        Toast.makeText(context, "Ruta guardada con éxito", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Guardar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { mostrarDialogoGuardarRuta.value = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                }
            }
        }
    )

    if (mostrarPantallaDescarga.value) {
        PantallaGuiaDescargaOffline(
            lat = latParaGuia,
            lng = lngParaGuia,
            onOmitir = {
                mostrarPantallaDescarga.value = false
            },
            onDescargaCompletada = {
                mostrarPantallaDescarga.value = false
                navController.navigate("rutas") {
                    popUpTo("pantallaInicio") { inclusive = false }
                }
            },
            context = context
        )
    }
}

