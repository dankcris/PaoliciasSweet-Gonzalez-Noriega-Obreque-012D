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
import cl.duoc.paoliciassweet.network.FechaMapper
import cl.duoc.paoliciassweet.network.ReservaRequest
import cl.duoc.paoliciassweet.network.reservaNotas
import cl.duoc.paoliciassweet.network.RetrofitClient
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
            val producto = try {
                RetrofitClient.remoto.producto(productoId) ?: repo.producto(productoId)
            } catch (_: Exception) {
                repo.producto(productoId)
            }
            _state.update { it.copy(producto = producto) }
        }
    }

    fun onFecha(millis: Long?) = _state.update {
        it.copy(fechaMillis = millis, fechaError = Validaciones.fecha(millis), mensaje = null)
    }

    fun onBloque(valor: String) = _state.update {
        it.copy(bloque = valor, bloqueError = Validaciones.bloque(valor), mensaje = null)
    }

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
        val fechaIso = FechaMapper.longToIsoString(fecha)

        viewModelScope.launch {
            try {
                // 1. Verificar disponibilidad en el backend y en local
                val ocupadaRemota = RetrofitClient.remoto.fechaOcupada(fechaIso, s.bloque)
                if (ocupadaRemota || repo.estaOcupado(fecha, s.bloque)) {
                    _state.update {
                        it.copy(mensaje = "Esa fecha y horario ya tienen un evento confirmado. Elige otro.")
                    }
                    return@launch
                }

                val cotizacion = Cotizador.cotizar(producto.precioPorPersona, asistentes)
                val ubicacionTexto = if (s.latitud != null && s.longitud != null) {
                    "GPS: ${s.latitud}, ${s.longitud}"
                } else {
                    "Por coordinar"
                }
                val notasCombinadas = reservaNotas(s.bloque, s.observaciones)

                // 2. Guardar reserva en PostgreSQL (puerto 8082)
                val reservaCreada = RetrofitClient.remoto.crearReserva(
                    ReservaRequest(
                        clienteNombre = "Cliente #$usuarioId",
                        clienteTelefono = "+56900000000",
                        clienteEmail = "usuario$usuarioId@paoliciassweet.cl",
                        productoId = producto.id,
                        productoNombre = producto.nombre,
                        asistentes = asistentes,
                        fechaEvento = fechaIso,
                        direccionEvento = ubicacionTexto,
                        notas = notasCombinadas,
                        total = cotizacion.total,
                        abono = cotizacion.abono
                    )
                )

                // 3. Mantener copia sincronizada en local con el mismo ID
                repo.crearReserva(
                    Reserva(
                        id = reservaCreada.id,
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
            } catch (e: Exception) {
                _state.update {
                    it.copy(mensaje = "Error al conectar con el servidor: ${e.localizedMessage}")
                }
            }
        }
    }
}
