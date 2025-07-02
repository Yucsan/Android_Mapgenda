package com.yucsan.mapgendafernandochang2025.servicio.maps.directions.directions

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val overview_polyline: OverviewPolyline,
    val legs: List<Leg>
)

data class OverviewPolyline(
    val points: String
)

data class Leg(
    val steps: List<Step>
)

data class Step(
    val html_instructions: String?,
    val start_location: LatLngJson,
    val end_location: LatLngJson
)

data class PasoRuta(
    val instruccion: String,
    val lat: Double,
    val lng: Double
)

data class LatLngJson(
    val lat: Double,
    val lng: Double
)
