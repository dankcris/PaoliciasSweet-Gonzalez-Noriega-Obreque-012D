package cl.duoc.paoliciassweet.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.paoliciassweet.data.EstadoReserva
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.domain.Fechas
import cl.duoc.paoliciassweet.domain.moneda
import cl.duoc.paoliciassweet.ui.components.EstadoChip
import cl.duoc.paoliciassweet.viewmodel.MisReservasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(
    onVolver: () -> Unit,
    onComprobante: (Long) -> Unit,
    vm: MisReservasViewModel = viewModel()
) {
    val reservas by vm.reservas.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            AnimatedVisibility(visible = reservas.isEmpty()) {
                Text(
                    "Aún no tienes reservas. Elige un producto del catálogo y cotiza tu evento.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reservas, key = { it.id }) { reserva ->
                    ReservaCard(
                        reserva = reserva,
                        onComprobante = { onComprobante(reserva.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ReservaCard(reserva: Reserva, onComprobante: () -> Unit, modifier: Modifier = Modifier) {
    var expandida by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandida = !expandida }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(reserva.productoNombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${Fechas.formatear(reserva.fechaEvento)} · ${reserva.bloque}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${reserva.asistentes} asistentes · ${moneda(reserva.total)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                EstadoChip(reserva.estado)
            }
            if (expandida) {
                Text(
                    "Abono requerido (50%): ${moneda(reserva.abono)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    if (reserva.observaciones.isBlank()) "Sin observaciones"
                    else "Observaciones: ${reserva.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (reserva.estado == EstadoReserva.PENDIENTE || reserva.estado == EstadoReserva.EN_REVISION) {
                Button(
                    onClick = onComprobante,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null)
                    Text(
                        if (reserva.estado == EstadoReserva.PENDIENTE) "  Adjuntar comprobante del abono"
                        else "  Cambiar comprobante"
                    )
                }
            }
        }
    }
}
