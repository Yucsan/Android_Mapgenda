package com.yucsan.mapgendafernandochang2025.componentes.cajasTexto


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.yucsan.mapgendafernandochang2025.componentes.Buscadores.BuscadorGeocoding
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal

import java.util.UUID

@Composable
fun InputDireccionBox(
    apiKey: String,
    googleMap: GoogleMap?,
    onLugarSeleccionado: (LugarLocal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            //.background(Color.White.copy(alpha = 0.3f), shape = MaterialTheme.shapes.medium)
    ) {
        BuscadorGeocoding(
            apiKey = apiKey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) { latLng, direccionBuscada ->

            val lugarTemporal = LugarLocal(
                id = UUID.randomUUID().toString(),
                nombre = direccionBuscada,
                direccion = direccionBuscada,
                latitud = latLng.latitude,
                longitud = latLng.longitude,
                categoriaGeneral = "custom",
                subcategoria = "custom",
                tipos = listOf("custom"),
                rating = null,
                totalReviews = null,
                precio = null,
                abiertoAhora = null,
                estado = null,
                photoReference = null,
                businessStatus = null,
                userRatingsTotal = null,
                fuente = "Geocoding"
            )

            onLugarSeleccionado(lugarTemporal)

            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(direccionBuscada)
            )

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }
}
