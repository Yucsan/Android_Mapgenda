package com.yucsan.mapgendafernandochang2025.screen.offLine


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes.EditarUbicacionDialog
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUbicaciones(viewModel: UbicacionViewModel, navController: NavController) {
    val ubicaciones by viewModel.ubicaciones.collectAsState()
    val ubicacionesPorUsuario by viewModel.ubicacionesPorUsuario.collectAsState()
    val query by viewModel.query.collectAsState()
    var ubicacionSeleccionada by remember { mutableStateOf<UbicacionLocal?>(null) }

    LaunchedEffect(ubicaciones) {
        Log.d("UI_FILTRADAS", "Total: ${ubicaciones.size}")
        ubicaciones.forEach {
            Log.d("UI_FILTRADAS", "🧭 ${it.nombre} | userId=${it.usuarioId}")
        }
    }

    LaunchedEffect(ubicacionesPorUsuario) {
        Log.d("UI_FILTRADAS", "Total: ${ubicacionesPorUsuario.size}")
        ubicacionesPorUsuario.forEach {
            Log.d("UI_FILTRADAS", "🧭 ${it.nombre} | userId=${it.usuarioId}")
        }
    }



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestion Ubicaciones",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    start = 12.dp,
                    end = 12.dp
                )
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.actualizarQuery(it) },
                label = { Text("Buscar ubicaciones") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(ubicacionesPorUsuario) { ubicacion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "\uD83D\uDCCC ${ubicacion.nombre}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Tipo: ${ubicacion.tipo}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Lat: ${ubicacion.latitud}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Lng: ${ubicacion.longitud}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "ID_Usuario: ${ubicacion.usuarioId}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { ubicacionSeleccionada = ubicacion }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }
                    }
                }
            }
        }

        ubicacionSeleccionada?.let { ubicacion ->
            EditarUbicacionDialog(
                ubicacion = ubicacion,
                onDismiss = { ubicacionSeleccionada = null },
                onGuardar = { nuevoNombre, nuevoTipo ->
                    viewModel.actualizarUbicacionCompleta(ubicacion.id, nuevoNombre, nuevoTipo)

                    ubicacionSeleccionada = null
                },
                onEliminar = {
                    viewModel.eliminarUbicacion(ubicacion)
                    ubicacionSeleccionada = null
                },
                onSeleccionarRuta = {
                    // lógica para ruta base aquí si se necesita más adelante
                    ubicacionSeleccionada = null
                }
            )
        }
    }
}

