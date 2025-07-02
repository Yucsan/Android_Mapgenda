package com.yucsan.mapgendafernandochang2025.screens.rutasoffline.componentes

import com.google.android.gms.maps.model.LatLng



import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BotonesAcciones(
    context: Context,
    navController: NavController,
    ubicacionSeleccionada: LatLng?,
    modoAgregarLugar: Boolean,
    onModoAgregarLugar: () -> Unit,
    modoCrearRuta: Boolean,
    onModoCrearRuta: () -> Unit,
    onDescargarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier.padding(end = 16.dp, bottom = 100.dp)
    ) {
        FloatingActionButton(
            onClick = {
                navController.navigate("filtrooffline")
            }
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filtrar lugares")
        }

        FloatingActionButton(
            onClick = onModoAgregarLugar,
            containerColor = if (modoAgregarLugar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (modoAgregarLugar) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text("+")
        }

        FloatingActionButton(
            onClick = onModoCrearRuta,
            containerColor = if (modoCrearRuta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (modoCrearRuta) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(Icons.Default.Directions, contentDescription = "Crear Ruta")
        }

        FloatingActionButton(
            onClick = {
                if (ubicacionSeleccionada != null) {
                    onDescargarClick()
                } else {
                    Toast.makeText(context, "Selecciona una ubicación primero", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = "Descargar Mapa")
        }
    }
}
