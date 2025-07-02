package com.yucsan.mapgendafernandochang2025.componentes



sealed class Ruta(val ruta:String) {

    object Pantalla1:Ruta("pantalla1")
    object Filtro : Ruta("filtro")
    object MapaCompose:Ruta("mapacompose")

    object MenuOffline:Ruta("menuoffline")

    object PantallaDescargas:Ruta("descargas")

    object PantallaPerfil:Ruta("perfil")

    object PantallaZonas:Ruta("lugares")

    //Mapa OffLine


}

