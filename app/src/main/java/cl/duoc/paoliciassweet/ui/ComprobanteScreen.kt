package cl.duoc.paoliciassweet.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.paoliciassweet.domain.Fechas
import cl.duoc.paoliciassweet.domain.moneda
import cl.duoc.paoliciassweet.recursos.crearUriFoto
import cl.duoc.paoliciassweet.ui.components.ErrorTexto
import cl.duoc.paoliciassweet.viewmodel.ComprobanteViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprobanteScreen(
    reservaId: Long,
    onVolver: () -> Unit,
    vm: ComprobanteViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(reservaId) { vm.cargar(reservaId) }
    LaunchedEffect(s.enviado) {
        if (s.enviado) onVolver()
    }

    // ----- Recurso nativo 1: cámara -----
    var uriTemporal by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) uriTemporal?.let { vm.onFoto(it.toString()) }
    }
    fun abrirCamara() {
        val uri = crearUriFoto(context)
        uriTemporal = uri
        camara.launch(uri)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) abrirCamara() else vm.onError("Permiso de cámara denegado")
    }

    // ----- Recurso nativo 2: galería de imágenes -----
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onGaleria(uri)
    }

    val vista = s.fotoUri ?: s.reserva?.comprobanteUri

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comprobante de abono") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            s.reserva?.let { reserva ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(reserva.productoNombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${Fechas.formatear(reserva.fechaEvento)} · ${reserva.bloque}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Total: ${moneda(reserva.total)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Abono a transferir (50%): ${moneda(reserva.abono)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                "Adjunta una foto o captura de la transferencia. La administradora la revisará " +
                    "y, al validarla, tu fecha quedará bloqueada.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val concedido = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (concedido) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Cámara")
                }
                OutlinedButton(
                    onClick = {
                        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Galería")
                }
            }
            ErrorTexto(s.error)

            AnimatedVisibility(visible = vista != null) {
                AsyncImage(
                    model = vista,
                    contentDescription = "Comprobante de transferencia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Button(onClick = vm::enviar, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar comprobante")
            }
        }
    }
}
