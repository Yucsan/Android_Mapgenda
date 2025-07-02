package com.yucsan.mapgendafernandochang2025.viewmodel

import android.content.Context
import android.location.Location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.yucsan.mapgendafernandochang2025.GeoPosHandler


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//import com.yucsan.brujula.Firestore.FirestoreRepository

// ViewModel para manejar el GPS
class GPSViewModel(
    private val gph: GeoPosHandler,
    private val context: Context
) : ViewModel(),
    ViewModelProvider.Factory {


    //private val repository = FirestoreRepository()



    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create(modelClass, CreationExtras.Empty)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GPSViewModel::class.java)) {
            return GPSViewModel(gph, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


/*
    init {
        viewModelScope.launch {
            repository.getLugaresFlow().collect { nuevosLugares ->
                _ubicaciones.value = nuevosLugares
            }
        }

    }

    suspend fun agregarUbicacion(lugar: Lugar) {
        _ubicaciones.value = _ubicaciones.value + lugar
        repository.upsertLugar(lugar)
    }
    fun actualizarUbicacion(lugar: Lugar) {
        viewModelScope.launch {
            repository.upsertLugar(lugar)
        }
    }
    fun eliminarUbicacion(lugar: Lugar) {
        viewModelScope.launch {
            repository.deleteLugar(lugar.id)
        }
    }

*/
    private val _ubicacionActual = MutableStateFlow<Location?>(null)
    val ubicacionActual: StateFlow<Location?> = _ubicacionActual

    private val _ubicacionOrigen = MutableStateFlow<Location?>(null)
    val ubicacionOrigen: StateFlow<Location?> = _ubicacionOrigen

    private val _distancia = MutableStateFlow(0f)
    val distancia: StateFlow<Float> = _distancia

    private val _rumbo = MutableStateFlow(0f)
    val rumbo: StateFlow<Float> = _rumbo




    fun start() {
        gph.iniciarActualizacionPeriodica(){
            _ubicacionActual.value=it.lastLocation
            if(_ubicacionActual.value != null && ubicacionOrigen.value!=null){
                _distancia.value =ubicacionOrigen.value!!.distanceTo(ubicacionActual.value!!)
                var angulo=ubicacionOrigen.value!!.bearingTo(ubicacionActual.value!!)
                if(angulo<0) angulo+=360f
                _rumbo.value=angulo
            }
        }
    }

    fun stop() {
        gph.detenerActualizacionPeriodica()
    }

    fun referenciaNueva() {
        _ubicacionOrigen.value = ubicacionActual.value
    }
}


