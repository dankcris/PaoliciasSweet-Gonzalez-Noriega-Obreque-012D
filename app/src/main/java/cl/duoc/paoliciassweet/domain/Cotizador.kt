package cl.duoc.paoliciassweet.domain

data class Cotizacion(
    val asistentes: Int,
    val tramo: Int,
    val descuentoPct: Int,
    val subtotal: Int,
    val total: Int,
    val abono: Int
)

/**
 * Tarifario escalonado (datos sintéticos): se cobra por tramos de 10 personas con tope de 100.
 * Descuento por volumen: hasta 20 personas 0 %, hasta 50 personas 5 %, hasta 100 personas 10 %.
 * El abono del 50 % es obligatorio para bloquear la fecha.
 */
object Cotizador {
    const val MIN_ASISTENTES = 10
    const val MAX_ASISTENTES = 100

    fun tramo(asistentes: Int): Int =
        (((asistentes + 9) / 10) * 10).coerceIn(MIN_ASISTENTES, MAX_ASISTENTES)

    fun descuentoPct(tramo: Int): Int = when {
        tramo <= 20 -> 0
        tramo <= 50 -> 5
        else -> 10
    }

    fun cotizar(precioPorPersona: Int, asistentes: Int): Cotizacion {
        val tramo = tramo(asistentes)
        val descuento = descuentoPct(tramo)
        val subtotal = precioPorPersona * tramo
        val total = subtotal - subtotal * descuento / 100
        val abono = (total + 1) / 2
        return Cotizacion(asistentes, tramo, descuento, subtotal, total, abono)
    }

    // Estimación de dotación: 1 garzón cada 20 asistentes y 1 ayudante de cocina cada 40
    fun garzones(asistentes: Int): Int = (asistentes + 19) / 20

    fun ayudantesCocina(asistentes: Int): Int = (asistentes + 39) / 40
}
