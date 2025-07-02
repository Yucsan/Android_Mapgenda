package com.yucsan.mapgendafernandochang2025.screens.mapa.alertas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import com.yucsan.mapgendafernandochang2025.componentes.navegacion.DropdownMenuBox
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AgregarLugarDialog(
    latLngSeleccionado: LatLng,
    categoriasDisponibles: List<String>,
    onGuardar: (LugarLocal) -> Unit,
    onDismiss: () -> Unit,
    mapScope: CoroutineScope,
    googleMap: GoogleMap?,
    guardarCamara: (camara: com.google.android.gms.maps.model.CameraPosition) -> Unit
) {
    var nombreNuevoLugar by remember { mutableStateOf("") }
    var descripcionNuevoLugar by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(categoriasDisponibles.last()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                val nuevoLugar = LugarLocal(
                    id = UUID.randomUUID().toString(),
                    nombre = nombreNuevoLugar.ifBlank { "Nuevo lugar" },
                    direccion = descripcionNuevoLugar.ifBlank { "Sin dirección" },
                    latitud = latLngSeleccionado.latitude,
                    longitud = latLngSeleccionado.longitude,
                    categoriaGeneral = categoriaSeleccionada,
                    subcategoria = categoriaSeleccionada,
                    tipos = listOf(categoriaSeleccionada),
                    rating = null,
                    totalReviews = null,
                    precio = null,
                    abiertoAhora = null,
                    estado = null,
                    photoReference = null,
                    businessStatus = null,
                    userRatingsTotal = null,
                    fuente = "Local"
                )

                onGuardar(nuevoLugar)

                googleMap?.let { map ->
                    mapScope.launch {
                        map.addMarker(
                            com.google.android.gms.maps.model.MarkerOptions()
                                .position(latLngSeleccionado)
                                .title(nuevoLugar.nombre)
                                .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN))
                        )
                        map.cameraPosition?.let { guardarCamara(it) }
                    }
                }

                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        },
        title = { Text("Agregar nuevo lugar") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombreNuevoLugar,
                    onValueChange = { nombreNuevoLugar = it },
                    label = { Text("Nombre del lugar") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcionNuevoLugar,
                    onValueChange = { descripcionNuevoLugar = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Categoría:")
                DropdownMenuBox(
                    options = categoriasDisponibles,
                    selectedOption = categoriaSeleccionada,
                    onOptionSelected = { categoriaSeleccionada = it }
                )
            }
        }
    )
}
