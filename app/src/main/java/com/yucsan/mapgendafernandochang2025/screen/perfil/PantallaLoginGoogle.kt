package com.yucsan.mapgendafernandochang2025.screen.perfil

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.yucsan.mapgendafernandochang2025.R
import com.yucsan.mapgendafernandochang2025.viewmodel.AuthViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.GPSViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UsuarioViewModel
import com.yucsan.mapgendafernandochang2025.mapper.toEntity
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapgendafernandochang2025.servicio.log.ApiService
import com.yucsan.mapgendafernandochang2025.util.config.ApiConfig
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Composable
fun PantallaLoginGoogle(
    gpsViewModel: GPSViewModel,
    authViewModel: AuthViewModel,
    usuarioViewModel: UsuarioViewModel,
    networkMonitor: NetworkMonitor,
    context: Context
) {
    val context = LocalContext.current
    val activity = context as Activity
    var forzarReintentoLogin by remember { mutableStateOf(false) }

    val hayConexion by networkMonitor.isConnected.collectAsState()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("***reemplaza con usuario google***.apps.googleusercontent.com") // 👈 Usa tu ID real
        .requestEmail()
        .build()

    val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
    Log.d("GOOGLE_LOGIN", "Última cuenta firmada: ${lastAccount?.email}")

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    var cuentaDesactivada by remember { mutableStateOf(false) }
    var emailDesactivado by remember { mutableStateOf("") }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            Log.d("ID_TOKEN", "👉 Token de Google obtenido: $idToken")

            val service = RetrofitInstance.api
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val response = service.loginConGoogle(mapOf("idToken" to idToken!!))

                    if (response.code() == 403) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Cuenta desactivada. Puedes reactivarla.", Toast.LENGTH_LONG).show()

                            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                            val idUsuario = prefs.getString("usuario_id", null)

                            if (idUsuario != null) {
                                usuarioViewModel.reactivarCuenta(
                                    idUsuario,
                                    onSuccess = {
                                        Toast.makeText(context, "Cuenta reactivada. Reintentando login...", Toast.LENGTH_SHORT).show()
                                        forzarReintentoLogin = true
                                    },
                                    onError = {
                                        Toast.makeText(context, "Error al reactivar: $it", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                // ✅ No hay ID → activa botón manual
                                cuentaDesactivada = true
                                emailDesactivado = account.email ?: ""
                            }

                        }
                        return@launch
                    }

                    RetrofitInstance.setTokenProvider {
                        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                            .getString("jwt_token", null)
                    }

                    if (response.isSuccessful) {
                        Log.d("LOGIN_FLOW", "✅ Login exitoso")
                        val loginResponse = response.body()


                        if (loginResponse != null) {
                            Log.d("LOGIN_FLOW", "✅ loginResponse no es null")
                            val usuario = loginResponse.usuario
                            val token = loginResponse.token

                            withContext(Dispatchers.Main) {
                                Log.d("LOGIN_FLOW", "🟢 Dentro del withContext MAIN")

                                usuarioViewModel.establecerUsuarioActivo(usuario.id.toString())
                                Log.d("LOGIN_FLOW", "Usuario activo establecido: ${usuario.id}")

                                gpsViewModel.start()
                                authViewModel.iniciarSesion(context, usuario.toEntity(), token)

                                Log.d("LOGIN_FLOW", "LLEGO AQUÍ")

                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LOGIN_FLOW", "Error al contactar el backend", e)
                }
            }
        } catch (e: ApiException) {
            Log.e("LOGIN_FLOW", "Fallo el login", e)
        }
    }

    if (forzarReintentoLogin) {
        LaunchedEffect(Unit) {
            forzarReintentoLogin = false
            signInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoapp),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.migoogleboton),
            contentDescription = "Botón Google",
            modifier = Modifier
                .size(200.dp)
                .clickable(enabled = hayConexion) {
                    googleSignInClient.signOut().addOnCompleteListener {
                        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .remove("jwt_token")      // 👈 Borra token viejo
                            .remove("usuario_id")     // 👈 Borra usuario activo anterior
                            .apply()

                        signInLauncher.launch(googleSignInClient.signInIntent)
                    }

                }
        )

        if (cuentaDesactivada) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Tu cuenta está desactivada. Puedes reactivarla manualmente.",
                color = Color.Yellow,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Button(onClick = {
                usuarioViewModel.buscarYReactivarPorEmail(
                    emailDesactivado,
                    onSuccess = {
                        Toast.makeText(context, "Cuenta reactivada. Reintentando login...", Toast.LENGTH_SHORT).show()
                        cuentaDesactivada = false
                        forzarReintentoLogin = true
                    },
                    onError = {
                        Toast.makeText(context, "Error al reactivar: $it", Toast.LENGTH_LONG).show()
                    }
                )
            }) {
                Text("Reactivar cuenta manualmente")
            }
        }

        if (!hayConexion) {
            Text(
                text = "Sin conexión. Por favor, verifica tu red.",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

    }
}
