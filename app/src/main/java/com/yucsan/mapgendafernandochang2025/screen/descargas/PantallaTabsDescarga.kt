package com.yucsan.mapgendafernandochang2025.screen.descargas


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.ThemeViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.*


@Composable
fun PantallaTabsDescarga(
    lugarViewModel: LugarViewModel,
    ubicacionViewModel: UbicacionViewModel,
    rutaViewModel: RutaViewModel,
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val tabs = listOf("Descarga Google", "Mi Nube")
    var selectedTabIndex by remember { mutableStateOf(0) }



    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> PantallaFiltroDescarga(
                    viewModelLugar = lugarViewModel,
                    ubicacionViewModel = ubicacionViewModel,
                    navController = navController,
                    themeViewModel = themeViewModel
                )
                1 -> PantallaDescargaSimple(viewModel = lugarViewModel, ubicacionViewModel = ubicacionViewModel, rutaViewModel = rutaViewModel)
            }
        }
    }
}

