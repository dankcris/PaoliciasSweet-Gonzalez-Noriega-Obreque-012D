package cl.duoc.paoliciassweet.network

import com.google.gson.annotations.SerializedName

// --- Catálogo y Cotizador (Puerto 8081) ---
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precioPorPersona: Int
)

data class CotizacionRequest(
    val productoId: Long,
    val asistentes: Int
)

data class CotizacionDto(
    val asistentes: Int,
    val tramo: Int,
    val descuentoPct: Int,
    val subtotal: Int,
    val total: Int,
    val abono: Int
)

// --- Reservas (Puerto 8082) ---
data class ReservaRequest(
    val clienteNombre: String,
    val clienteTelefono: String,
    val clienteEmail: String?,
    val productoId: Long,
    val productoNombre: String,
    val asistentes: Int,
    val fechaEvento: String,
    val direccionEvento: String?,
    val notas: String?,
    val total: Int,
    val abono: Int
)

data class ReservaDto(
    val id: Long,
    val clienteNombre: String,
    val clienteTelefono: String,
    val clienteEmail: String?,
    val productoId: Long,
    val productoNombre: String,
    val asistentes: Int,
    val fechaEvento: String,
    val direccionEvento: String?,
    val notas: String?,
    val total: Int,
    val abono: Int,
    val estado: String
)

// --- Comprobantes (Puerto 8083) ---
data class ComprobanteDto(
    val id: Long,
    val reservaId: Long,
    val nombreArchivo: String,
    val montoAbonado: Int?,
    val estado: String
)


// --- API externa: mindicador.cl (UF, dólar, euro, UTM) ---
data class IndicadorDto(
    val codigo: String?,
    val nombre: String?,
    @SerializedName("unidad_medida") val unidadMedida: String?,
    val fecha: String?,
    val valor: Double?
)

data class IndicadoresDto(
    val uf: IndicadorDto?,
    val dolar: IndicadorDto?,
    val euro: IndicadorDto?,
    val utm: IndicadorDto?
)
