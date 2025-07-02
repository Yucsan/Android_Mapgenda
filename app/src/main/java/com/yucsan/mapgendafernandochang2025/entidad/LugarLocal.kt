package com.yucsan.mapgendafernandochang2025.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "lugares")
data class LugarLocal(
    @PrimaryKey val id: String,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val categoriaGeneral: String? = null,
    val subcategoria: String = "",
    var tipos: List<String>? = null,
    val rating: Float? = null,
    val totalReviews: Int? = null,
    val precio: Int? = null,
    val abiertoAhora: Boolean? = null,
    val estado: String? = null,
    val photoReference: String? = null,
    val businessStatus: String? = null,
    val userRatingsTotal: Int? = null,
    val fuente: String = "Google",
    val duracionEstimadaMinutos: Int = 0,
    val ultimaActualizacion: Long = System.currentTimeMillis(),
    val usuarioId: String? = null // ✅ este faltaba también
)


data class SubcategoriaConteo(
    val subcategoria: String,
    val cantidad: Int
)




