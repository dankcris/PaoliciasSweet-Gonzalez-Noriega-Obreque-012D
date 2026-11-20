package cl.duoc.paoliciassweet.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object FechaMapper {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Convierte el Long (milisegundos) de tu app a "2026-11-26" para el backend
    fun longToIsoString(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(formatter)
    }

    // Convierte "2026-11-26" del backend a Long (milisegundos) para tu app
    fun isoStringToLong(fecha: String): Long {
        return try {
            LocalDate.parse(fecha.take(10), formatter)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

object UriMultipartHelper {
    fun uriToMultipart(context: Context, uri: Uri, partName: String = "archivo"): MultipartBody.Part? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val fileName = "comprobante_${System.currentTimeMillis()}.jpg"

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    fun textToRequestBody(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())
}