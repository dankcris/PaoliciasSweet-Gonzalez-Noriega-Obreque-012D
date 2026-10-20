package cl.duoc.paoliciassweet.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.paoliciassweet.data.EstadoReserva
import cl.duoc.paoliciassweet.data.ReservaAgenda
import cl.duoc.paoliciassweet.domain.Cotizador
import cl.duoc.paoliciassweet.domain.Fechas
import cl.duoc.paoliciassweet.domain.moneda
import cl.duoc.paoliciassweet.ui.components.EstadoChip
import cl.duoc.paoliciassweet.viewmodel.AdminViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onSalir: () -> Unit,
    vm: AdminViewModel = viewModel()
) {
    val reservas by vm.reservas.collectAsStateWithLifecycle()
    val resumen by vm.resumen.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()
    val abonos by animateIntAsState(targetValue = resumen.abonosValidados, label = "abonos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda operativa") },
                navigationIcon = {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onSalir) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Eventos confirmados (abono validado)", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${resumen.eventosConfirmados} eventos · ${resumen.asistentes} asistentes",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Personal estimado: ${resumen.garzones} garzones y ${resumen.ayudantes} ayudantes de cocina",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Abonos validados: ${moneda(abonos)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Comprobantes por revisar: ${resumen.porRevisar}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filtro == null,
                        onClick = { vm.filtrar(null) },
                        label = { Text("Todos") }
                    )
                }
                items(EstadoReserva.todos) { estado ->
                    FilterChip(
                        selected = filtro == estado,
                        onClick = { vm.filtrar(estado) },
                        label = { Text(EstadoReserva.etiqueta(estado)) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = reservas.isEmpty()) {
                Text(
                    "No hay reservas para mostrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reservas, key = { it.id }) { reserva ->
                    AgendaCard(
                        reserva = reserva,
                        onValidar = { vm.validarAbono(reserva.id) },
                        onRechazar = { vm.rechazarComprobante(reserva.id) },
                        onCompletar = { vm.completar(reserva.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun AgendaCard(
    reserva: ReservaAgenda,
    onValidar: () -> Unit,
    onRechazar: () -> Unit,
    onCompletar: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    Text(
                        "${Fechas.formatear(reserva.fechaEvento)} · ${reserva.bloque}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${reserva.productoNombre} · ${reserva.asistentes} asistentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(reserva.clienteNombre, style = MaterialTheme.typography.bodySmall)
                }
                EstadoChip(reserva.estado)
            }

            if (expandida) {
                Text(
                    "Contacto: ${reserva.clienteTelefono} · ${reserva.clienteEmail}" +
                        if (reserva.clienteEmpresa.isNotBlank()) " · ${reserva.clienteEmpresa}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "Total ${moneda(reserva.total)} · Abono 50%: ${moneda(reserva.abono)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "Personal: ${Cotizador.garzones(reserva.asistentes)} garzones, " +
                        "${Cotizador.ayudantesCocina(reserva.asistentes)} ayudantes",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    if (reserva.observaciones.isBlank()) "Sin observaciones"
                    else "Observaciones: ${reserva.observaciones}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (reserva.latitud != null && reserva.longitud != null) {
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Text(
                            "%.5f, %.5f".format(reserva.latitud, reserva.longitud),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (reserva.comprobanteUri != null) {
                    AsyncImage(
                        model = reserva.comprobanteUri,
                        contentDescription = "Comprobante de transferencia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }

            when (reserva.estado) {
                EstadoReserva.EN_REVISION -> Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) { Text("Rechazar") }
                    Button(onClick = onValidar, modifier = Modifier.weight(1f)) { Text("Validar abono") }
                }
                EstadoReserva.ABONO_50 -> Button(
                    onClick = onCompletar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) { Text("Marcar evento completado") }
                else -> {}
            }
        }
    }
}
