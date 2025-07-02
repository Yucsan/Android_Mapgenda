package com.yucsan.mapgendafernandochang2025.componentes.navegacion


import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun DialogoConfirmacionBorrado(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Borrar todo?") },
        text = { Text("Esto eliminará todos los lugares guardados. ¿Estás seguro?") },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Sí, borrar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
