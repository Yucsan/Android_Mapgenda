package com.yucsan.mapgendafernandochang2025.componentes.navegacion


sealed class NavRoutes(val route: String) {
    object Mapa : NavRoutes("mapa")
    object Filtro : NavRoutes("filtro")
    object Favoritos : NavRoutes("favoritos")
    object PantallaOffline : NavRoutes("menuoffline")
    object Perfil : NavRoutes("perfil")
    object FiltroDescarga : NavRoutes("filtrodescarga")

    object MapaOffline : NavRoutes("mapaubi?modoSeleccionUbicacion={modoSeleccionUbicacion}&modoCrearRuta={modoCrearRuta}") {
        fun crearRuta(
            modoSeleccionUbicacion: Boolean = false,
            modoCrearRuta: Boolean = false
        ): String {
            return "mapaubi?modoSeleccionUbicacion=$modoSeleccionUbicacion&modoCrearRuta=$modoCrearRuta"
        }
    }

}
