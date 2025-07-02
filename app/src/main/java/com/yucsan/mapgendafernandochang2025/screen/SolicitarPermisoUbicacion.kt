package com.yucsan.mapgendafernandochang2025.screen



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.yucsan.mapgendafernandochang2025.util.PermissionState

@Composable
fun SolicitarPermisoUbicacion(
    onPermisoConcedido: @Composable () -> Unit,
    onPermisoDenegado: @Composable (reintentar: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var estadoPermiso by rememberSaveable { mutableStateOf(PermissionState.PENDING) }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        estadoPermiso = if (concedido) PermissionState.GRANTED else PermissionState.DENIED
    }

    LaunchedEffect(Unit) {
        val chequeo = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        estadoPermiso = if (chequeo == PackageManager.PERMISSION_GRANTED) {
            PermissionState.GRANTED
        } else {
            permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            PermissionState.PENDING
        }
    }

    when (estadoPermiso) {
        PermissionState.PENDING -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        PermissionState.GRANTED -> {
            onPermisoConcedido()
        }
        PermissionState.DENIED -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = {
                    estadoPermiso = PermissionState.PENDING
                    permisoLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Reintentar permiso")
                }
            }
        }
    }
}

