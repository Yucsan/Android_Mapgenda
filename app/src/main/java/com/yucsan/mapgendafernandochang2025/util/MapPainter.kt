package com.yucsan.mapgendafernandochang2025.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.R

object MapPainter {


    fun pintarLugar(
        context: Context,
        map: GoogleMap,
        lugar: LugarLocal,
        index: Int? = null
    ): Marker? {
        val drawableRes = when (lugar.categoriaGeneral?.lowercase()) {
            "restaurant", "food", "meal_delivery", "meal_takeaway" -> R.drawable.restaurant
            "cafe", "bakery" -> R.drawable.cafe
            "clothing_store", "store", "shopping_mall", "supermarket" -> R.drawable.shop
            "park", "natural_feature", "point_of_interest" -> R.drawable.tree
            "tourist_attraction", "landmark" -> R.drawable.turistatraction
            "museum", "art_gallery", "aquarium" -> R.drawable.museum
            "stadium", "gym", "sports_complex" -> R.drawable.estadio
            "night_club", "bar", "liquor_store" -> R.drawable.night_club
            "movie_theater", "cinema" -> R.drawable.cine
            "casino", "amusement_park" -> R.drawable.casino
            "lodging", "hotel", "motel" -> R.drawable.hotel
            "bus_station", "train_station", "subway_station", "transit_station" -> R.drawable.bus
            "custom", "point_of_interest" -> R.drawable.custom
            else -> {
                // Intentar determinar la categoría basada en los tipos
                when {
                    lugar.tipos?.any { it.contains("food", ignoreCase = true) } == true -> R.drawable.restaurant
                    lugar.tipos?.any { it.contains("cafe", ignoreCase = true) } == true -> R.drawable.cafe
                    lugar.tipos?.any { it.contains("store", ignoreCase = true) } == true -> R.drawable.shop
                    lugar.tipos?.any { it.contains("park", ignoreCase = true) } == true -> R.drawable.tree
                    lugar.tipos?.any { it.contains("museum", ignoreCase = true) } == true -> R.drawable.museum
                    lugar.tipos?.any { it.contains("stadium", ignoreCase = true) } == true -> R.drawable.estadio
                    lugar.tipos?.any { it.contains("night", ignoreCase = true) } == true -> R.drawable.night_club
                    lugar.tipos?.any { it.contains("movie", ignoreCase = true) } == true -> R.drawable.cine
                    lugar.tipos?.any { it.contains("casino", ignoreCase = true) } == true -> R.drawable.casino
                    lugar.tipos?.any { it.contains("hotel", ignoreCase = true) } == true -> R.drawable.hotel
                    lugar.tipos?.any { it.contains("station", ignoreCase = true) } == true -> R.drawable.bus
                    else -> R.drawable.custom
                }
            }
        }

        val icono: BitmapDescriptor? = try {
            if (index != null) {
                IconosMapa.generarIconoNumerado(context, index + 1)
            } else {
                generarIconoConTextoSobreImagen(context, lugar.nombre, drawableRes)
            }
        } catch (e: Exception) {
            Log.e("MAPA_ICONO", "❌ Error al generar icono para ${lugar.nombre}: ${e.message}")
            null
        }

        return try {
            val options = MarkerOptions()
                .position(LatLng(lugar.latitud, lugar.longitud))
                .title(lugar.nombre)
                .snippet(lugar.direccion)

            if (icono != null) {
                options.icon(icono)
            }

            val marker = map.addMarker(options)
            marker?.tag = lugar.id
            marker
        } catch (e: Exception) {
            Log.e("MAPA_MARKER", "❌ Error al agregar marker para ${lugar.nombre}: ${e.message}")
            null
        }
    }

    fun pintarUbicacion(
        context: Context,
        map: GoogleMap,
        ubicacion: UbicacionLocal
    ): Marker {
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(ubicacion.latitud, ubicacion.longitud))
                .title("📌 ${ubicacion.nombre}")
                .snippet("Tipo: ${ubicacion.tipo}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
        marker?.tag = ubicacion
        return marker!!
    }

    fun pintarPolylineRuta(
        map: GoogleMap,
        lugares: List<LugarLocal>
    ) {
        if (lugares.size < 2) return

        val puntos = lugares.map { LatLng(it.latitud, it.longitud) }

        map.addPolyline(
            PolylineOptions()
                .addAll(puntos)
                .width(8f)
                .color(Color.BLUE)
                .geodesic(true)
        )
    }

    fun generarIconoConTextoSobreImagen(
        context: Context,
        texto: String,
        drawableRes: Int
    ): BitmapDescriptor {
        val baseBitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
        val scaledBitmap = Bitmap.createScaledBitmap(baseBitmap, 150, 150, false)

        val mutableBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val textPaint = Paint().apply {
            color = Color.parseColor("#7100D4") // magenta oscuro
            textSize = 35f
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val backgroundPaint = Paint().apply {
            color = Color.argb(180, 255, 255, 255) // fondo blanco semitransparente
            style = Paint.Style.FILL
        }

        val maxWidth = mutableBitmap.width - 20f
        val lines = wrapText(texto, textPaint, maxWidth, maxLines = 2)

        val lineHeight = 35f
        val padding = 12f
        val totalHeight = lineHeight * lines.size + padding * 2

        val rect = RectF(
            0f,
            mutableBitmap.height - totalHeight - 5f,
            mutableBitmap.width.toFloat(),
            mutableBitmap.height.toFloat()
        )

        canvas.drawRoundRect(rect, 8f, 8f, backgroundPaint)

        lines.forEachIndexed { index, line ->
            val x = 10f
            val y = mutableBitmap.height - totalHeight + padding + (index + 1) * lineHeight
            canvas.drawText(line, x, y, textPaint)
        }

        return BitmapDescriptorFactory.fromBitmap(mutableBitmap)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float, maxLines: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
                if (lines.size == maxLines - 1) break
            }
        }

        if (lines.size < maxLines && currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }




}
