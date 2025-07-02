package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal


@Composable
fun ResumenRutaSeleccionada(
    lugaresSeleccionados: List<LugarLocal>,
    modifier: Modifier = Modifier
) {
    if (lugaresSeleccionados.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
            .zIndex(3f)
    ) {
        Text(
            text = "Lugares seleccionados:",
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        lugaresSeleccionados.forEachIndexed { index, lugar ->
            Text(
                text = "${index + 1}. ${lugar.nombre}",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}