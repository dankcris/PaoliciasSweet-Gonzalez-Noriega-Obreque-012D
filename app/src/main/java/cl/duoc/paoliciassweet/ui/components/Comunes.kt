package cl.duoc.paoliciassweet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.paoliciassweet.data.EstadoReserva

/** Mensaje de error animado para controles que no son campos de texto (fecha, horario). */
@Composable
fun ErrorTexto(error: String?, modifier: Modifier = Modifier) {
    var ultimo by remember { mutableStateOf("") }
    if (error != null) ultimo = error
    AnimatedVisibility(visible = error != null, modifier = modifier) {
        Text(
            text = ultimo,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

/** Etiqueta de color según el estado del abono. */
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        EstadoReserva.PENDIENTE -> Pair(Color(0xFFFFE0B2), Color(0xFF7A4100))
        EstadoReserva.EN_REVISION -> Pair(Color(0xFFBBDEFB), Color(0xFF0D3C73))
        EstadoReserva.ABONO_50 -> Pair(Color(0xFFC8E6C9), Color(0xFF1B5E20))
        else -> Pair(Color(0xFFE0E0E0), Color(0xFF333333))
    }
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = EstadoReserva.etiqueta(estado),
            color = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun iconoCategoria(categoria: String): ImageVector =
    if (categoria == "Tortas") Icons.Default.Cake else Icons.Default.Restaurant
