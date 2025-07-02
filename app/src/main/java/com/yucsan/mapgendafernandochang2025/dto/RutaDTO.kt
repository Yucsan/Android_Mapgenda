package com.yucsan.mapgendafernandochang2025.dto

data class RutaDTO(
    val id: Long? = null,

    val nombre: String,
    val origenLat: Double?,
    val origenLng: Double?,
    val destinoLat: Double?,
    val destinoLng: Double?,
    val modoTransporte: String?,
    val lugaresIntermedios: String?,
    val polylineCodificada: String?,
    val categoria: String?,
    val ubicacionId: Long?,
    val lugarIdsOrdenados: List<String>,
    val usuarioId: String
)
