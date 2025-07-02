package com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places

import com.yucsan.mapgendafernandochang2025.dto.TextSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("type") type: String,
        @Query("key") apiKey: String,
        @Query("radius") radius: Int
    ): NearbyPlacesResponse

    // 🔄 Llamada siguiente solo con el token
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNextPage(
        @Query("pagetoken") pageToken: String,
        @Query("key") apiKey: String
    ): NearbyPlacesResponse
/*
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlacesWithRadius(
        @Query("location") location: String,
        @Query("type") type: String,
        @Query("key") apiKey: String,
        @Query("radius") radius: Int
    ): NearbyPlacesResponse
*/
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlacesByDistance(
        @Query("location") location: String,
        @Query("type") type: String,
        @Query("key") apiKey: String,
        @Query("rankby") rankby: String = "distance"
    ): NearbyPlacesResponse

    @GET("maps/api/place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,
        @Query("key") apiKey: String
    ): TextSearchResponse
}
