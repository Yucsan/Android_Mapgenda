package com.yucsan.mapgendafernandochang2025.dto

import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal

data class UbicacionDTO(
    val id: String? = null, // UUID generado por backend
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val tipo: String, // "país" o "provincia"
    val fechaCreacion: Long,
    val usuarioId: String
)

fun UbicacionLocal.toDTO(usuarioId: String): UbicacionDTO {
    return UbicacionDTO(
        nombre = this.nombre,
        latitud = this.latitud,
        longitud = this.longitud,
        tipo = this.tipo,
        fechaCreacion = this.fechaCreacion,
        usuarioId = usuarioId
    )
}

fun UbicacionDTO.toEntity(): UbicacionLocal {
    return UbicacionLocal(
        nombre = this.nombre,
        latitud = this.latitud,
        longitud = this.longitud,
        tipo = this.tipo,
        fechaCreacion = this.fechaCreacion,
        usuarioId = this.usuarioId
    )
}



