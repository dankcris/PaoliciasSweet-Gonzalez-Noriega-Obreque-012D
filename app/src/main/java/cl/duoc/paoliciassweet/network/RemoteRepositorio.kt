package cl.duoc.paoliciassweet.network

import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.domain.Indicadores
import cl.duoc.paoliciassweet.domain.ResumenIndicadores
import okhttp3.MultipartBody

/**
 * Acceso unificado a los 3 microservicios propios (catálogo, reservas, comprobantes)
 * y a la API externa de indicadores económicos.
 */
class RemoteRepositorio(
    private val catalogoApi: CatalogoApi,
    private val reservasApi: ReservasApi,
    private val comprobantesApi: ComprobantesApi,
    private val indicadoresApi: IndicadoresApi
) {
    companion object {
        const val ESTADO_CANCELADA = "CANCELADA"

        fun emailDe(usuarioId: Long): String = "usuario$usuarioId@paoliciassweet.cl"
    }

    // ---- Catálogo ----
    suspend fun productos(): List<Producto> =
        catalogoApi.listarProductos().map { it.toEntity() }

    suspend fun producto(id: Long): Producto? = productos().find { it.id == id }

    suspend fun cotizar(productoId: Long, asistentes: Int): CotizacionDto =
        catalogoApi.cotizar(CotizacionRequest(productoId, asistentes))

    // ---- Reservas (CRUD) ----
    suspend fun reservas(email: String? = null): List<ReservaDto> = reservasApi.listarReservas(email)

    suspend fun reservasDeUsuario(usuarioId: Long): List<Reserva> =
        reservasApi.listarReservas(emailDe(usuarioId)).map { it.toEntity(usuarioId) }

    suspend fun fechaOcupada(fechaIso: String, bloque: String): Boolean =
        reservasApi.listarReservas().any {
            it.fechaEvento == fechaIso &&
                it.notas?.contains("Bloque: $bloque") == true &&
                it.estado != ESTADO_CANCELADA
        }

    suspend fun crearReserva(request: ReservaRequest): ReservaDto = reservasApi.crearReserva(request)

    suspend fun actualizarReserva(id: Long, request: ReservaRequest): ReservaDto =
        reservasApi.actualizarReserva(id, request)

    suspend fun cambiarEstado(id: Long, estado: String): ReservaDto =
        reservasApi.cambiarEstado(id, mapOf("estado" to estado))

    suspend fun eliminarReserva(id: Long) = reservasApi.eliminarReserva(id)

    // ---- Comprobantes ----
    suspend fun subirComprobante(reservaId: Long, monto: Int, archivo: MultipartBody.Part): ComprobanteDto =
        comprobantesApi.subirComprobante(
            reservaId = UriMultipartHelper.textToRequestBody(reservaId.toString()),
            montoAbonado = UriMultipartHelper.textToRequestBody(monto.toString()),
            archivo = archivo
        )

    suspend fun comprobantes(reservaId: Long): List<ComprobanteDto> =
        comprobantesApi.listarPorReserva(reservaId)

    // ---- API externa (mindicador.cl) ----
    suspend fun indicadores(): Result<ResumenIndicadores> =
        try {
            Result.success(Indicadores.resumir(indicadoresApi.indicadores()))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
