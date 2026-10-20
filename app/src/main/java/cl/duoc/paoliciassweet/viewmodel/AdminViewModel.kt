package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.EstadoReserva
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.ReservaAgenda
import cl.duoc.paoliciassweet.data.Roles
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.domain.Cotizador
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResumenOperativo(
    val eventosConfirmados: Int = 0,
    val asistentes: Int = 0,
    val garzones: Int = 0,
    val ayudantes: Int = 0,
    val abonosValidados: Int = 0,
    val porRevisar: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))

    private val _filtro = MutableStateFlow<String?>(null)
    val filtro: StateFlow<String?> = _filtro.asStateFlow()

    private val agenda: StateFlow<List<ReservaAgenda>> = Sesion.rol
        .flatMapLatest { rol ->
            if (rol == Roles.ADMIN) repo.agenda() else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reservas: StateFlow<List<ReservaAgenda>> = combine(agenda, _filtro) { lista, filtro ->
        if (filtro == null) lista else lista.filter { it.estado == filtro }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estimación consolidada de personal para los eventos con abono validado
    val resumen: StateFlow<ResumenOperativo> = agenda.map { lista ->
        val confirmados = lista.filter { it.estado == EstadoReserva.ABONO_50 }
        ResumenOperativo(
            eventosConfirmados = confirmados.size,
            asistentes = confirmados.sumOf { it.asistentes },
            garzones = confirmados.sumOf { Cotizador.garzones(it.asistentes) },
            ayudantes = confirmados.sumOf { Cotizador.ayudantesCocina(it.asistentes) },
            abonosValidados = lista
                .filter { it.estado == EstadoReserva.ABONO_50 || it.estado == EstadoReserva.COMPLETADO }
                .sumOf { it.abono },
            porRevisar = lista.count { it.estado == EstadoReserva.EN_REVISION }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenOperativo())

    fun filtrar(estado: String?) {
        _filtro.value = estado
    }

    fun validarAbono(id: Long) = cambiar(id, EstadoReserva.ABONO_50)

    fun completar(id: Long) = cambiar(id, EstadoReserva.COMPLETADO)

    private fun cambiar(id: Long, estado: String) {
        if (!Sesion.esAdmin) return
        viewModelScope.launch { repo.cambiarEstado(id, estado) }
    }

    fun rechazarComprobante(id: Long) {
        if (!Sesion.esAdmin) return
        viewModelScope.launch { repo.rechazarComprobante(id) }
    }
}
