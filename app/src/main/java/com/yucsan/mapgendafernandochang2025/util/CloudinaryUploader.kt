package com.yucsan.mapgendafernandochang2025.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.utils.ObjectUtils
import com.yucsan.mapgendafernandochang2025.util.config.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryUploader {

    private var initialized = false
    private val BASE_URL = ApiConfig.BASE_URL

    fun init(context: Context) {
        if (!initialized) {
            val config = hashMapOf(
                "cloud_name" to "dknlc31bw",
                "upload_preset" to "mapgenda1"
            )
            MediaManager.init(context, config)
            initialized = true
        }
    }

    suspend fun subirImagenDesdeUri(context: Context, uri: Uri, folder: String): String? {
        init(context)

        return suspendCancellableCoroutine { continuation ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("img_", ".jpg", context.cacheDir)
                tempFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }

                MediaManager.get().upload(tempFile.path)
                    .unsigned("mapgenda1") // 👈 reemplaza con el nombre real
                    .option("folder", folder)
                    .option("public_id", UUID.randomUUID().toString())
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}

                        override fun onProgress(
                            requestId: String?,
                            bytes: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {
                            val url = resultData?.get("secure_url") as? String
                            continuation.resume(url)
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(Exception("Error al subir imagen: ${error?.description}"))
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(Exception("Reintento al subir imagen: ${error?.description}"))
                        }
                    })
                    .dispatch()

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    fun extraerPublicIdDesdeUrl(url: String): String? {
        val regex = Regex("""/upload/[^/]+/(.+)\.(jpg|jpeg|png|webp)""")
        val match = regex.find(url)
        val publicId = match?.groups?.get(1)?.value
        Log.d("CloudinaryUploader", "🔍 Extraído public_id: $publicId desde URL: $url")
        return publicId
    }

    suspend fun eliminarImagenDesdeBackend(publicId: String, jwt: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}usuarios/eliminar-imagen")

                Log.d("CloudinaryUploader", "📡 Llamando a URL: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $jwt")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = """{ "public_id": "$publicId" }"""
                connection.outputStream.use { os ->
                    os.write(jsonBody.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                Log.d("CloudinaryUploader", "🌐 Respuesta del servidor: $responseCode")

                if (responseCode in 200..299) {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d("CloudinaryUploader", "🗑️ Imagen eliminada exitosamente. Respuesta: $response")
                    true
                } else {
                    val error = connection.errorStream?.bufferedReader()?.readText()
                    Log.w("CloudinaryUploader", "⚠️ Fallo al eliminar imagen. Código: $responseCode. Error: $error")
                    false
                }
            } catch (e: Exception) {
                Log.e("CloudinaryUploader", "❌ Excepción al eliminar imagen: ${e.message}", e)
                false
            }
        }
    }
}

