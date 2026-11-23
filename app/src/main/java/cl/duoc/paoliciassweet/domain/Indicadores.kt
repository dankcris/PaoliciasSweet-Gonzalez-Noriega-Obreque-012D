package cl.duoc.paoliciassweet.domain

import cl.duoc.paoliciassweet.network.IndicadoresDto

data class ResumenIndicadores(
    val uf: Double?,
    val dolar: Double?,
    val fecha: String?
)

object Indicadores {
    fun resumir(dto: IndicadoresDto): ResumenIndicadores = ResumenIndicadores(
        uf = dto.uf?.valor,
        dolar = dto.dolar?.valor,
        fecha = (dto.dolar?.fecha ?: dto.uf?.fecha)?.take(10)
    )

    /** Equivalente en dólares de un monto en pesos; null si no hay tipo de cambio válido. */
    fun clpAUsd(clp: Int, dolar: Double?): Double? =
        if (dolar == null || dolar <= 0.0) null else clp / dolar
}
