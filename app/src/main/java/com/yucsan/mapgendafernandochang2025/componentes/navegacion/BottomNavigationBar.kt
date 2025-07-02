package com.yucsan.mapgendafernandochang2025.componentes.navegacion


import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import kotlinx.coroutines.flow.StateFlow
import androidx.navigation.NavDestination.Companion.hierarchy


@Composable
fun BottomNavigationBar(navController: NavController, authState: StateFlow<AuthState>) {
    val items = listOf(
        BottomBarScreen.Mapa,
        BottomBarScreen.MenuOffline,
        BottomBarScreen.Descargas,
        BottomBarScreen.PerfilTabs
    )

    val estado by authState.collectAsState()
    val sesionActiva = estado is AuthState.Autenticado
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(modifier = Modifier.navigationBarsPadding()) {
        items.forEach { screen ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == screen.route }
                ?: false

            NavigationBarItem(
                icon    = { Icon(screen.icon, screen.title, Modifier.height(30.dp)) },
                label   = { Text(screen.title) },
                selected = selected,

                onClick = {
                    if (!sesionActiva) return@NavigationBarItem

                    val currentRoute = currentDestination?.route

                    if (screen == BottomBarScreen.MenuOffline) {
                        // 🔁 Forzar reinicio de la sección si no estás en la raíz
                        navController.navigate("menuoffline") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        // 🧭 Navegación normal para otros botones
                        if (!selected) {
                            navController.navigate(screen.route) {
                                popUpTo(screen.route) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                enabled = sesionActiva
            )
        }
    }
}
