package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.domain.Indicadores
import cl.duoc.paoliciassweet.network.IndicadorDto
import cl.duoc.paoliciassweet.network.IndicadoresDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IndicadoresTest {

    @Test
    fun resumir_tomaFechaDelDolarSiExiste() {
        val dto = IndicadoresDto(
            uf = IndicadorDto("uf", "UF", "Pesos", "2026-11-22T00:00:00.000Z", 39000.0),
            dolar = IndicadorDto("dolar", "Dólar", "Pesos", "2026-11-23T00:00:00.000Z", 950.0),
            euro = null, utm = null
        )
        val r = Indicadores.resumir(dto)
        assertEquals("2026-11-23", r.fecha)
        assertEquals(39000.0, r.uf ?: 0.0, 0.001)
    }

    @Test
    fun resumir_sinDatosRetornaNulos() {
        val r = Indicadores.resumir(IndicadoresDto(null, null, null, null))
        assertNull(r.uf)
        assertNull(r.dolar)
        assertNull(r.fecha)
    }

    @Test
    fun resumir_usaFechaDeLaUfSiNoHayDolar() {
        val dto = IndicadoresDto(IndicadorDto("uf", "UF", "Pesos", "2026-11-22T00:00:00.000Z", 1.0), null, null, null)
        assertEquals("2026-11-22", Indicadores.resumir(dto).fecha)
    }

    @Test
    fun clpAUsd_convierteConTipoDeCambioValido() {
        assertEquals(100.0, Indicadores.clpAUsd(95000, 950.0) ?: 0.0, 0.001)
    }

    @Test
    fun clpAUsd_sinTipoDeCambioValidoRetornaNull() {
        assertNull(Indicadores.clpAUsd(1000, null))
        assertNull(Indicadores.clpAUsd(1000, 0.0))
        assertNull(Indicadores.clpAUsd(1000, -5.0))
    }
}
