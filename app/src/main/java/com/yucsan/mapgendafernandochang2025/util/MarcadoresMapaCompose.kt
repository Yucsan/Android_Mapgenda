package com.yucsan.mapgendafernandochang2025.util

import android.content.Context
import android.graphics.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.yucsan.mapgendafernandochang2025.R

object MarcadorIconoUtils {

    private fun getColorFromHue(hue: Float): Int {
        val hsv = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsv)
    }

    fun getHueForCategoria(categoria: String?): Float {
        return when (categoria?.lowercase()) {
            "restaurant" -> BitmapDescriptorFactory.HUE_RED
            "cafe" -> BitmapDescriptorFactory.HUE_ORANGE
            "clothing_store" -> BitmapDescriptorFactory.HUE_VIOLET
            "park" -> BitmapDescriptorFactory.HUE_GREEN
            "tourist_attraction" -> BitmapDescriptorFactory.HUE_YELLOW
            "museum" -> BitmapDescriptorFactory.HUE_AZURE
            "art_gallery" -> BitmapDescriptorFactory.HUE_ROSE
            "aquarium" -> BitmapDescriptorFactory.HUE_CYAN
            "stadium" -> BitmapDescriptorFactory.HUE_BLUE
            "night_club" -> BitmapDescriptorFactory.HUE_MAGENTA
            "movie_theater" -> BitmapDescriptorFactory.HUE_RED
            "casino" -> BitmapDescriptorFactory.HUE_ROSE
            "lodging" -> BitmapDescriptorFactory.HUE_BLUE
            "bus_station", "train_station", "subway_station" -> BitmapDescriptorFactory.HUE_ORANGE
            "custom" -> BitmapDescriptorFactory.HUE_RED
            else -> BitmapDescriptorFactory.HUE_ROSE
        }
    }

    fun generarIconoConTextoYColor(context: Context, texto: String, hue: Float): BitmapDescriptor {
        val width = 300
        val height = 100

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo blanco
        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // Solo el texto lleva color
        val textPaint = Paint().apply {
            color = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
            textSize = 32f
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 16f, 16f, backgroundPaint)

        val x = 20f
        val y = height / 2f + 12f
        canvas.drawText(texto.take(25), x, y, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


//---
    fun getDrawableForCategoria(categoria: String?): Int {
        return when (categoria?.lowercase()) {
            "restaurant" -> R.drawable.restaurant
            "cafe" -> R.drawable.cafe
            "clothing_store" -> R.drawable.shop
            "park" -> R.drawable.tree
            "tourist_attraction" -> R.drawable.turistatraction
            "museum" -> R.drawable.museum
            "art_gallery" -> R.drawable.art_galery
            "aquarium" -> R.drawable.acuario
            "stadium" -> R.drawable.estadio
            "night_club" -> R.drawable.night_club
            "movie_theater" -> R.drawable.cine
            "casino" -> R.drawable.casino
            "lodging" -> R.drawable.hotel
            "bus_station", "train_station", "subway_station" -> R.drawable.bus
            "custom" -> R.drawable.custom
            else -> R.drawable.custom // asegúrate de tener este como fallback
        }
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