package com.yucsan.mapgendafernandochang2025.util


import android.location.Location

fun distanciaMetros(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val resultado = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, resultado)
    return resultado[0] // en metros
}
