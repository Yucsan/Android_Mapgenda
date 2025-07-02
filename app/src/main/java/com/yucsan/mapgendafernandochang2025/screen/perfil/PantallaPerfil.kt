package com.yucsan.mapgendafernandochang2025.screen.perfil

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.mapper.toDTO
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapgendafernandochang2025.util.CloudinaryUploader
import com.yucsan.mapgendafernandochang2025.viewmodel.AuthViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(viewModel: LugarViewModel,
                   usuarioViewModel: UsuarioViewModel,
                   navController:NavController,
                   authViewModel: AuthViewModel
                   ) {
    val context = LocalContext.current
    val cargando by viewModel.cargando.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var nuevaFotoUri by remember { mutableStateOf<String?>(null) }
    val usuario by usuarioViewModel.usuario.collectAsState()
    var showSolicitarBajaDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarConteoPorCategoria()
        usuarioViewModel.cargarUsuario()
        usuario?.let {
            usuarioViewModel.sincronizarUsuarioConBackend(it.id)
        }

    }

    val coroutineScope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            nuevaFotoUri = it.toString()
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf(TextFieldValue("")) }
    var nuevoPais by remember { mutableStateOf(TextFieldValue("")) }
    var nuevaCiudad by remember { mutableStateOf(TextFieldValue("")) }
    var showCerrarSesionDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Configuración", modifier = Modifier.padding(16.dp))
                Divider()

                Text(
                    text = "Solicitar baja de cuenta",
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { showSolicitarBajaDialog = true },
                    color = Color.Red
                )
                Divider()
                Text("Ayuda", modifier = Modifier.padding(16.dp))
                Divider()
                Text(
                    text = "Cerrar Sesión",
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable {
                            showCerrarSesionDialog = true
                        },
                    color = MaterialTheme.colorScheme.error
                )
            }

        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Perfil", style = MaterialTheme.typography.headlineMedium ) },
                    modifier = Modifier.height(55.dp),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {





                    if (usuario != null) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(40.dp))
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.Gray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val painter = rememberAsyncImagePainter(
                                        model = nuevaFotoUri ?: usuario?.fotoPerfilUri ?: "",
                                        contentScale = ContentScale.Crop
                                    )
                                    Image(
                                        painter = painter,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp) // separación entre botones
                                ) {
                                    OutlinedButton(
                                        onClick = { pickImageLauncher.launch(arrayOf("image/*")) },
                                        modifier = Modifier
                                            .weight(0.7f)
                                            .height(40.dp)
                                            .fillMaxWidth(),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cambiar foto")
                                    }


                                    Button(
                                        onClick = {
                                            usuario?.id?.let {
                                                usuarioViewModel.refrescarUsuarioDesdeApi(
                                                    UUID.fromString(
                                                        it
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().weight(0.2f).height(40.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Icon(imageVector = Icons.Default.Cached, contentDescription = "Actualizar")
                                    }
                                }

                            }
                        }

                        if (nuevaFotoUri != null && nuevaFotoUri != usuario?.fotoPerfilUri) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            nuevaFotoUri?.let { uriString ->
                                                val uri = Uri.parse(uriString)
                                                isUploading = true
                                                try {
                                                    val urlAnterior = usuario!!.fotoPerfilUri
                                                    val jwt = authViewModel.getTokenSeguro(context)

                                                    val secureUrl = CloudinaryUploader.subirImagenDesdeUri(context, uri, "usuarios")

                                                    if (secureUrl != null) {
                                                        val actualizado = usuario!!.copy(fotoPerfilUri = secureUrl)
                                                        usuarioViewModel.guardarUsuario(actualizado)

                                                        val dto = actualizado.toDTO()
                                                        RetrofitInstance.api.actualizarUsuario(actualizado.id.toString(), dto)

                                                        nuevaFotoUri = null

                                                        Toast.makeText(context, "✅ Imagen subida con éxito", Toast.LENGTH_SHORT).show()

                                                        // 🧹 Eliminar imagen anterior desde backend
                                                        if (!urlAnterior.isNullOrBlank() && !jwt.isNullOrBlank()) {
                                                            val publicId = CloudinaryUploader.extraerPublicIdDesdeUrl(urlAnterior)
                                                            if (!publicId.isNullOrBlank()) {
                                                                val eliminado = CloudinaryUploader.eliminarImagenDesdeBackend(publicId, jwt)
                                                                Log.d("Perfil", "🧹 Resultado de eliminación: $eliminado")
                                                            } else {
                                                                Log.w("Perfil", "⚠️ No se pudo extraer public_id desde $urlAnterior")
                                                            }
                                                        } else {
                                                            Log.w("Perfil", "⚠️ No se puede eliminar imagen: faltan datos previos o JWT")
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "❌ Error: URL de Cloudinary nula", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("Perfil", "❌ Error inesperado: ${e.message}", e)
                                                    Toast.makeText(context, "⚠️ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                } finally {
                                                    isUploading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Guardar cambios")
                                }





                            }
                        }

                        item {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Nombre: ") }
                                    append(usuario?.nombre ?: "Desconocido")
                                },fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))

                                Text(buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Email: ") }
                                    append(usuario?.email ?: "Desconocido")
                                },fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))

                                Text(buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Rol: ") }
                                    append(usuario?.rol ?: "Desconocido")
                                },fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp) )

                                Text(buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("País: ") }
                                    append(usuario?.pais ?: "No especificado")
                                },fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))

                                Text(buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Ciudad: ") }
                                    append(usuario?.ciudad ?: "No especificado")
                                },fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))

                                Text(buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Verificado: ") }
                                    append(if (usuario?.verificado == true) "Sí" else "No")
                                },fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(onClick = {
                                usuario?.let {
                                    nuevoNombre = TextFieldValue(it.nombre ?: "")
                                    nuevoPais = TextFieldValue(it.pais ?: "")
                                    nuevaCiudad = TextFieldValue(it.ciudad ?: "")
                                    showDialog = true
                                }
                            },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Editar Perfil")
                            }
                        }

                    } else {
                        item {
                            Text("No hay sesión iniciada.")
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            usuario?.let {
                                val actualizado = it.copy(
                                    nombre = nuevoNombre.text,
                                    pais = nuevoPais.text.ifBlank { null },
                                    ciudad = nuevaCiudad.text.ifBlank { null }
                                )
                                coroutineScope.launch {
                                    usuarioViewModel.guardarUsuario(actualizado)
                                }
                            }
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("Editar Perfil") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = nuevoNombre,
                                onValueChange = { nuevoNombre = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = nuevoPais,
                                onValueChange = { nuevoPais = it },
                                label = { Text("País") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = nuevaCiudad,
                                onValueChange = { nuevaCiudad = it },
                                label = { Text("Ciudad") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            }

            if (showCerrarSesionDialog) {
                AlertDialog(
                    onDismissRequest = { showCerrarSesionDialog = false },
                    title = { Text("¿Cerrar sesión?") },
                    text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showCerrarSesionDialog = false

                            authViewModel.cerrarSesion(context)

                            navController.navigate(Ruta.Pantalla1.ruta) { popUpTo(0) } //Ruta.Pantalla1.ruta
                        }) {
                            Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCerrarSesionDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showSolicitarBajaDialog) {
                AlertDialog(
                    onDismissRequest = { showSolicitarBajaDialog = false },
                    title = { Text("¿Deseas darte de baja?") },
                    text = { Text("Tu cuenta será desactivada y no podrás volver a iniciar sesión hasta que la reactives.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showSolicitarBajaDialog = false
                            usuario?.let {
                                usuarioViewModel.desactivarCuenta(
                                    it.id,
                                    onSuccess = {
                                        Toast.makeText(context, "Cuenta desactivada", Toast.LENGTH_SHORT).show()
                                        authViewModel.cerrarSesion(context)
                                        navController.navigate(Ruta.Pantalla1.ruta) { popUpTo(0) }
                                    },
                                    onError = {
                                        Toast.makeText(context, "Error al desactivar: $it", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }) {
                            Text("Confirmar baja", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSolicitarBajaDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }


        }
    }
}






