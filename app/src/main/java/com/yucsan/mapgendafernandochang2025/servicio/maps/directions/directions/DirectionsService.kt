package com.yucsan.mapgendafernandochang2025.servicio.maps.directions.directions

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectionsService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: DirectionsApiService = retrofit.create(DirectionsApiService::class.java)
}