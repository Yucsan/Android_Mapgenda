package com.yucsan.mapgendafernandochang2025.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import android.graphics.*
import com.yucsan.mapgendafernandochang2025.R

object IconosMapa {
    fun seleccionado(context: Context): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.selected)
        val scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
        return BitmapDescriptorFactory.fromBitmap(scaled)
    }

    fun generarIconoNumerado(context: Context, numero: Int): BitmapDescriptor {
        val baseBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.selected)
        val scaledBitmap = Bitmap.createScaledBitmap(baseBitmap, 120, 120, false)

        val mutableBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            textSize = 80f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Dibujar el número en el centro del marcador
        val x = mutableBitmap.width / 2f
        val y = (mutableBitmap.height / 2f) + 15f
        canvas.drawText(numero.toString(), x, y, paint)

        return BitmapDescriptorFactory.fromBitmap(mutableBitmap)
    }

}


