package com.yucsan.mapgendafernandochang2025.servicio.maps.directions.directions

import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("directions/json")
    suspend fun obtenerRuta(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "walking", // walking, driving, bicycling
        @Query("language") language: String = "es",
        @Query("key") apiKey: String
    ): DirectionsResponse


    @GET("directions/json")
    suspend fun obtenerRutaConWaypoints(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String = "walking",
        @Query("language") language: String = "es",
        @Query("key") apiKey: String
    ): DirectionsResponse




}
