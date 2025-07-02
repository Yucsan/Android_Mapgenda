package com.yucsan.mapgendafernandochang2025.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yucsan.mapgendafernandochang2025.GeoPosHandler


class GPSViewModelFactory(
    private val geoPosHandler: GeoPosHandler,
    private val activity: ComponentActivity
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GPSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GPSViewModel(geoPosHandler, activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
