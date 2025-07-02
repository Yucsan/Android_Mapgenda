package com.yucsan.mapgendafernandochang2025.dto

import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal


data class LugarDTO(
    val id: String,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val tipo: String,
    val calificacion: Double,
    val fotoUrl: String,
    val abiertoAhora: Boolean,
    val duracionEstimadaMinutos: Int,
    val usuarioId: String? = null // Agregado para asociar con el usuario que lo creó o modificó
)

fun LugarDTO.toEntity(): LugarLocal {
    return LugarLocal(
        id = id,
        nombre = nombre,
        latitud = latitud,
        longitud = longitud,
        direccion = direccion,
        subcategoria = tipo,     // ✅ AQUÍ ESTÁ LA CLAVE
        tipos = null,
        rating = calificacion.toFloat(),
        totalReviews = null,
        precio = null,
        abiertoAhora = abiertoAhora,
        estado = null,
        photoReference = fotoUrl,
        businessStatus = null,
        userRatingsTotal = null,
        fuente = "API", // o "Backend" si quieres diferenciar
        duracionEstimadaMinutos = duracionEstimadaMinutos,
        ultimaActualizacion = System.currentTimeMillis(),
        usuarioId = usuarioId
    )
}


