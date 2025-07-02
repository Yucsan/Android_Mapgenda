package com.yucsan.mapgendafernandochang2025.screen.offLine.componentes

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yucsan.mapgendafernandochang2025.R

@Composable
fun PantallaGuiaDescargaOffline(
    lat: Double,
    lng: Double,
    onOmitir: () -> Unit,
    onDescargaCompletada: () -> Unit,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // ✅ Botones siempre arriba
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOmitir,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lo haré después")
                    }

                    Button(
                        onClick = {
                            // Abrir Google Maps y luego navegar
                            onDescargaCompletada()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✅ Ya descargué el mapa")
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("geo:$lat,$lng?z=14")
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ir a Google Maps para descargar")
                    }
                }
            }

            item {
                Text(
                    "¡Ruta guardada con éxito!",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    "Para usar tu ruta sin conexión, descarga el mapa de la zona ahora.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Image(
                    painter = painterResource(R.drawable.travel),
                    contentDescription = "Pasos para descargar mapa offline",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("1. Abre Google Maps y toca tu foto de perfil")
                    Text("2. Toca “Mapas sin conexión”")
                    Text("3. Elige “Selecciona tu propio mapa”")
                    Text("4. Mueve y ajusta el área, luego toca “Descargar”")
                }
            }


        }
    }
}

