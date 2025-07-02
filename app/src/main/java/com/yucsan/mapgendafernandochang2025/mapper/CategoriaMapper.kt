package com.yucsan.mapgendafernandochang2025.util

object CategoriaMapper {

    fun subcategoriasPorCategoria(categoria: String): List<String> {
        return when (categoria) {
            "Comida" -> listOf("restaurant", "cafe","bakery")
            "Compras" -> listOf("clothing_store", "supermarket")
            "Aire libre" -> listOf("park", "tourist_attraction")
            "Cultura" -> listOf("museum", "art_gallery", "aquarium","stadium")
            "Ocio" -> listOf("night_club", "movie_theater", "casino")
            "Hospedaje" -> listOf("lodging")
            "Transporte" -> listOf("bus_station", "train_station", "subway_station")
            else -> listOf(categoria) // fallback: se usa el tipo tal cual
        }
    }
}
