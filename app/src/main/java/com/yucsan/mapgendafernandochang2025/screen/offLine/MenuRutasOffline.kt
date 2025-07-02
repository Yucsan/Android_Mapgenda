package com.yucsan.mapgendafernandochang2025.screen.offLine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yucsan.mapgendafernandochang2025.R

@Composable
fun MenuRutaOffline(
    navController: NavController,
    lugarViewModel: LugarViewModel
) {
    val buttonColor = Color(0xFFF5881F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "MENÚ OFFLINE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        BotonOfflineAccion(
            texto = "Seleccionar Ubicación",
            colorFondo = buttonColor
        ) {
            navController.navigate("mapaSeleccionUbicacion")
        }

        BotonOfflineAccion(
            texto = "Ubicaciones",
            colorFondo = buttonColor
        ) {
            navController.navigate("ubicaciones")
        }



        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.vuelo),
                contentDescription = "Mapa del mundo con avión",
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        navController.navigate("mapaubi?modoSeleccionUbicacion=false")
                    }
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Mapa Rutas",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto)), // Asegúrate de tener roboto_regular.ttf en res/font
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }



        BotonOfflineAccion(
            texto = "Gestión de Rutas",
            colorFondo = buttonColor
        ) {
            navController.navigate("rutas")
        }

        BotonOfflineAccion(
            texto = "Gestión de Lugares"
        ) {
            lugarViewModel.agruparLugaresPorZona()
            navController.navigate("listalugares")
        }
    }
}

@Composable
fun BotonOfflineAccion(
    texto: String,
    colorFondo: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorFondo),
        shape = MaterialTheme.shapes.medium // mismo estilo cuadrado suave
    ) {
        Text(
            text = texto,
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

