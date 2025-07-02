package com.yucsan.mapgendafernandochang2025.componentes.Buscadores

import android.net.Uri
import android.widget.Toast
import androidx.benchmark.perfetto.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun BuscadorGeocoding(
    apiKey: String,
    modifier: Modifier = Modifier,
    onUbicacionEncontrada: (LatLng, String) -> Unit  // <--- cambio aquí
) {
    var direccion by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // 🔥 Fondo difuminado
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.medium) // bordes redondeados
                .background( Color.White.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                )
        )
        // 🔥 Contenido normal encima del fondo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=0.dp, bottom=5.dp)
                .padding(horizontal = 15.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Buscador", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                cursorColor = Color.Black
                )
            )

            IconButton(
                onClick = {
                    if (direccion.isNotBlank()) {
                        scope.launch {
                            val latLng = obtenerLatLngPorGeocoding(direccion, apiKey)
                            if (latLng != null) {
                                onUbicacionEncontrada(latLng, direccion) // <--- le mandamos LatLng y dirección
                                keyboardController?.hide() // <-- esto oculta el teclado
                            } else {
                                Toast.makeText(context, "No se encontró ubicación", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.White
                )
            }
        }
    }
}



suspend fun obtenerLatLngPorGeocoding(direccion: String, apiKey: String): LatLng? {
    return withContext(Dispatchers.IO) {
        val encodedDireccion = Uri.encode(direccion)
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedDireccion&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext null

        val json = JSONObject(body)
        val resultados = json.optJSONArray("results")
        if (resultados != null && resultados.length() > 0) {
            val location = resultados.getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONObject("location")
            val lat = location.getDouble("lat")
            val lng = location.getDouble("lng")
            return@withContext LatLng(lat, lng)
        }
        return@withContext null
    }
}
