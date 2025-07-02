package com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places



data class NearbyPlacesResponse(
    val results: List<PlaceResult>,
    val next_page_token: String? = null,
    val status: String
)

data class PlaceResult(
    val place_id: String,
    val name: String,
    val types: List<String>,
    val vicinity: String,
    val geometry: Geometry,
    val rating: Float?, // ⭐️ Rating promedio
    val price_level: Int?, // 💰 Nivel de precio
    val business_status: String?, // 🏢 Estado del negocio (OPERATIONAL, CLOSED_TEMPORARILY...)
    val user_ratings_total: Int?, // 👥 Total de reseñas
    val opening_hours: OpeningHours?, // ⏰ Info sobre apertura
    val photos: List<Photo>? // 📸 Lista de fotos disponibles
)

data class Geometry(
    val location: LocationData
)

data class LocationData(
    val lat: Double,
    val lng: Double
)

data class Photo(
    val photo_reference: String
)

data class OpeningHours(
    val open_now: Boolean?
)

