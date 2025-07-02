package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUbicacionDialog(
    ubicacion: UbicacionLocal,
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit,
    onEliminar: () -> Unit,
    onSeleccionarRuta: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(ubicacion.nombre) }
    var tipo by remember { mutableStateOf(ubicacion.tipo.ifBlank { "provincia" }) }


    var mostrarConfirmacionBorrado by remember { mutableStateOf(false) }
    val opcionesTipo = listOf("provincia", "país")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Editar ubicación")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = tipo,
                        onValueChange = {},
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        opcionesTipo.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    tipo = opcion
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text("Latitud: ${ubicacion.latitud}")
                Text("Longitud: ${ubicacion.longitud}")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onGuardar(nombre, tipo)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF77C00),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar ubicación")
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { mostrarConfirmacionBorrado = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar ubicación", tint = Color.Red)
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", color = Color.Red)
                }

            }
        },

    )

    if (mostrarConfirmacionBorrado) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionBorrado = false },
            title = { Text("¿Eliminar ubicación?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar()
                    mostrarConfirmacionBorrado = false
                    Toast.makeText(context, "Ubicación eliminada", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
