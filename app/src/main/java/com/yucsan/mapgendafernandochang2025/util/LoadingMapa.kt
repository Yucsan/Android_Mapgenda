package com.yucsan.mapgendafernandochang2025.util


import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel


@Composable
fun CargandoUbicacionYMapa(
    ubicacionViewModel: UbicacionViewModel,
    contenidoMapa: @Composable () -> Unit
) {
    // 1️⃣ Recoge la primera ubicación (null hasta que llegue)
    val primeraUbicacion by ubicacionViewModel.ubicacionActual.collectAsState(initial = null)

    // 2️⃣ Si aún no hay ubicación, mostramos loader
    if (primeraUbicacion == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // 3️⃣ En cuanto llegue, montamos el mapa
        contenidoMapa()
    }
}
