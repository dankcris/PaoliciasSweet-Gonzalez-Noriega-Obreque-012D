package cl.duoc.paoliciassweet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?
}

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY categoria, nombre")
    fun observarTodos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Producto?
}

@Dao
interface ReservaDao {
    @Insert
    suspend fun insertar(reserva: Reserva): Long

    @Query("SELECT * FROM reservas WHERE usuarioId = :usuarioId ORDER BY fechaEvento ASC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<Reserva>>

    @Query("SELECT * FROM reservas WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    fun observarUna(id: Long, usuarioId: Long): Flow<Reserva?>

    // Regla de negocio: solo un evento con abono validado por fecha y horario
    @Query(
        "SELECT COUNT(*) FROM reservas WHERE fechaEvento = :fecha AND bloque = :bloque " +
            "AND estado IN ('ABONO_50', 'COMPLETADO')"
    )
    suspend fun contarOcupadas(fecha: Long, bloque: String): Int

    @Query(
        "UPDATE reservas SET comprobanteUri = :uri, estado = 'EN_REVISION' " +
            "WHERE id = :id AND usuarioId = :usuarioId AND estado IN ('PENDIENTE', 'EN_REVISION')"
    )
    suspend fun adjuntarComprobante(id: Long, usuarioId: Long, uri: String)

    @Query("UPDATE reservas SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String)

    @Query("UPDATE reservas SET estado = 'PENDIENTE', comprobanteUri = NULL WHERE id = :id")
    suspend fun rechazarComprobante(id: Long)

    @Query(
        "SELECT r.id AS id, r.productoNombre AS productoNombre, r.fechaEvento AS fechaEvento, " +
            "r.bloque AS bloque, r.asistentes AS asistentes, r.observaciones AS observaciones, " +
            "r.total AS total, r.abono AS abono, r.estado AS estado, " +
            "r.comprobanteUri AS comprobanteUri, r.latitud AS latitud, r.longitud AS longitud, " +
            "u.nombre AS clienteNombre, u.email AS clienteEmail, " +
            "u.telefono AS clienteTelefono, u.empresa AS clienteEmpresa " +
            "FROM reservas r INNER JOIN usuarios u ON u.id = r.usuarioId " +
            "ORDER BY r.fechaEvento ASC, r.id ASC"
    )
    fun observarAgenda(): Flow<List<ReservaAgenda>>
}
