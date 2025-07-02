package com.yucsan.mapgendafernandochang2025.entidad


import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places.PlaceResult

data class PlacesResponse(
    val results: List<PlaceResult>,
    val next_page_token: String? = null // 👈 ¡Este!
)
