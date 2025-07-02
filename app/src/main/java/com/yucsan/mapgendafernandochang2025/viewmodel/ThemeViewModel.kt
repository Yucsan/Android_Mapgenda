package com.yucsan.mapgendafernandochang2025



import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel : ViewModel() {
   private val _isDarkMode = MutableStateFlow(false)
   val isDarkMode: StateFlow<Boolean> = _isDarkMode

   private val _useGreenTheme = MutableStateFlow(true)  // Inicia con tema Verde
   val useGreenTheme: StateFlow<Boolean> = _useGreenTheme

   fun toggleTheme() {
      _isDarkMode.value = !_isDarkMode.value
   }

   fun toggleColorScheme() {
      _useGreenTheme.value = !_useGreenTheme.value
   }
}


