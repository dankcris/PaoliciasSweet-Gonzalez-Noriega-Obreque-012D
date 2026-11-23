package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.network.RetrofitClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalCoroutinesApi::class)
class MisReservasViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))
    private val _remotas = MutableStateFlow<List<Reserva>>(emptyList())

    init {
        viewModelScope.launch {
            Sesion.usuarioId.collect { id ->
                if (id != null) {
                    cargarDesdeServidor(id)
                } else {
                    _remotas.value = emptyList()
                }
            }
        }
    }

    fun cargarDesdeServidor(usuarioId: Long) {
        viewModelScope.launch {
            try {
                _remotas.value = RetrofitClient.remoto.reservasDeUsuario(usuarioId)
            } catch (_: Exception) {
                // Si no hay conexión con Docker, mantiene el respaldo de Room
            }
        }
    }

    val reservas: StateFlow<List<Reserva>> = Sesion.usuarioId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                combine(_remotas, repo.reservas(id)) { remotas, locales ->
                    if (remotas.isNotEmpty()) {
                        remotas.map { remota ->
                            val local = locales.find { it.id == remota.id }
                            local?.copy(estado = remota.estado) ?: remota
                        }
                    } else {
                        locales
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /** DELETE en el microservicio de reservas y luego en la copia local (Room). */
    fun eliminar(reservaId: Long) {
        val usuarioId = Sesion.usuarioId.value ?: return
        viewModelScope.launch {
            try {
                try {
                    RetrofitClient.remoto.eliminarReserva(reservaId)
                } catch (e: HttpException) {
                    if (e.code() != 404) throw e
                }
                repo.eliminarReserva(reservaId, usuarioId)
                _remotas.value = _remotas.value.filterNot { it.id == reservaId }
                _mensaje.value = "Reserva cancelada"
            } catch (e: Exception) {
                _mensaje.value = "No se pudo cancelar la reserva: ${e.localizedMessage}"
            }
        }
    }
}
