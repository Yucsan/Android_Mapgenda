package com.yucsan.mapgendafernandochang2025.util.config

object ApiConfig {

    enum class ApiEnvironment(val url: String) {
        LOCAL("http://192.168.0.11:8080/aventura/"),
        RENDER("https://backend-mapgenda.onrender.com/")
    }

    // Cambia esto manualmente para seleccionar el entorno
    var currentEnvironment: ApiEnvironment = ApiEnvironment.LOCAL

    val BASE_URL: String
        get() = currentEnvironment.url
}