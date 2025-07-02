// MainNavigation.kt
package com.yucsan.mapgendafernandochang2025.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.ui.PantallaFiltroOffline
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel

import com.yucsan.mapgendafernandochang2025.ThemeViewModel
import com.yucsan.mapgendafernandochang2025.screen.descargas.PantallaTabsDescarga
import com.yucsan.mapgendafernandochang2025.screen.perfil.PantallaPerfil
import com.yucsan.mapgendafernandochang2025.screen.iniciarMapa

import com.yucsan.mapgendafernandochang2025.screen.lugares.PantallaListadoLugares
import com.yucsan.mapgendafernandochang2025.screen.offLine.MenuRutaOffline
import com.yucsan.mapgendafernandochang2025.screen.offLine.PantallaMapaOffline
import com.yucsan.mapgendafernandochang2025.screen.offLine.PantallaMapaUbicacion
import com.yucsan.mapgendafernandochang2025.screen.offLine.PantallaRutas
import com.yucsan.mapgendafernandochang2025.screen.offLine.PantallaUbicaciones
import com.yucsan.mapgendafernandochang2025.screen.offLine.PantallaVerRutaOffline

import com.yucsan.mapgendafernandochang2025.screen.onLine.PantallaFiltro
import com.yucsan.mapgendafernandochang2025.screen.onLine.PantallaMapaCompose
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import com.yucsan.mapgendafernandochang2025.viewmodel.*

@Composable
fun MainNavigation(
    navController: NavHostController,
    lugarViewModel: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    mapViewModel: MapViewModel,
    gpsViewModel: GPSViewModel,
    themeViewModel: ThemeViewModel,
    ubicacionViewModel: UbicacionViewModel,
    lugareOfflineViewModel: LugarRutaOfflineViewModel,
    rutaViewModel: RutaViewModel,
    usuarioViewModel: UsuarioViewModel,
    authViewModel: AuthViewModel,
    networkMonitor: NetworkMonitor
) {
    NavHost(
        navController = navController,
        startDestination = Ruta.Pantalla1.ruta
    ) {
        composable(Ruta.Pantalla1.ruta) {
            iniciarMapa(
                lugarViewModel = lugarViewModel,
                navegacionViewModel = navegacionViewModel,
                mapViewModel = mapViewModel,
                navController = navController,
                gpsViewModel = gpsViewModel,
                themeViewModel = themeViewModel,
                usuarioViewModel = usuarioViewModel,
                authViewModel = authViewModel,
                networkMonitor = networkMonitor,
                ubicacionViewModel = ubicacionViewModel
            )
        }

        composable(Ruta.Filtro.ruta) {
            PantallaFiltro(
                viewModelLugar = lugarViewModel,
                ubicacionViewModel = ubicacionViewModel,
                navController = navController
            )
        }

        composable(Ruta.MapaCompose.ruta) {
            PantallaMapaCompose(
                lugarViewModel,
                navegacionViewModel,
                mapViewModel,
                navController,
                themeViewModel,
                networkMonitor,
                ubicacionViewModel,
                authViewModel
            )
        }

        composable(Ruta.PantallaDescargas.ruta) {
            PantallaTabsDescarga(
                lugarViewModel = lugarViewModel,
                ubicacionViewModel = ubicacionViewModel,
                navController = navController,
                themeViewModel = themeViewModel,
                rutaViewModel = rutaViewModel
            )

        }

        composable(Ruta.PantallaPerfil.ruta) {
            PantallaPerfil(
                lugarViewModel,
                usuarioViewModel,
                navController,
                authViewModel = authViewModel
            )
        }

        composable(Ruta.MenuOffline.ruta) {
            MenuRutaOffline(
                navController,
                lugarViewModel
            )
        }

        composable(
            route = "mapaubi?modoSeleccionUbicacion={modoSeleccionUbicacion}&modoCrearRuta={modoCrearRuta}",
            arguments = listOf(
                navArgument("modoSeleccionUbicacion") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("modoCrearRuta") { defaultValue = "false" }
            )
        ) { backStackEntry ->
            val modoSeleccionUbicacion = backStackEntry.arguments?.getBoolean("modoSeleccionUbicacion") ?: false
            val crearRuta = backStackEntry.arguments?.getString("modoCrearRuta") == "true"

            PantallaMapaOffline(
                lugarRutaOfflineViewModel = lugareOfflineViewModel,
                lugarViewModel = lugarViewModel,
                mapViewModel = mapViewModel,
                navController = navController,
                rutaViewModel = rutaViewModel,
                modoSeleccionUbicacion = modoSeleccionUbicacion,
                onUbicacionConfirmada = if (modoSeleccionUbicacion) { latLng ->
                    lugarViewModel.actualizarUbicacionManual(latLng)
                    lugareOfflineViewModel.actualizarUbicacionManual(latLng)
                } else null,
                ubicacionViewModel = ubicacionViewModel,
                modoCrearRuta = crearRuta,
                usuarioViewModel = usuarioViewModel
            )
        }

        composable("filtrooffline") {
            PantallaFiltroOffline(lugareOfflineViewModel,lugarViewModel, ubicacionViewModel, navController)
        }

        composable(
            route = "mapaSeleccionUbicacion?desdeDescarga={desdeDescarga}",
            arguments = listOf(
                navArgument("desdeDescarga") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val desdeDescarga = backStackEntry.arguments?.getBoolean("desdeDescarga") ?: false

            PantallaMapaUbicacion(
                lugarViewModel = lugarViewModel,
                mapViewModel = mapViewModel,
                navController = navController,
                ubicacionViewModel = ubicacionViewModel,
                desdeDescarga = desdeDescarga,
                usuarioViewModel = usuarioViewModel
            )
        }

        composable("rutas") {
            PantallaRutas(rutaViewModel, navController)
        }

        composable("ubicaciones") {
            PantallaUbicaciones(
                viewModel = ubicacionViewModel,
                navController= navController
            )
        }

        composable("verRutaOffline/{rutaId}") { backStackEntry ->
            val rutaId = backStackEntry.arguments?.getString("rutaId")?.toLongOrNull() ?: return@composable
            PantallaVerRutaOffline(
                rutaId = rutaId,
                rutaViewModel = rutaViewModel,
                navController = navController
            )
        }

        composable ( "listalugares" ) {
            PantallaListadoLugares(
                lugarRutaOfflineViewModel = lugareOfflineViewModel,
                ubicacionViewModel = ubicacionViewModel,
                lugarViewModel = lugarViewModel,
                navegacionViewModel = navegacionViewModel,
                networkMonitor = networkMonitor,
                navController = navController,
                authViewModel = authViewModel,
            )

        }

    }
}
