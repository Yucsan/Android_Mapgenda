package com.yucsan.mapgendafernandochang2025.screen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

import com.yucsan.mapgendafernandochang2025.screen.onLine.PantallaMapaCompose
import com.yucsan.mapgendafernandochang2025.ThemeViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.GPSViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.NavegacionViewModel

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import androidx.compose.runtime.*

import androidx.media3.datasource.DefaultDataSource
import androidx.media3.ui.AspectRatioFrameLayout


import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.yucsan.mapgendafernandochang2025.screen.perfil.PantallaLoginGoogle
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import com.yucsan.mapgendafernandochang2025.viewmodel.AuthViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UsuarioViewModel
import android.provider.Settings
import android.net.Uri
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yucsan.mapgendafernandochang2025.util.CargandoUbicacionYMapa
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import dagger.hilt.android.internal.Contexts.getApplication


@OptIn(UnstableApi::class)
@Composable
fun iniciarMapa(
    lugarViewModel: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    mapViewModel: MapViewModel,
    navController: NavController,
    gpsViewModel: GPSViewModel,
    themeViewModel: ThemeViewModel,
    usuarioViewModel: UsuarioViewModel,
    authViewModel: AuthViewModel,
    networkMonitor: NetworkMonitor,
    ubicacionViewModel: UbicacionViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val estadoUbic by ubicacionViewModel.estado.collectAsStateWithLifecycle()

   val mostrarVideo = authState is AuthState.NoAutenticado || authState is AuthState.Loading

    /* ───────── Vídeo de fondo ───────── */
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaItem =
                MediaItem.fromUri("android.resource://${context.packageName}/raw/introvideo")
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            setMediaSource(mediaSource)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            prepare(); playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }


    Box(Modifier.fillMaxSize()) {
        /* 1️⃣ Vídeo de fondo */
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
           modifier = Modifier
              .fillMaxSize()
              .graphicsLayer {
                 alpha = if (mostrarVideo) 1f else 0f
              }
        )


        /* Contenido UI*/
        when (authState) {
            is AuthState.Loading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                   CircularProgressIndicator()
                }
            }

            is AuthState.NoAutenticado -> {
                PantallaLoginGoogle(
                    gpsViewModel = gpsViewModel,
                    authViewModel = authViewModel,
                    usuarioViewModel = usuarioViewModel,
                    networkMonitor = networkMonitor,
                    context = context
                )
            }

            is AuthState.Autenticado -> {
                LaunchedEffect(Unit) {
                   exoPlayer.release()
                }

                /* 3️⃣ Flow de permisos + primera ubicación */
                when (estadoUbic) {
                    /* — Sin permiso: lo pedimos — */
                    is UbicacionViewModel.EstadoUbicacion.SinPermiso -> {
                        SolicitarPermisoUbicacion(
                            onPermisoConcedido = { ubicacionViewModel.onPermisoConcedido() },
                            onPermisoDenegado = { ubicacionViewModel.onPermisoDenegado() }
                        )
                    }

                    /* — Esperando “fix” (mostramos loader pero el mapa ya puede estar detrás) — */
                    is UbicacionViewModel.EstadoUbicacion.EsperandoFix -> {
                        CargandoUbicacionYMapa(
                            ubicacionViewModel = ubicacionViewModel,
                            contenidoMapa = {
                                PantallaMapaCompose(
                                    viewModelLugar = lugarViewModel,
                                    navegacionViewModel = navegacionViewModel,
                                    mapViewModel = mapViewModel,
                                    navController = navController,
                                    themeViewModel = themeViewModel,
                                    networkMonitor = networkMonitor,
                                    ubicacionViewModel = ubicacionViewModel,
                                    authViewModel = authViewModel
                                )
                            }
                        )
                    }

                    /* — Tenemos ubicación: mapa con MyLocation y listo — */
                    is UbicacionViewModel.EstadoUbicacion.Disponible -> {
                        PantallaMapaCompose(
                            viewModelLugar = lugarViewModel,
                            navegacionViewModel = navegacionViewModel,
                            mapViewModel = mapViewModel,
                            navController = navController,
                            themeViewModel = themeViewModel,
                            networkMonitor = networkMonitor,
                            ubicacionViewModel = ubicacionViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}










