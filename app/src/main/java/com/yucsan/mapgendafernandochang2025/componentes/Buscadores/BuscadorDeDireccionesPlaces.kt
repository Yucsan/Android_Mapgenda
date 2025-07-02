package com.yucsan.mapgendafernandochang2025.componentes.Buscadores



import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


/*
//--------------------------------------------------------------------- INPUT DIRECCION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(1f)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
        ) {
            BuscadorDeDirecciones(
                apiKey = apiKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) { latLng ->
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Ubicación buscada")
                )
            }
        }

//----------------------------------------------------------------------
* */
/*
@Composable
fun BuscadorPlacesAPI2(
    apiKey: String,
    modifier: Modifier,
    onUbicacionSeleccionada: (LatLng) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (query.length >= 3) {
                    scope.launch {
                        resultados = buscarSugerencias(query, apiKey)
                    }
                } else {
                    resultados = emptyList()
                }
            },
            label = { Text("Buscar lugar o dirección...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        resultados.forEach { (nombre, latLng) ->
            TextButton(
                onClick = {
                    onUbicacionSeleccionada(latLng)
                    query = ""
                    resultados = emptyList()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(nombre)
            }
        }
    }
}*/
/*
suspend fun buscarSugerencias(input: String, apiKey: String): List<Pair<String, LatLng>> {
    return withContext(Dispatchers.IO) {
        val url = "https://maps.googleapis.com/maps/api/place/autocomplete/json" +
                "?input=${Uri.encode(input)}&key=$apiKey&types=geocode&language=es"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val body = response.body?.string() ?: return@withContext emptyList()
        val json = JSONObject(body)
        val predictions = json.optJSONArray("predictions") ?: return@withContext emptyList()

        val results = mutableListOf<Pair<String, LatLng>>()

        for (i in 0 until predictions.length()) {
            val pred = predictions.getJSONObject(i)
            val placeId = pred.getString("place_id")
            val name = pred.getString("description")

            val locUrl = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&key=$apiKey"
            val locRequest = Request.Builder().url(locUrl).build()
            val locResponse = client.newCall(locRequest).execute()
            val locBody = locResponse.body?.string() ?: continue
            val locJson = JSONObject(locBody)
            val latLngObj = locJson.getJSONObject("result").getJSONObject("geometry").getJSONObject("location")

            val lat = latLngObj.getDouble("lat")
            val lng = latLngObj.getDouble("lng")

            results.add(name to LatLng(lat, lng))
        }

        return@withContext results
    }
}
*/
// Llama a este componente dentro de PantallaMapa:
// BuscadorDeDirecciones(apiKey = apiKey) { latLng ->
//     googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
//     googleMap?.addMarker(MarkerOptions().position(latLng).title("Ubicación buscada"))
// }
