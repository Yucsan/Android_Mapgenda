package com.yucsan.mapgendafernandochang2025.dto

import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places.Geometry
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places.OpeningHours
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places.Photo

data class TextSearchResponse(
    val results: List<TextSearchPlaceResult>,
    val status: String,
    val next_page_token: String? = null
)

data class TextSearchPlaceResult(
    val place_id: String,
    val name: String,
    val formatted_address: String,
    val types: List<String>,
    val geometry: Geometry,
    val rating: Float?,
    val price_level: Int?,
    val business_status: String?,
    val user_ratings_total: Int?,
    val opening_hours: OpeningHours?,
    val photos: List<Photo>?
)
