package com.yucsan.mapgendafernandochang2025.screen.offLine


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespuestaFiltroOffline(lugarRutaOfflineViewModel: LugarRutaOfflineViewModel) {
    val todosLosLugares by lugarRutaOfflineViewModel.lugaresOffline.collectAsState()
    // Agrupar por categoría general
    val agrupados = todosLosLugares.groupBy { it.categoriaGeneral ?: "Sin categoría" }

    LaunchedEffect(todosLosLugares) {
        Log.d("UI_RESULTADO", "📦 UI recibió lugares: ${todosLosLugares.size}")
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        agrupados.forEach { (categoria, lista) ->
            item {
                Text(
                    text = "🔹 $categoria (${lista.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            itemsIndexed(lista) { index, lugar ->
                Text(" ${index + 1}. ${lugar.nombre} - ${lugar.direccion}")
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        item {
            Text(
                text = "📦 Total lugares: ${todosLosLugares.size}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
