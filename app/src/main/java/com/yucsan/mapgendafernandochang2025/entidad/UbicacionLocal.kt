package com.yucsan.mapgendafernandochang2025.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ubicaciones")
data class UbicacionLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val tipo: String, // Solo puede ser "país" o "provincia"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val usuarioId: String?
)
