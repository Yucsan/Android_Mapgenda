package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng

@Composable
fun DialogoDescargaMapas(
    mostrar: Boolean,
    ubicacionSeleccionada: LatLng?,
    context: Context,
    onCerrar: () -> Unit
) {
    if (!mostrar) return

    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(onClick = {
                onCerrar()
                ubicacionSeleccionada?.let { ubicacion ->
                    val lat = ubicacion.latitude
                    val lng = ubicacion.longitude
                    val uri = "geo:$lat,$lng?q=$lat,$lng"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    intent.setPackage("com.google.android.apps.maps")
                    context.startActivity(intent)
                }
            }) {
                Text("Abrir Google Maps")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        },
        title = { Text("Descargar mapa sin conexión") },
        text = {
            Text(
                """
                1. Toca tu perfil en Google Maps.
                2. Elige “Mapas sin conexión”.
                3. Presiona “Seleccionar tu propio mapa”.
                4. Ajusta el área y toca ‘Descargar’.
                """.trimIndent()
            )
        }
    )
}
