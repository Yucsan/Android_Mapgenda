package com.yucsan.aventurafernandochang2025.room

import com.yucsan.aventurafernandochang2025.room.Converters
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaEntity
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaLugarCrossRef



import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yucsan.mapgendafernandochang2025.dao.FavoritoDao
import com.yucsan.mapgendafernandochang2025.dao.LugarDao
import com.yucsan.mapgendafernandochang2025.dao.RutaDao
import com.yucsan.mapgendafernandochang2025.dao.UbicacionDao
import com.yucsan.mapgendafernandochang2025.dao.UsuarioDao
import com.yucsan.mapgendafernandochang2025.entidad.FavoritoLocal
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal


import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

@Database(
    entities = [
        FavoritoLocal::class,
        LugarLocal::class,
        UbicacionLocal::class,
        RutaEntity::class,
        RutaLugarCrossRef::class,
        UsuarioEntity::class
    ],
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritoDao(): FavoritoDao
    abstract fun lugarDao(): LugarDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun rutaDao(): RutaDao
    abstract fun UsuarioDao(): UsuarioDao

}