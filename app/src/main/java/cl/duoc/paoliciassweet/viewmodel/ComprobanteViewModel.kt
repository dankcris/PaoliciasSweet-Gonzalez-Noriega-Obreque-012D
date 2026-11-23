package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.network.FechaMapper
import cl.duoc.paoliciassweet.network.RetrofitClient
import cl.duoc.paoliciassweet.network.UriMultipartHelper
import cl.duoc.paoliciassweet.recursos.copiarAInterno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class ComprobanteUiState(
    val reserva: Reserva? = null,
    val fotoUri: String? = null,
    val error: String? = null,
    val enviado: Boolean = false
)

class ComprobanteViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))
    private val _state = MutableStateFlow(ComprobanteUiState())
    val state: StateFlow<ComprobanteUiState> = _state.asStateFlow()
    private var job: Job? = null

    fun cargar(reservaId: Long) {
        val usuarioId = Sesion.usuarioId.value ?: return
        job?.cancel()
        job = viewModelScope.launch {
            repo.reserva(reservaId, usuarioId).collect { reservaLocal ->
                if (reservaLocal != null) {
                    _state.update { it.copy(reserva = reservaLocal) }
                } else {
                    // Si la reserva viene directo del backend, la busca en el microservicio
                    try {
                        val dto = RetrofitClient.remoto.reservas()
                            .find { it.id == reservaId }
                        if (dto != null) {
                            _state.update {
                                it.copy(
                                    reserva = Reserva(
                                        id = dto.id,
                                        usuarioId = usuarioId,
                                        productoId = dto.productoId,
                                        productoNombre = dto.productoNombre,
                                        fechaEvento = FechaMapper.isoStringToLong(dto.fechaEvento),
                                        bloque = "Confirmado",
                                        asistentes = dto.asistentes,
                                        observaciones = dto.notas ?: "",
                                        total = dto.total,
                                        abono = dto.abono,
                                        estado = dto.estado,
                                        latitud = null,
                                        longitud = null,
                                        creadaEn = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    } catch (_: Exception) { }
                }
            }
        }
    }

    fun onFoto(uri: String?) = _state.update { it.copy(fotoUri = uri, error = null) }

    fun onGaleria(uri: Uri) {
        viewModelScope.launch {
            val interno = withContext(Dispatchers.IO) { copiarAInterno(getApplication<Application>(), uri) }
            if (interno != null) onFoto(interno)
            else _state.update { it.copy(error = "No se pudo leer la imagen seleccionada") }
        }
    }

    fun onError(mensaje: String) = _state.update { it.copy(error = mensaje) }

    fun enviar() {
        val s = _state.value
        val reserva = s.reserva ?: return
        val uriString = s.fotoUri
        if (uriString == null) {
            _state.update { it.copy(error = "Adjunta el comprobante de transferencia (cámara o galería)") }
            return
        }
        val usuarioId = Sesion.usuarioId.value ?: return

        viewModelScope.launch {
            try {
                // Soporta tanto rutas internas (/data/...) como URIs (content:// o file://)
                val archivoPart = withContext(Dispatchers.IO) {
                    if (uriString.startsWith("/")) {
                        val file = File(uriString)
                        val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("archivo", file.name, reqFile)
                    } else {
                        UriMultipartHelper.uriToMultipart(
                            getApplication<Application>(),
                            Uri.parse(uriString)
                        )
                    }
                } ?: throw IllegalStateException("No se pudo procesar el archivo de imagen")

                // 1. Subir imagen al microservicio comprobantes-service (puerto 8083)
                RetrofitClient.remoto.subirComprobante(reserva.id, reserva.abono, archivoPart)

                // 2. Actualizar estado de la reserva en reservas-service (puerto 8082)
                try {
                    RetrofitClient.remoto.cambiarEstado(reserva.id, "COMPROBANTE_ENVIADO")
                } catch (_: Exception) { }

                // 3. Guardar respaldo en Room local
                repo.adjuntarComprobante(reserva.id, usuarioId, uriString)
                _state.update { it.copy(enviado = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Error al subir el comprobante al servidor: ${e.localizedMessage}")
                }
            }
        }
    }
}
