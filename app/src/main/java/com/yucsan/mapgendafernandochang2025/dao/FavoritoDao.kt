package com.yucsan.mapgendafernandochang2025.dao
import androidx.room.*
import com.yucsan.mapgendafernandochang2025.entidad.FavoritoLocal

import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface FavoritoDao {

    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId")
    fun obtenerFavoritosPorUsuario(usuarioId: UUID): Flow<List<FavoritoLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFavorito(favorito: FavoritoLocal)

    @Delete
    suspend fun eliminarFavorito(favorito: FavoritoLocal)

    @Query("DELETE FROM favoritos WHERE id = :favoritoId")
    suspend fun eliminarFavoritoPorId(favoritoId: UUID)
}
