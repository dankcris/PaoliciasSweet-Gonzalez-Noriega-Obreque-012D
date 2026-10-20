package cl.duoc.paoliciassweet.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.Flow

class Repositorio(private val db: AppDatabase) {

    // ---- Usuarios ----
    suspend fun registrar(
        nombre: String,
        email: String,
        telefono: String,
        empresa: String,
        password: String
    ): Long? =
        try {
            db.usuarioDao().insertar(
                Usuario(
                    nombre = nombre.trim(),
                    email = email.trim().lowercase(),
                    telefono = telefono.replace(" ", "").replace("-", ""),
                    empresa = empresa.trim(),
                    passwordHash = hashPassword(password),
                    rol = Roles.USER
                )
            )
        } catch (e: SQLiteConstraintException) {
            null
        }

    suspend fun login(email: String, password: String): Usuario? {
        val usuario = db.usuarioDao().buscarPorEmail(email.trim().lowercase()) ?: return null
        return if (usuario.passwordHash == hashPassword(password)) usuario else null
    }

    // ---- Catálogo ----
    fun productos(): Flow<List<Producto>> = db.productoDao().observarTodos()

    suspend fun producto(id: Long): Producto? = db.productoDao().buscarPorId(id)

    // ---- Reservas (cliente) ----
    fun reservas(usuarioId: Long): Flow<List<Reserva>> = db.reservaDao().observarPorUsuario(usuarioId)

    fun reserva(id: Long, usuarioId: Long): Flow<Reserva?> = db.reservaDao().observarUna(id, usuarioId)

    suspend fun estaOcupado(fechaEvento: Long, bloque: String): Boolean =
        db.reservaDao().contarOcupadas(fechaEvento, bloque) > 0

    suspend fun crearReserva(reserva: Reserva): Long = db.reservaDao().insertar(reserva)

    suspend fun adjuntarComprobante(id: Long, usuarioId: Long, uri: String) =
        db.reservaDao().adjuntarComprobante(id, usuarioId, uri)

    // ---- Agenda (administradora) ----
    fun agenda(): Flow<List<ReservaAgenda>> = db.reservaDao().observarAgenda()

    suspend fun cambiarEstado(id: Long, estado: String) = db.reservaDao().actualizarEstado(id, estado)

    suspend fun rechazarComprobante(id: Long) = db.reservaDao().rechazarComprobante(id)
}
