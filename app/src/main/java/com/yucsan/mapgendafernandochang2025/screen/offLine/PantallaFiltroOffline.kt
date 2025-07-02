package com.yucsan.mapgendafernandochang2025.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.Color
import com.yucsan.mapgendafernandochang2025.util.solicitarPermisoUbicacion
import kotlinx.coroutines.flow.first
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.util.coloresPorCategoriaPadre
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.componentes.navegacion.BottomBarScreen
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFiltroOffline(
    lugarOfflineViewModel: LugarRutaOfflineViewModel,
    lugarViewModel: LugarViewModel,
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val cargando by lugarOfflineViewModel.cargando.collectAsState()
    val seleccionadasViewModel by lugarOfflineViewModel.filtrosActivos.collectAsState()
    val seleccionadas = remember { mutableStateListOf<String>() }
    val permisoConcedido = remember { mutableStateOf(false) }
    var distanciaFiltro by remember { mutableStateOf(10000f) }
    var iniciarCarga by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val conteoPorSubcategoria by lugarOfflineViewModel.conteoPorSubcategoriaFiltrado.collectAsState()
    //val conteoPorSubcategoria by lugarOfflineViewModel.conteoPorSubcategoria.collectAsState()

    var visible by remember { mutableStateOf(false) }
    val categoriasActivas = remember { mutableStateListOf<String>() }
    val categoriasActivasOrdenadas =
        categoriasPorGrupo.keys.filter { categoriasActivas.contains(it) }
    val switchesActivos = remember { mutableStateMapOf<String, Boolean>() }
    val ubicacion by lugarOfflineViewModel.ubicacion.collectAsState()
    val ubicacionesGuardadas by ubicacionViewModel.ubicacionesPorUsuario.collectAsState()

    val categoriasExpandibles = remember { mutableStateMapOf<String, Boolean>() }

    var expandirMenu by remember { mutableStateOf(false) }

/*
    LaunchedEffect(Unit) {
        lugarOfflineViewModel.probarFiltroManual()
    }*/

    LaunchedEffect(seleccionadasViewModel) {
        seleccionadas.clear()
        seleccionadas.addAll(seleccionadasViewModel)
        categoriasActivas.clear()

        seleccionadasViewModel.forEach { subcat ->
            categoriasPorGrupo.entries.find { it.value.contains(subcat) }?.key?.let { categoria ->
                if (!categoriasActivas.contains(categoria)) {
                    categoriasActivas.add(categoria)
                }
                categoriasExpandibles[categoria] = true
            }
        }
    }

    LaunchedEffect(ubicacion) {
        Log.d("DEBUG_UI", "🔁 UI recomposición: ubicación actualizada a: $ubicacion")
    }

    //Acciones iniciales al entrar a la pantalla
    LaunchedEffect(Unit) {
        lugarOfflineViewModel.cargarConteoSubcategorias()
        lugarOfflineViewModel.observarTodosLosLugares()
        verificarPermisoYIniciar(context, permisoConcedido, lugarOfflineViewModel)
        visible = true
    }

    // Animaciones de entrada/salida
    val enterAnimation = remember {
        slideInVertically(initialOffsetY = { fullHeight -> fullHeight })
    }
    val exitAnimation = remember {
        slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
    }


    AnimatedVisibility(visible = visible, enter = enterAnimation, exit = exitAnimation) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 32.dp
                    )
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // Espacio entre ícono y texto

                            Text(
                                text = "Filtro Mapa Offline",
                                color =  MaterialTheme.colorScheme.secondary ,
                                style = MaterialTheme.typography.headlineMedium

                            )
                        }
                    }
                    item {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                        )
                        {
                            Text(
                                text = "Selecciona una ubicación guardada:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 5.dp)
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expandirMenu = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    val ubicacionActual = ubicacion
                                    val colorScheme = MaterialTheme.colorScheme
                                    val (textoUbicacion, colorUbicacion) = remember(
                                        ubicacionesGuardadas,
                                        ubicacionActual
                                    ) {
                                        val match = ubicacionesGuardadas.find {
                                            it.latitud == ubicacionActual?.first && it.longitud == ubicacionActual?.second
                                        }
                                        if (match != null) {
                                            "📍 ${match.nombre} (${match.tipo})" to Color(0xFFFF4000)
                                        } else if (ubicacionActual != null) {
                                            "📍 %.4f, %.4f".format(
                                                ubicacionActual.first,
                                                ubicacionActual.second
                                            ) to Color(0xFFFF4000)
                                        } else {
                                            "Elegir ubicación" to colorScheme.primary
                                        }
                                    }

                                    Text(
                                        text = textoUbicacion,
                                        color = colorUbicacion,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                // Dropdown fuera del LazyColumn (pero aún dentro del Box para correcto posicionamiento)
                                DropdownMenu(
                                    expanded = expandirMenu,
                                    onDismissRequest = { expandirMenu = false }
                                ) {
                                    if (ubicacionesGuardadas.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No hay ubicaciones guardadas") },
                                            onClick = {}
                                        )
                                    } else {
                                        ubicacionesGuardadas.forEach { ubi ->
                                            DropdownMenuItem(
                                                text = { Text("${ubi.nombre} (${ubi.tipo})") },
                                                onClick = {
                                                    lugarOfflineViewModel.actualizarUbicacionManual(
                                                        LatLng(ubi.latitud, ubi.longitud)
                                                    )
                                                    expandirMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val categoriasConDatos = categoriasPorGrupo.entries.filter { (_, subcategorias) ->
                            subcategorias.any { subcat -> (conteoPorSubcategoria[subcat] ?: 0) > 0 }
                        }

                        if (categoriasConDatos.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp)
                            ) {
                                categoriasConDatos.forEach { (categoria, _) ->
                                    val color = coloresPorCategoriaPadre[categoria]
                                    FilterChip(
                                        selected = categoriasExpandibles[categoria] == true,
                                        onClick = {
                                            val estabaExpandida = categoriasExpandibles[categoria] ?: false
                                            categoriasExpandibles[categoria] = !estabaExpandida
                                        },
                                        enabled = ubicacion != null,
                                        label = { Text(categoria) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color ?: Color.Gray,
                                            containerColor = Color.Transparent,
                                            selectedLabelColor = Color.White,
                                            labelColor = color ?: MaterialTheme.colorScheme.primary,
                                            disabledLabelColor = color?.copy(alpha = 0.4f) ?: Color.LightGray
                                        ),
                                        border = BorderStroke(1.dp, color ?: Color.Gray)
                                    )
                                }
                            }
                        }
                    }


                    item {
                        val categoriasConDatos = categoriasPorGrupo.entries.filter { (_, subcategorias) ->
                            subcategorias.any { subcat -> (conteoPorSubcategoria[subcat] ?: 0) > 0 }
                        }

                        if (categoriasConDatos.isNotEmpty()) {
                            categoriasConDatos.forEach { (categoria, subcategorias) ->
                                val color = coloresPorCategoriaPadre[categoria]
                                val subcategoriasConDatos = subcategorias.filter { (conteoPorSubcategoria[it] ?: 0) > 0 }

                                val expandida = categoriasExpandibles[categoria] == true
                                if (expandida && subcategoriasConDatos.isNotEmpty()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            text = categoria,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = color ?: MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                        )

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp)
                                        ) {
                                            subcategoriasConDatos.forEach { subcategoria ->
                                                FilterChip(
                                                    selected = seleccionadas.contains(subcategoria),
                                                    onClick = {
                                                        if (seleccionadas.contains(subcategoria)) {
                                                            seleccionadas.remove(subcategoria)
                                                            lugarOfflineViewModel.actualizarFiltrosActivos(seleccionadas.toSet())
                                                        } else {
                                                            seleccionadas.add(subcategoria)
                                                            lugarOfflineViewModel.actualizarFiltrosActivos(seleccionadas.toSet())
                                                        }
                                                    },
                                                    enabled = ubicacion != null,
                                                    label = {
                                                        Text(
                                                            "$subcategoria (${conteoPorSubcategoria[subcategoria] ?: 0})",
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = color ?: Color.Gray,
                                                        selectedLabelColor = Color.White,
                                                        containerColor = color?.copy(alpha = 0.2f) ?: Color.LightGray
                                                    ),
                                                    border = BorderStroke(1.dp, color ?: Color.Gray)
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }


                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                if (seleccionadas.isNotEmpty()) {
                                    scope.launch {
                                        val ubicacionManual = lugarOfflineViewModel.ubicacion.value
                                        if (ubicacionManual != null) {
                                            val latLng = LatLng(ubicacionManual.first, ubicacionManual.second)
                                            Log.d("DEBUG_UBICACION", "🌍 Aplicando filtro con ubicación: $latLng")
                                            lugarOfflineViewModel.actualizarUbicacionManual(latLng)
                                            lugarOfflineViewModel.actualizarRadio(distanciaFiltro)
                                            lugarOfflineViewModel.actualizarFiltrosActivos(seleccionadas.toSet())
                                            lugarOfflineViewModel.actualizarCategorias(seleccionadas.toSet())
                                            /*
                                            lugarOfflineViewModel.aplicarFiltroManualConParametros(
                                                subcategorias = seleccionadas.toList(),
                                                centro = latLng,
                                                radio = distanciaFiltro
                                            )*/

                                            lugarOfflineViewModel.aplicarFiltroManualSoloDelUsuario(
                                                subcategorias = seleccionadas.toList(),
                                                centro = latLng,
                                                radio = distanciaFiltro
                                            )

                                            delay(600)
                                            val lugares = lugarOfflineViewModel.lugaresOffline.value
                                            if (lugares.isNotEmpty()) {
                                                navController.navigate("mapaubi") {
                                                    popUpTo(0)
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "❗ No se encontraron lugares con el filtro actual",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "❗ No hay ubicación disponible para aplicar el filtro",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Selecciona al menos una categoría",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = ubicacion != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buscar lugares")
                        }
                    }

                    item {
                        val hayLugares = categoriasPorGrupo.values.flatten()
                            .any { subcat -> (conteoPorSubcategoria[subcat] ?: 0) > 0 }

                        if (!hayLugares) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😕 No hay sitios descargados para esta ubicación.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Button(
                                    onClick = {
                                        navController.navigate(BottomBarScreen.Descargas.route) {
                                            // popUpTo al principio de tu grafo, para restaurar estados como haces en el bottom-nav
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Ir a descargas")
                                }
                            }
                        }
                    }


                    if (!permisoConcedido.value) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        verificarPermisoYIniciar(
                                            context,
                                            permisoConcedido,
                                            lugarOfflineViewModel
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("\uD83D\uDD13 Volver a intentar permiso")
                            }
                        }
                    }

                    if (cargando) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            CircularProgressIndicator()
                            Text("Cargando lugares desde base local...")
                        }
                    }
                }
            }
        }




    LaunchedEffect(ubicacion, iniciarCarga) {
        if (iniciarCarga && ubicacion != null && permisoConcedido.value && !cargando) {
            visible = false
            snapshotFlow { visible }.first { !it }

            lugarOfflineViewModel.actualizarCategorias(seleccionadas.toSet())
            lugarOfflineViewModel.actualizarRadio(distanciaFiltro)

            Log.d("DEBUG_UBICACION", "🌍 Ubicación usada para filtrar: $ubicacion")
            Toast.makeText(context, "Usando ubicación: $ubicacion", Toast.LENGTH_SHORT).show()

            navController.navigate("mapaubi") {
                popUpTo(0)
            }

            iniciarCarga = false
        }
    }


}


// Función auxiliar para manejar el permiso de ubicación
private suspend fun verificarPermisoYIniciar(
    context: Context,
    permisoConcedido: MutableState<Boolean>,
    lugarOfflineViewModel: LugarRutaOfflineViewModel
) {
    val ok = solicitarPermisoUbicacion(context)
    permisoConcedido.value = ok
    if (ok) {
        // SOLO INICIA actualización si no hay ubicación ya establecida
        if (lugarOfflineViewModel.ubicacion.value == null) {
            //lugarOfflineViewModel.iniciarActualizacionUbicacion(context)
        } else {
            Log.d("DEBUG_UBI", "✅ Ubicación manual ya establecida, no se sobrescribirá.")
        }
    } else {
        Toast.makeText(context, "Se necesita el permiso de ubicación", Toast.LENGTH_SHORT).show()
    }
}

fun Double.format(decimales: Int) = "%.${decimales}f".format(this)
