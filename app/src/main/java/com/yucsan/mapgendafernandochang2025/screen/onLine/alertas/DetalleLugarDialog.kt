package com.yucsan.mapgendafernandochang2025.screens.mapa.alertas

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.Polyline
import com.yucsan.mapgendafernandochang2025.R
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.util.CloudinaryUploader
import com.yucsan.mapgendafernandochang2025.viewmodel.AuthViewModel

import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.NavegacionViewModel
import kotlinx.coroutines.launch


@Composable
fun DetalleLugarDialog(
    lugar: LugarLocal,
    apiKey: String,
    viewModelLugar: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    onDismiss: () -> Unit,
    ubicacionActual: Pair<Double, Double>?,
    hayConexion: Boolean,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var modoEdicion by remember { mutableStateOf(false) }
    var nombreEditado by remember { mutableStateOf("") }
    var descripcionEditada by remember { mutableStateOf("") }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    var nuevaFotoUri by remember { mutableStateOf<Uri?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Control de cambios
    var hayCambios by remember { mutableStateOf(false) }

    // Inicializar valores cuando se entra en modo edición
    LaunchedEffect(modoEdicion) {
        if (modoEdicion) {
            nombreEditado = lugar.nombre
            descripcionEditada = lugar.direccion
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            nuevaFotoUri = it
            hayCambios = true
        }
    }

    // Observar cambios en los campos de texto
    LaunchedEffect(nombreEditado, descripcionEditada) {
        hayCambios = nombreEditado != lugar.nombre || descripcionEditada != lugar.direccion
    }

    fun construirUrlFoto(photoReference: String, apiKey: String, maxWidth: Int = 600): String {
        return if (photoReference.startsWith("http")) {
            photoReference
        } else {
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=$apiKey"
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {},
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 8.dp)
                ) {
                    if (nuevaFotoUri != null) {
                        // Mostrar la nueva imagen seleccionada
                        val painter = rememberAsyncImagePainter(
                            model = nuevaFotoUri,
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Nueva imagen del lugar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (lugar.photoReference != null && hayConexion) {
                        // Mostrar la imagen existente
                        val url = if (lugar.photoReference.startsWith("http")) {
                            lugar.photoReference
                        } else {
                            construirUrlFoto(lugar.photoReference, apiKey)
                        }
                        val painter = rememberAsyncImagePainter(
                            model = url,
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Imagen del lugar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Mostrar mensaje cuando no hay imagen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ){
                                Image(
                                    painter = painterResource(id = R.drawable.sinfoto),
                                    contentDescription = "Imagen no disponible",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(150.dp),
                                    colorFilter = ColorFilter.tint(Color.Black)
                                )
                                Text(
                                    text = "Sin imagen disponible",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }

                        }
                    }
                }

                if (modoEdicion) {
                    OutlinedTextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcionEditada,
                        onValueChange = { descripcionEditada = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )


                        OutlinedButton(
                            onClick = { pickImageLauncher.launch(arrayOf("image/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                                shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Cambiar foto",
                                modifier = Modifier.padding(vertical= 8.dp))
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    cargando = true
                                    try {
                                        // Primero actualizar la foto si hay una nueva
                                        val nuevaUrl = nuevaFotoUri?.let {
                                            CloudinaryUploader.subirImagenDesdeUri(
                                                context,
                                                it,
                                                "lugares"
                                            )
                                        }

                                        val jwt = authViewModel.getTokenSeguro(context)

                                        if (!nuevaUrl.isNullOrBlank() && !jwt.isNullOrBlank()) {

                                            // Eliminar la imagen anterior si era una URL completa
                                            val fotoAnterior = lugar.photoReference
                                            if (!fotoAnterior.isNullOrBlank() && fotoAnterior.startsWith("http")) {
                                                val publicId = CloudinaryUploader.extraerPublicIdDesdeUrl(fotoAnterior)
                                                if (!publicId.isNullOrBlank()) {
                                                    CloudinaryUploader.eliminarImagenDesdeBackend(publicId, jwt)
                                                }
                                            }

                                            viewModelLugar.actualizarFotoLugar(
                                                lugar.id,
                                                nuevaUrl,
                                                jwt
                                            )
                                        }

                                        // Luego actualizar el lugar con los nuevos datos
                                        val lugarActualizado = lugar.copy(
                                            nombre = nombreEditado,
                                            direccion = descripcionEditada,
                                            photoReference = nuevaUrl ?: lugar.photoReference
                                        )
                                        viewModelLugar.actualizarLugarLocal(lugarActualizado)

                                        Toast.makeText(
                                            context,
                                            "Cambios guardados",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        cargando = false
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = hayCambios,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                                shape = MaterialTheme.shapes.medium

                        ) {
                            Text("Guardar cambios",
                                modifier = Modifier.padding(vertical= 8.dp))
                        }

                } else {
                    Text("📍 Dirección: ${lugar.direccion}")
                    lugar.rating?.let { Text("⭐️ Rating: $it") }
                    lugar.userRatingsTotal?.let { Text("🗣️ Opiniones: $it") }
                    lugar.precio?.let { Text("💰 Precio: ${"$".repeat(it)}") }
                    lugar.abiertoAhora?.let {
                        Text("⏰ Abierto ahora: ${if (it) "Sí" else "No"}")
                    }
                    lugar.businessStatus?.let { Text("📌 Estado: $it") }
                    lugar.tipos?.let { Text("🏷️ Tipos: ${it.joinToString()}") }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = {
                            modoEdicion = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }

                        IconButton(
                            onClick = {
                                ubicacionActual?.let { origen ->
                                    val destino = lugar.latitud to lugar.longitud
                                    val uri =
                                        Uri.parse("http://maps.google.com/maps?saddr=${origen.first},${origen.second}&daddr=${destino.first},${destino.second}&mode=w")
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Google Maps no está instalado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            enabled = hayConexion
                        ) {
                            Icon(
                                Icons.Default.Directions,
                                contentDescription = "Abrir en Google Maps"
                            )
                        }

                        IconButton(onClick = {
                            mostrarConfirmacionEliminar = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    )


    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModelLugar.eliminarLugar(lugar.id)
                    mostrarConfirmacionEliminar = false
                    onDismiss()
                    Toast.makeText(context, "Lugar eliminado", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("¿Eliminar lugar?") },
            text = { Text("¿Seguro que deseas eliminar este lugar? Esta acción no se puede deshacer.") }
        )
    }
}
