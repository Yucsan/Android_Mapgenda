package com.yucsan.mapgendafernandochang2025

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GeoPosHandler(private val context: ComponentActivity,
                    private val permiso:String=Manifest.permission.ACCESS_FINE_LOCATION) {

   private lateinit var permisosLauncher: ActivityResultLauncher<String>

   private var locationCallback: LocationCallback? = null // Guardar el callback

   init{
      inicializarLanzadorPermisos()
      solicitarPermiso(permiso){}
   }

   fun obtenerCliente()=LocationServices.getFusedLocationProviderClient(context)

   // Lanzador de permisos para manejar el resultado

   fun inicializarLanzadorPermisos(){
      permisosLauncher= context.registerForActivityResult(
         ActivityResultContracts.RequestPermission()
      ) { isGranted ->
         if (!isGranted) {
            println("Permiso denegado")
         }
      }
   }

   fun solicitarPermiso(permiso:String, accion:()->Unit){
      if(!permisosOtorgados(permiso))
         permisosLauncher.launch(permiso)
      else accion()
   }

   // Verificar si los permisos ya están otorgados
   public fun permisosOtorgados(permiso:String): Boolean {
      return ActivityCompat.checkSelfPermission(
         context, permiso
      ) == PackageManager.PERMISSION_GRANTED
   }

   //Inicia la actualización constante de la ubicación
   //Elimino la advertencia de AndroidStudio sobre la necesidad de gestionar los permisos
   //porque ya lo estoy haciendo adecuadamente
   @SuppressLint("MissingPermission")
   fun iniciarActualizacionPeriodica(
      prioridad: Int = Priority.PRIORITY_HIGH_ACCURACY,
      intervalo: Long = 500L,
      update: Long = 500L,
      accion: (locationResult: LocationResult) -> Unit
   ) {
      val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
         prioridad, intervalo
      ).setMinUpdateIntervalMillis(update).build()

      locationCallback = object : com.google.android.gms.location.LocationCallback() {
         override fun onLocationResult(locationResult: LocationResult) {
            accion(locationResult)
         }
      }

      solicitarPermiso(permiso) {
         obtenerCliente().requestLocationUpdates(
            locationRequest,
            locationCallback!!,  // Guardamos el callback para poder detenerlo luego
            null
         )
      }
   }

   // Nuevo método para detener las actualizaciones
   fun detenerActualizacionPeriodica() {
      locationCallback?.let {
         obtenerCliente().removeLocationUpdates(it)
         locationCallback = null
         Log.i("TEST FERNANDO","Actualización de ubicación detenida")
      }
   }
}



