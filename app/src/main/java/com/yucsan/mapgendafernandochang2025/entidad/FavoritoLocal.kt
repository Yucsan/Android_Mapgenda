package com.yucsan.mapgendafernandochang2025.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "favoritos")
data class FavoritoLocal(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val lugarId: String,
    val usuarioId: UUID,
    val fechaGuardado: Long = System.currentTimeMillis()
)
