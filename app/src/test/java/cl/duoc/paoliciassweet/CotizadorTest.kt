package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.domain.Cotizador
import org.junit.Assert.assertEquals
import org.junit.Test

class CotizadorTest {

    @Test
    fun tramo_redondeaHaciaArribaDeDiezEnDiez() {
        assertEquals(10, Cotizador.tramo(10))
        assertEquals(30, Cotizador.tramo(21))
        assertEquals(100, Cotizador.tramo(100))
    }

    @Test
    fun cotizar_hastaVeintePersonas_sinDescuento() {
        val c = Cotizador.cotizar(precioPorPersona = 10_000, asistentes = 20)
        assertEquals(0, c.descuentoPct)
        assertEquals(200_000, c.total)
        assertEquals(100_000, c.abono)
    }

    @Test
    fun cotizar_veinticincoPersonas_cobraTramoDe30Con5PorCiento() {
        val c = Cotizador.cotizar(precioPorPersona = 10_000, asistentes = 25)
        assertEquals(30, c.tramo)
        assertEquals(5, c.descuentoPct)
        assertEquals(285_000, c.total)
        assertEquals(142_500, c.abono)
    }

    @Test
    fun cotizar_cienPersonas_descuentoDiezPorCiento() {
        val c = Cotizador.cotizar(precioPorPersona = 10_000, asistentes = 100)
        assertEquals(10, c.descuentoPct)
        assertEquals(900_000, c.total)
    }

    @Test
    fun personal_estimadoPorAsistentes() {
        assertEquals(1, Cotizador.garzones(20))
        assertEquals(2, Cotizador.garzones(21))
        assertEquals(1, Cotizador.ayudantesCocina(40))
        assertEquals(3, Cotizador.ayudantesCocina(81))
    }
}
