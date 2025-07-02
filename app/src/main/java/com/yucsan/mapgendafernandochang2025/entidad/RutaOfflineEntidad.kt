package com.yucsan.mapmapgendafernandochang2025.entidad

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal

@Entity(tableName = "rutas")
data class RutaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val categoria: String? = null,
    val ubicacionId: Long? = null,
    val polylineCodificada: String? = null,
    val fechaDeCreacion: Long = System.currentTimeMillis(),
    val usuarioId : String? = null, // ✅ Agregado para relacionar con el usuario
)

@Entity(primaryKeys = ["rutaId", "lugarId"])
data class RutaLugarCrossRef(
    val rutaId: Long,
    val lugarId: String,
    val orden: Int
)

data class RutaConLugares(
    @Embedded val ruta: RutaEntity,
    @Relation(
        parentColumn = "id",                // ← Campo de RutaEntity
        entityColumn = "id",                // ← Campo de LugarLocal
        associateBy = Junction(
            RutaLugarCrossRef::class,
            parentColumn = "rutaId",        // ← Campo de RutaLugarCrossRef que apunta a RutaEntity.id
            entityColumn = "lugarId"        // ← Campo de RutaLugarCrossRef que apunta a LugarLocal.id
        )
    )
    val lugares: List<LugarLocal>
)

data class LugarConOrden(
    @Embedded val lugar: LugarLocal,
    val orden: Int
)

data class RutaConLugaresOrdenados(
    val ruta: RutaEntity,
    val lugares: List<LugarLocal> // extraemos el lugar dentro de LugarConOrden
)


fun UbicacionLocal.toLugarLocalParaRuta(): LugarLocal {
    return LugarLocal(
        id = "ubi_${this.id}", // Evita colisiones con IDs reales de lugares
        nombre = this.nombre,
        direccion = "Ubicación guardada",
        latitud = this.latitud,
        longitud = this.longitud,
        categoriaGeneral = "ubicacion",
        subcategoria = "ubicacion",
        tipos = listOf("ubicacion"),
        rating = null,
        totalReviews = null,
        precio = null,
        abiertoAhora = null,
        estado = "activo",
        photoReference = null,
        businessStatus = null,
        userRatingsTotal = null,
        fuente = "ubicacion_local"
    )
}





