package cl.duoc.paoliciassweet.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val email: String,
    val telefono: String,
    val empresa: String,
    val passwordHash: String,
    val rol: String = Roles.USER
)

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precioPorPersona: Int
)

@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val productoId: Long,
    val productoNombre: String,
    val fechaEvento: Long,
    val bloque: String,
    val asistentes: Int,
    val observaciones: String,
    val total: Int,
    val abono: Int,
    val estado: String = EstadoReserva.PENDIENTE,
    val comprobanteUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val creadaEn: Long
)

/** Fila de la agenda del administrador: reserva + datos del cliente. */
data class ReservaAgenda(
    val id: Long,
    val productoNombre: String,
    val fechaEvento: Long,
    val bloque: String,
    val asistentes: Int,
    val observaciones: String,
    val total: Int,
    val abono: Int,
    val estado: String,
    val comprobanteUri: String?,
    val latitud: Double?,
    val longitud: Double?,
    val clienteNombre: String,
    val clienteEmail: String,
    val clienteTelefono: String,
    val clienteEmpresa: String
)
