package cl.duoc.paoliciassweet.recursos

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private fun nuevoArchivo(context: Context): File {
    val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
    return File(carpeta, "comprobante_${System.currentTimeMillis()}.jpg")
}

private fun uriDe(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

/** Uri donde la cámara guardará la foto del comprobante. */
fun crearUriFoto(context: Context): Uri = uriDe(context, nuevoArchivo(context))

/** Copia una imagen elegida desde la galería al almacenamiento interno de la app. */
fun copiarAInterno(context: Context, origen: Uri): String? {
    return try {
        val archivo = nuevoArchivo(context)
        val entrada = context.contentResolver.openInputStream(origen) ?: return null
        entrada.use { input ->
            archivo.outputStream().use { salida -> input.copyTo(salida) }
        }
        uriDe(context, archivo).toString()
    } catch (e: Exception) {
        null
    }
}
