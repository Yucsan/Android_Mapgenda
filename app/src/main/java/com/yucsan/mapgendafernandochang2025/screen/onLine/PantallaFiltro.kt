package com.yucsan.mapgendafernandochang2025.screen.onLine

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
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
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.util.coloresPorCategoriaPadre
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.google.android.gms.maps.model.LatLng
import com.yucsan.mapgendafernandochang2025.componentes.navegacion.BottomBarScreen
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal

@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFiltro(
    viewModelLugar: LugarViewModel = viewModel(),
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val cargando by viewModelLugar.cargando.collectAsState()
    val seleccionadasViewModel by viewModelLugar.filtrosActivos.collectAsState()
    val seleccionadas = remember { mutableStateListOf<String>() }
    val permisoConcedido = remember { mutableStateOf(false) }
    var distanciaFiltro by remember { mutableStateOf(10000f) }
    var iniciarCarga by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val conteoPorSubcategoria by viewModelLugar.conteoPorSubcategoriaFiltrado.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val categoriasActivas = remember { mutableStateListOf<String>() }
    val categoriasActivasOrdenadas = categoriasPorGrupo.keys.filter { categoriasActivas.contains(it) }
    val ubicacionesGuardadas by ubicacionViewModel.ubicacionesPorUsuario.collectAsState()
    var expandirMenu by remember { mutableStateOf(false) }
    val ubicacionSeleccionada by viewModelLugar.ubicacionSeleccionada.collectAsState(initial = null)
    val categoriasExpandibles by viewModelLugar.categoriasExpandibles.collectAsState()

    // Al cambiar los filtros globales, actualizamos el estado local
    LaunchedEffect(seleccionadasViewModel) {
        seleccionadas.clear()
        seleccionadas.addAll(seleccionadasViewModel)

        categoriasActivas.clear()
        val nuevoMapa = categoriasExpandibles.toMutableMap()
        
        seleccionadasViewModel.forEach { subcat ->
            categoriasPorGrupo.entries.find { it.value.contains(subcat) }?.key?.let { categoria ->
                if (!categoriasActivas.contains(categoria)) {
                    categoriasActivas.add(categoria)
                }
                // Si hay una subcategoría seleccionada, expandimos su categoría padre
                nuevoMapa[categoria] = true
            }
        }
        
        viewModelLugar.actualizarCategoriasExpandibles(nuevoMapa)
    }

    // Actualizar categorías cuando cambia la ubicación
    LaunchedEffect(ubicacionSeleccionada) {
        //viewModelLugar.cargarConteoSubcategorias()
        viewModelLugar.cargarConteoSubcategoriasSoloDelUsuario()
    }

    //Acciones iniciales al entrar a la pantalla
    LaunchedEffect(Unit) {
        /*
        viewModelLugar.cargarConteoSubcategorias()
        viewModelLugar.observarTodosLosLugares()*/

        viewModelLugar.cargarConteoSubcategoriasSoloDelUsuario()
        viewModelLugar.iniciarFiltradoSoloDelUsuario()

        verificarPermisoYIniciar(context, permisoConcedido, viewModelLugar)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxSize()
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
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Filtro Mapa OnLine",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Selector de ubicación
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                ) {
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
                            Text(ubicacionSeleccionada?.let { "${it.nombre} (${it.tipo})" } ?: "Seleccionar ubicación")
                        }

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
                                            viewModelLugar.actualizarUbicacion(ubi.latitud, ubi.longitud, ubi)
                                            expandirMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sección de categorías principales
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
                                    val nuevoEstado = !estabaExpandida
                                    val nuevoMapa = categoriasExpandibles.toMutableMap()
                                    nuevoMapa[categoria] = nuevoEstado
                                    viewModelLugar.actualizarCategoriasExpandibles(nuevoMapa)
                                },
                                enabled = ubicacionSeleccionada != null,
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

            // Sección de subcategorías
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
                                                    viewModelLugar.actualizarFiltrosActivos(seleccionadas.toSet())
                                                } else {
                                                    seleccionadas.add(subcategoria)
                                                    viewModelLugar.actualizarFiltrosActivos(seleccionadas.toSet())
                                                }
                                            },
                                            enabled = ubicacionSeleccionada != null,
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

            // Botón para aplicar filtro
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (seleccionadas.isNotEmpty()) {
                            Log.d("Filtro", "🔍 Subcategorías seleccionadas: $seleccionadas")

                            viewModelLugar.actualizarCategorias(seleccionadas.toSet())
                            viewModelLugar.actualizarFiltrosActivos(seleccionadas.toSet())
                            viewModelLugar.actualizarRadio(distanciaFiltro)

                            iniciarCarga = true
                        } else {
                            Toast.makeText(
                                context,
                                "Selecciona al menos una categoría",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
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

            // Botón para volver a pedir permiso si no fue concedido
            if (!permisoConcedido.value) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                verificarPermisoYIniciar(
                                    context,
                                    permisoConcedido,
                                    viewModelLugar
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("\uD83D\uDD13 Volver a intentar permiso")
                    }
                }
            }

            // 🟦 Indicador de carga si está cargando
            if (cargando) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                    Text("Cargando lugares desde base local...")
                }
            }
        }
    }

    // Navegación al mapa si se concede el permiso y se seleccionan categorías
    LaunchedEffect(viewModelLugar.ubicacion.value, iniciarCarga) {
        Log.d(
            "DEBUG_NAV",
            "→ iniciarCarga=$iniciarCarga, ubicacion=${viewModelLugar.ubicacion.value}, permisoConcedido=${permisoConcedido.value}, cargando=$cargando"
        )

        if (iniciarCarga &&
            viewModelLugar.ubicacion.value != null &&
            !cargando
        ) {
            Log.d("DEBUG_NAV", "✅ Condiciones cumplidas, navegando a mapa")

            navController.navigate(Ruta.MapaCompose.ruta) {
                popUpTo(Ruta.Filtro.ruta) { inclusive = true }
            }

            iniciarCarga = false
        } else {
            Log.d("DEBUG_NAV", "⏳ Aún no se cumplen condiciones para navegar")
        }
    }
}

// Función auxiliar para manejar el permiso de ubicación
@androidx.annotation.OptIn(UnstableApi::class)
private suspend fun verificarPermisoYIniciar(
    context: Context,
    permisoConcedido: MutableState<Boolean>,
    viewModelLugar: LugarViewModel
) {
    val ok = solicitarPermisoUbicacion(context)
    permisoConcedido.value = ok
    if (ok) {
        viewModelLugar.iniciarActualizacionUbicacion(context)
        Log.d("FiltroUbicacion", "📍 Permiso concedido. Intentando obtener ubicación...")
        delay(1000)
        Log.d("FiltroUbicacion", "📍 Ubicación obtenida: ${viewModelLugar.ubicacion.value}")
    } else {
        Toast.makeText(context, "Se necesita el permiso de ubicación", Toast.LENGTH_SHORT).show()
        Log.w("FiltroUbicacion", "⛔ Permiso denegado")
    }
}


