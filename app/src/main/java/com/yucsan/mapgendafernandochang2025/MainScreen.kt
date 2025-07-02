package com.yucsan.mapgendafernandochang2025


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.yucsan.mapgendafernandochang2025.componentes.navegacion.BottomNavigationBar
import com.yucsan.mapgendafernandochang2025.navigation.MainNavigation
import com.yucsan.mapgendafernandochang2025.viewmodel.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel

@Composable
fun MainScreen(
    lugarViewModel: LugarViewModel,
    mapViewModel: MapViewModel,
    navegacionViewModel: NavegacionViewModel,
    gpsViewModel: GPSViewModel,
    themeViewModel: ThemeViewModel,
    ubicacionViewModel: UbicacionViewModel,
    lugareOfflineViewModel: LugarRutaOfflineViewModel,
    rutaViewModel: RutaViewModel,
    usuarioViewModel: UsuarioViewModel,
    authViewModel: AuthViewModel,
    networkMonitor: NetworkMonitor,
) {
    val navController = rememberNavController()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController, authViewModel.authState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            MainNavigation(
                navController = navController,
                lugarViewModel = lugarViewModel,
                mapViewModel = mapViewModel,
                navegacionViewModel = navegacionViewModel,
                gpsViewModel = gpsViewModel,
                themeViewModel = themeViewModel,
                ubicacionViewModel = ubicacionViewModel,
                lugareOfflineViewModel = lugareOfflineViewModel,
                rutaViewModel = rutaViewModel,
                usuarioViewModel = usuarioViewModel,
                authViewModel = authViewModel,
                networkMonitor = networkMonitor
            )
        }
    }
}