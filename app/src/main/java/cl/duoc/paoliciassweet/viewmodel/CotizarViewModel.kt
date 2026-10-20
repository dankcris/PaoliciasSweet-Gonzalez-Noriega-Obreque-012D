package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.domain.Cotizacion
import cl.duoc.paoliciassweet.domain.Cotizador
import cl.duoc.paoliciassweet.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CotizarUiState(
    val producto: Producto? = null,
    val fechaMillis: Long? = null,
    val bloque: String = "",
    val asistentes: String = "",
    val observaciones: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val cotizacion: Cotizacion? = null,
    val fechaError: String? = null,
    val bloqueError: String? = null,
    val asistentesError: String? = null,
    val observacionesError: String? = null,
    val mensaje: String? = null,
    val guardado: Boolean = false
)

class CotizarViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))
    private val _state = MutableStateFlow(CotizarUiState())
    val state: StateFlow<CotizarUiState> = _state.asStateFlow()

    fun cargar(productoId: Long) {
        viewModelScope.launch {
            val producto = repo.producto(productoId)
            _state.update { it.copy(producto = producto) }
        }
    }

    fun onFecha(millis: Long?) = _state.update {
        it.copy(fechaMillis = millis, fechaError = Validaciones.fecha(millis), mensaje = null)
    }

    fun onBloque(valor: String) = _state.update {
        it.copy(bloque = valor, bloqueError = Validaciones.bloque(valor), mensaje = null)
    }

    // La cotización se recalcula en tiempo real mientras el cliente escribe
    fun onAsistentes(valor: String) = _state.update {
        val error = Validaciones.asistentes(valor)
        val cotizacion = if (error == null && it.producto != null)
            Cotizador.cotizar(it.producto.precioPorPersona, valor.trim().toInt())
        else null
        it.copy(asistentes = valor, asistentesError = error, cotizacion = cotizacion, mensaje = null)
    }

    fun onObservaciones(valor: String) = _state.update {
        it.copy(observaciones = valor, observacionesError = Validaciones.observaciones(valor))
    }

    fun onUbicacion(latitud: Double?, longitud: Double?) = _state.update {
        it.copy(latitud = latitud, longitud = longitud)
    }

    fun reservar() {
        val s = _state.value
        val producto = s.producto ?: return
        val errorFecha = Validaciones.fecha(s.fechaMillis)
        val errorBloque = Validaciones.bloque(s.bloque)
        val errorAsistentes = Validaciones.asistentes(s.asistentes)
        val errorObservaciones = Validaciones.observaciones(s.observaciones)
        _state.update {
            it.copy(
                fechaError = errorFecha,
                bloqueError = errorBloque,
                asistentesError = errorAsistentes,
                observacionesError = errorObservaciones
            )
        }
        if (listOf(errorFecha, errorBloque, errorAsistentes, errorObservaciones).any { it != null }) return

        val usuarioId = Sesion.usuarioId.value ?: return
        val fecha = s.fechaMillis ?: return
        val asistentes = s.asistentes.trim().toInt()

        viewModelScope.launch {
            if (repo.estaOcupado(fecha, s.bloque)) {
                _state.update {
                    it.copy(mensaje = "Esa fecha y horario ya tienen un evento confirmado. Elige otro.")
                }
                return@launch
            }
            val cotizacion = Cotizador.cotizar(producto.precioPorPersona, asistentes)
            repo.crearReserva(
                Reserva(
                    usuarioId = usuarioId,
                    productoId = producto.id,
                    productoNombre = producto.nombre,
                    fechaEvento = fecha,
                    bloque = s.bloque,
                    asistentes = asistentes,
                    observaciones = s.observaciones.trim(),
                    total = cotizacion.total,
                    abono = cotizacion.abono,
                    latitud = s.latitud,
                    longitud = s.longitud,
                    creadaEn = System.currentTimeMillis()
                )
            )
            _state.update { it.copy(guardado = true) }
        }
    }
}
