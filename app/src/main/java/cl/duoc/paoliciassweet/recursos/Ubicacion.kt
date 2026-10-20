package cl.duoc.paoliciassweet.recursos

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
fun obtenerUbicacion(
    context: Context,
    onExito: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    try {
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) onExito(loc.latitude, loc.longitude)
                else onError("No se pudo obtener la ubicación. Activa el GPS.")
            }
            .addOnFailureListener { onError("Error al obtener la ubicación") }
    } catch (e: SecurityException) {
        onError("Permiso de ubicación denegado")
    }
}
