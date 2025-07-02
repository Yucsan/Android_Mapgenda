package com.yucsan.mapgendafernandochang2025.componentes.navegacion


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Mapa : BottomBarScreen("mapacompose", "Mapa Online", Icons.Filled.Map)
    object MenuOffline : BottomBarScreen("menuoffline", "Menu Offline", Icons.Filled.CloudDownload)
    object Descargas : BottomBarScreen("descargas", "Descargas", Icons.Filled.Download)
    object PerfilTabs : BottomBarScreen("perfil", "Perfil", Icons.Filled.Person)
}
