package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.recursos.copiarAInterno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            repo.reserva(reservaId, usuarioId).collect { reserva ->
                _state.update { it.copy(reserva = reserva) }
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
        val uri = s.fotoUri
        if (uri == null) {
            _state.update { it.copy(error = "Adjunta el comprobante de transferencia (cámara o galería)") }
            return
        }
        val usuarioId = Sesion.usuarioId.value ?: return
        viewModelScope.launch {
            repo.adjuntarComprobante(reserva.id, usuarioId, uri)
            _state.update { it.copy(enviado = true) }
        }
    }
}
