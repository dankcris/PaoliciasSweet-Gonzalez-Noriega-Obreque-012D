package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.network.FechaMapper
import cl.duoc.paoliciassweet.network.ReservaDto
import cl.duoc.paoliciassweet.network.RetrofitClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
                val emailUsuario = "usuario$usuarioId@paoliciassweet.cl"
                val listaDto = RetrofitClient.reservasApi.listarReservas(emailUsuario)
                _remotas.value = listaDto.map { it.toEntity(usuarioId) }
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

    private fun ReservaDto.toEntity(usuarioId: Long): Reserva {
        val partesNotas = (notas ?: "").split("|").map { it.trim() }
        val bloqueExtraido = partesNotas.firstOrNull { it.startsWith("Bloque:") }
            ?.removePrefix("Bloque:")
            ?.trim()
            ?: "Por definir"
        val obsExtraidas = partesNotas.filterNot { it.startsWith("Bloque:") }.joinToString(" | ")

        return Reserva(
            id = id,
            usuarioId = usuarioId,
            productoId = productoId,
            productoNombre = productoNombre,
            fechaEvento = FechaMapper.isoStringToLong(fechaEvento),
            bloque = bloqueExtraido,
            asistentes = asistentes,
            observaciones = obsExtraidas,
            total = total,
            abono = abono,
            estado = estado,
            latitud = null,
            longitud = null,
            creadaEn = System.currentTimeMillis()
        )
    }
}
