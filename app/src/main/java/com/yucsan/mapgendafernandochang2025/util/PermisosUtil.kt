package com.yucsan.mapgendafernandochang2025.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

suspend fun solicitarPermisoUbicacion(context: Context): Boolean {
    val activity = context as? Activity
    if (activity == null) {
        Toast.makeText(context, "Error: no es un Activity", Toast.LENGTH_SHORT).show()
        return false
    }

    val yaTienePermiso = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (yaTienePermiso) return true

    // Solicitar permisos
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        1001
    )

    // Reintentar hasta 5 veces (máx ~5s esperando al usuario)
    repeat(5) {
        delay(1000)
        val concedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (concedido) return true
    }

    return false
}
