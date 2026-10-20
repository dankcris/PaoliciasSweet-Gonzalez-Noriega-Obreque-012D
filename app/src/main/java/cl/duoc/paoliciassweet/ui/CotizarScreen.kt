package cl.duoc.paoliciassweet.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.paoliciassweet.domain.BLOQUES
import cl.duoc.paoliciassweet.domain.Fechas
import cl.duoc.paoliciassweet.domain.moneda
import cl.duoc.paoliciassweet.recursos.obtenerUbicacion
import cl.duoc.paoliciassweet.ui.components.CampoTexto
import cl.duoc.paoliciassweet.ui.components.ErrorTexto
import cl.duoc.paoliciassweet.viewmodel.CotizarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotizarScreen(
    productoId: Long,
    onVolver: () -> Unit,
    onReservado: () -> Unit,
    vm: CotizarViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mostrarFecha by remember { mutableStateOf(false) }

    LaunchedEffect(productoId) { vm.cargar(productoId) }
    LaunchedEffect(s.guardado) {
        if (s.guardado) onReservado()
    }

    fun avisar(texto: String) {
        scope.launch { snackbar.showSnackbar(texto) }
    }

    // ----- Recurso nativo: ubicación (lugar del evento) -----
    fun pedirUbicacion() {
        obtenerUbicacion(
            context,
            onExito = { lat, lng -> vm.onUbicacion(lat, lng) },
            onError = { avisar(it) }
        )
    }
    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) pedirUbicacion() else avisar("Permiso de ubicación denegado")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotizar evento") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            s.producto?.let { producto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(producto.nombre, style = MaterialTheme.typography.titleLarge)
                        Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${moneda(producto.precioPorPersona)} por persona (valor base)",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Fecha
            OutlinedButton(onClick = { mostrarFecha = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    "  " + (s.fechaMillis?.let { Fechas.formatear(it) } ?: "Seleccionar fecha del evento")
                )
            }
            ErrorTexto(s.fechaError)

            // Bloque horario
            Text("Horario", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BLOQUES.forEach { bloque ->
                    FilterChip(
                        selected = s.bloque == bloque,
                        onClick = { vm.onBloque(bloque) },
                        label = { Text(bloque) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            ErrorTexto(s.bloqueError)

            CampoTexto(
                valor = s.asistentes,
                onCambio = vm::onAsistentes,
                etiqueta = "Asistentes (10 a 100)",
                icono = Icons.Default.Groups,
                error = s.asistentesError,
                teclado = KeyboardType.Number
            )
            CampoTexto(
                valor = s.observaciones,
                onCambio = vm::onObservaciones,
                etiqueta = "Alergias o requerimientos especiales",
                icono = Icons.Default.Edit,
                error = s.observacionesError,
                lineas = 3,
                opcional = true
            )

            // Recurso nativo: ubicación
            OutlinedButton(
                onClick = {
                    val concedido = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (concedido) pedirUbicacion() else permisoUbicacion.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Usar mi ubicación como lugar del evento")
            }
            AnimatedVisibility(visible = s.latitud != null && s.longitud != null) {
                Text(
                    "Lugar del evento: %.5f, %.5f".format(s.latitud ?: 0.0, s.longitud ?: 0.0),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Cotización en tiempo real
            AnimatedVisibility(visible = s.cotizacion != null) {
                val cotizacion = s.cotizacion
                val total by animateIntAsState(targetValue = cotizacion?.total ?: 0, label = "total")
                val abono by animateIntAsState(targetValue = cotizacion?.abono ?: 0, label = "abono")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tu cotización", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tramo cobrado: ${cotizacion?.tramo ?: 0} personas · " +
                                "Descuento por volumen: ${cotizacion?.descuentoPct ?: 0}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Total: ${moneda(total)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            Text(
                                "Abono 50% para bloquear la fecha: ${moneda(abono)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            ErrorTexto(s.mensaje)

            Button(onClick = vm::reservar, modifier = Modifier.fillMaxWidth()) {
                Text("Reservar")
            }
            Text(
                "Tu fecha queda bloqueada cuando la administradora valide el comprobante del abono.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (mostrarFecha) {
        val hoy = remember { Fechas.hoyUtc() }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = s.fechaMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= hoy
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.onFecha(pickerState.selectedDateMillis)
                    mostrarFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
