package com.yucsan.mapgendafernandochang2025.repository

import android.util.Log


import com.yucsan.mapgendafernandochang2025.dao.LugarDao
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.servicio.backend.BackendLugarService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.math.pow

class LugarRepository(private val lugarDao: LugarDao) {

    fun obtenerTodos(): Flow<List<LugarLocal>> {
        return lugarDao.obtenerTodos().onEach { lista ->
            Log.d("LugarRepository", "obtenerTodos: Se han recibido ${lista.size} registros")
        }
    }

    fun obtenerLugaresPorUsuario(usuarioId: String): Flow<List<LugarLocal>> {
        return lugarDao.obtenerPorUsuario(usuarioId)
            .onEach { lista ->
                Log.d("LugarRepository", "🔐 Lugares del usuario $usuarioId: ${lista.size}")
            }
    }

    fun obtenerPorSubcategoriasDelUsuarioFiltrando(
        subcategorias: List<String>,
        usuarioId: String,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Flow<List<LugarLocal>> {
        return lugarDao.obtenerPorSubcategoriasDelUsuario(subcategorias, usuarioId)
            .map { lugares ->
                Log.d("RepoSubUsuario", "🧠 Recibidos ${lugares.size} lugares del usuario $usuarioId")

                lugares.filter {
                    val distancia = calcularDistanciaEnMetros(latitud, longitud, it.latitud, it.longitud)
                    distancia <= radio
                }.also {
                    Log.d("RepoSubUsuario", "✅ Luego de distancia: ${it.size} lugares válidos")
                }
            }
    }

    suspend fun insertarLugar(lugar: LugarLocal) {
        lugarDao.insertarLugar(lugar)
    }

    suspend fun contarLugaresPorCategoria(): Map<String, Int> {
        val lugares = lugarDao.obtenerTodosSuspend()
        return lugares
            .filter { it.categoriaGeneral != null } // Filtramos nulos antes de agrupar
            .groupBy { it.categoriaGeneral!! }
            .mapValues { it.value.size }
    }


    fun obtenerLugaresPorCategorias(categorias: List<String>): Flow<List<LugarLocal>> {
        return lugarDao.obtenerPorCategorias(categorias)
    }

    //-------------------------------------------------REVIZAR---
    fun obtenerPorSubcategoriasCercanos(
        subcategorias: List<String>,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Flow<List<LugarLocal>> {
        val radioGrados = radio / 111320.0
        return lugarDao.obtenerPorSubcategoriasCercanos(subcategorias, latitud, longitud, radioGrados)
    }

// -----   NUEVA
    fun obtenerPorSubcategoriasCercanosHaversine(
        subcategorias: List<String>,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Flow<List<LugarLocal>> {
        return lugarDao.obtenerPorSubcategoriasSinFiltroGeografico(subcategorias)
            .map { lugares ->

                Log.d("RepositoryFiltro", "🔎 Subcategorías recibidas: $subcategorias")
                Log.d("RepositoryFiltro", "📍 Centro: ($latitud, $longitud) | Radio: $radio m")
                Log.d("RepositoryFiltro", "📦 Lugares antes de filtrar por distancia: ${lugares.size}")

                lugares.filter {
                    val distancia = calcularDistanciaEnMetros(
                        latitud,
                        longitud,
                        it.latitud,
                        it.longitud

                    )
                    //Log.d("RepositoryFiltro", "📏 Distancia hasta '${it.nombre}': ${distancia} m")
                    distancia  <= radio

                }.also {
                    Log.d("RepositoryFiltro", "✅ Lugares después del filtro por distancia: ${it.size}")
                }
            }

    }

    private fun calcularDistanciaEnMetros(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371000.0 // Radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(lat1)) *
                kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).pow(2.0)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }
    //-------------------------------------------------------------------------------------------- recuerda que obtenerPorSubcategoriasCercanosHaversine utiliza la fun calcularDistanciaEnMetros

    fun obtenerLugaresCercanos(
        categorias: List<String>,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Flow<List<LugarLocal>> {
        val radioGrados = radio / 111320.0
        Log.d(
            "LugarRepository",
            "obtenerLugaresCercanos: Parámetros -> Categorías: $categorias, latitud: $latitud, longitud: $longitud, radioGrados: $radioGrados"
        )
        return lugarDao.obtenerCercanos(categorias, latitud, longitud, radioGrados).onEach { lista ->
            Log.d("LugarRepository", "obtenerLugaresCercanos: Se han obtenido ${lista.size} lugares")
        }
    }

    suspend fun actualizarLugar(lugar: LugarLocal) {
        lugarDao.actualizarLugar(lugar)
    }


    suspend fun insertarLugares(lugares: List<LugarLocal>) {
        Log.d("LugarRepository", "insertarLugares: Insertando ${lugares.size} lugares")
        lugarDao.insertarLugares(lugares)
        Log.d("LugarRepository", "insertarLugares: Inserción completada")
    }

    suspend fun limpiarTodo() {
        Log.d("LugarRepository", "limpiarTodo: Eliminando todos los registros")
        lugarDao.limpiarTodo()
        Log.d("LugarRepository", "limpiarTodo: Eliminación completada")
    }

    suspend fun hayLugaresParaCategoriaYUbicacion(
        categoria: String,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Boolean {
        // Convertir el radio de metros a grados (elevado al cuadrado) para la comparación
        val radioGrados = radio / 111320.0
        Log.d(
            "LugarRepository",
            "hayLugaresParaCategoriaYUbicacion: Verificando para la categoría '$categoria' en lat: $latitud, lng: $longitud, con radioGrados: $radioGrados"
        )
        val lugares = lugarDao.obtenerCercanosSuspend(categoria, latitud, longitud, radioGrados)
        Log.d("LugarRepository", "hayLugaresParaCategoriaYUbicacion: Encontrados ${lugares.size} lugares")
        return lugares.isNotEmpty()
    }


    suspend fun contarLugaresPorSubcategoria(): Map<String, Int> {
        return lugarDao.contarLugaresPorSubcategoria()
            .associate { it.subcategoria to it.cantidad }
    }

    suspend fun existeLugarConId(id: String): Boolean {
        return lugarDao.existeLugar(id)
    }

    suspend fun eliminarLugarPorId(id: String) {
        lugarDao.eliminarPorId(id)
    }


    //     ******************    CONEXION BACKEND   ******************

    suspend fun sincronizarConBackend(lugares: List<LugarLocal>, usuarioId: String) {

        Log.d("SYNC_REPO", "📤 Enviando ${lugares.size} lugares al backend")
        lugares.forEach {
            Log.d("SYNC_REPO", "📍 Lugar: ${it.nombre} (${it.id})")
        }

        val lugaresValidados = lugares.map {
            it.copy(
                categoriaGeneral = it.categoriaGeneral?.takeIf { cat -> cat.isNotBlank() } ?: "otro",
                direccion = it.direccion.ifBlank { "Sin dirección" },
                // otros campos si querés
            )
        }

        val backend = BackendLugarService()
        backend.subirLugaresEnLote(lugaresValidados, usuarioId)
    }

    suspend fun contarLugaresPorSubcategoriaFiltrando(
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Map<String, Int> {
        return lugarDao
            .contarSubcategoriasCercanas(latitud, longitud, radio)
            .associate { it.subcategoria to it.cantidad }
    }

    fun obtenerPorSubcategoriasDelUsuarioSinFiltroGeografico(
        subcategorias: List<String>,
        usuarioId: String,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Flow<List<LugarLocal>> {
        return lugarDao.obtenerPorSubcategoriasDelUsuarioSinFiltroGeografico(subcategorias, usuarioId)
            .map { lugares ->
                Log.d("RepoFiltroUsuario", "🎯 Lugares antes de distancia: ${lugares.size}")
                lugares.filter {
                    val distancia = calcularDistanciaEnMetros(latitud, longitud, it.latitud, it.longitud)
                    distancia <= radio
                }.also {
                    Log.d("RepoFiltroUsuario", "✅ Lugares luego de distancia: ${it.size}")
                }
            }
    }

    suspend fun contarLugaresPorSubcategoriaFiltrandoDelUsuario(
        usuarioId: String,
        latitud: Double,
        longitud: Double,
        radio: Float
    ): Map<String, Int> {
        return lugarDao
            .contarSubcategoriasCercanasDelUsuario(usuarioId, latitud, longitud, radio)
            .associate { it.subcategoria to it.cantidad }
    }




}
