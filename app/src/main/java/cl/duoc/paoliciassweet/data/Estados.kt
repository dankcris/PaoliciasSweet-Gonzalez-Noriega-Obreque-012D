package cl.duoc.paoliciassweet.data

/** Estado del abono del 50 % que bloquea la fecha. */
object EstadoReserva {
    const val PENDIENTE = "PENDIENTE"
    const val EN_REVISION = "EN_REVISION"
    const val ABONO_50 = "ABONO_50"
    const val COMPLETADO = "COMPLETADO"

    val todos = listOf(PENDIENTE, EN_REVISION, ABONO_50, COMPLETADO)

    fun etiqueta(estado: String): String = when (estado) {
        PENDIENTE -> "Pendiente de abono"
        EN_REVISION -> "Comprobante en revisión"
        ABONO_50 -> "Abono 50% pagado"
        COMPLETADO -> "Completado"
        else -> estado
    }
}
