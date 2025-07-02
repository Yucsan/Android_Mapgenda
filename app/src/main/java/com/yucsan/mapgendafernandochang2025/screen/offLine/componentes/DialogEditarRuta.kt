package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares


@Composable
fun DialogEditarRuta(
    ruta: RutaConLugares,
    onDismiss: () -> Unit,
    onGuardar: (String, String?) -> Unit
) {
    var nuevoNombre by remember { mutableStateOf(ruta.ruta.nombre) }
    var nuevaCategoria by remember { mutableStateOf(ruta.ruta.categoria ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ruta") },
        text = {
            Column {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nuevaCategoria,
                    onValueChange = { nuevaCategoria = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(nuevoNombre, nuevaCategoria.ifBlank { null })
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
