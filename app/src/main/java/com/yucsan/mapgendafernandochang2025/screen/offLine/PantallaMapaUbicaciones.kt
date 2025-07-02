package com.yucsan.mapgendafernandochang2025.screen.offLine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.layout.BoxScope


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.componentes.cajasTexto.InputDireccionBox
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes.EditarUbicacionDialog
import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes.MapaSeleccionUbicacionView
import com.yucsan.mapgendafernandochang2025.screens.rutasoffline.components.MapaOfflineView
import com.yucsan.mapgendafernandochang2025.util.Secrets
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission", "SuspiciousIndentation")
@Composable
fun PantallaMapaUbicacion(
    lugarViewModel: LugarViewModel,
    mapViewModel: MapViewModel,
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    desdeDescarga: Boolean = false
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

    var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    val mostrarDialogoGuardar = remember { mutableStateOf(false) }
    val ubicaciones by ubicacionViewModel.ubicaciones.collectAsState()

    var usuarioId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        usuarioId = usuarioViewModel.obtenerIdUsuario()
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            // Mapa como fondo
            MapaSeleccionUbicacionView(
                onUbicacionSeleccionada = { ubicacionSeleccionada = it },
                ubicacionCentro = ubicacionSeleccionada,
                ubicaciones = ubicaciones,
                mapViewModel = mapViewModel,
                onMapReady = { googleMap = it },
                modifier = Modifier.fillMaxSize()
            )


            // Buscador arriba
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .zIndex(2f)
            ) {

                InputDireccionBox(
                    apiKey = Secrets.GOOGLE_MAPS_API_KEY,
                    googleMap = googleMap,
                    onLugarSeleccionado = { lugar ->
                        ubicacionSeleccionada = LatLng(lugar.latitud, lugar.longitud)
                        Log.d("MAPA_UBICACION", "📍 Ubicación seleccionada: ${lugar.latitud}, ${lugar.longitud}")
                    }
                )

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


            }

            // FAB para confirmar
            if (ubicacionSeleccionada != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        mostrarDialogoGuardar.value = true
                        googleMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                ubicacionSeleccionada!!, 15f
                            )
                        )
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Confirmar") },
                    text = { Text("Confirmar zona") },
                    containerColor = Color(0xFFF77C00),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }

            // Diálogo para guardar ubicación
            if (mostrarDialogoGuardar.value && ubicacionSeleccionada != null) {
                val scope = rememberCoroutineScope()
                EditarUbicacionDialog(
                    ubicacion = UbicacionLocal(
                        nombre = "",
                        latitud = ubicacionSeleccionada!!.latitude,
                        longitud = ubicacionSeleccionada!!.longitude,
                        tipo = "provincia",
                        usuarioId = usuarioId
                    ),
                    onDismiss = {
                        mostrarDialogoGuardar.value = false
                        ubicacionSeleccionada = null // ✅ ELIMINA el marcador si se cancela
                    },
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

                                lugarViewModel.actualizarUbicacionManual(
                                    LatLng(ubicacionSeleccionada!!.latitude, ubicacionSeleccionada!!.longitude)
                                )

                                if (desdeDescarga) {
                                    navController.navigate(Ruta.PantallaDescargas.ruta) {
                                        popUpTo(Ruta.PantallaDescargas.ruta) { inclusive = true }
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            } else {
                                Toast.makeText(context, "⚠️ Ya existe una ubicación cercana del mismo tipo", Toast.LENGTH_SHORT).show()
                                ubicacionSeleccionada = null // ✅ Elimina marcador si NO se guarda
                            }

                            mostrarDialogoGuardar.value = false
                        }
                    },
                    onEliminar = {
                        mostrarDialogoGuardar.value = false
                        ubicacionSeleccionada = null // ✅ ELIMINA el marcador si se cancela
                    },
                    onSeleccionarRuta = {
                        mostrarDialogoGuardar.value = false
                        ubicacionSeleccionada = null // ✅ ELIMINA el marcador si se elige otra acción
                    }
                )
            }




            // Botón flotante para centrar en la ubicación actual
            FloatingActionButton(
                onClick = {
                    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                                )
                            } else {
                                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
                        }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }


        }



}

