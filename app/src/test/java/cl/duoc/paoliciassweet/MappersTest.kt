package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.network.FechaMapper
import cl.duoc.paoliciassweet.network.ProductoDto
import cl.duoc.paoliciassweet.network.ReservaDto
import cl.duoc.paoliciassweet.network.reservaNotas
import cl.duoc.paoliciassweet.network.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private fun dto(notas: String?, fecha: String = "2026-12-05") = ReservaDto(
        id = 8, clienteNombre = "C", clienteTelefono = "9", clienteEmail = null,
        productoId = 3, productoNombre = "Torta", asistentes = 40, fechaEvento = fecha,
        direccionEvento = null, notas = notas, total = 100000, abono = 50000, estado = "ABONO_50"
    )

    @Test
    fun productoDto_aEntidad() {
        val p = ProductoDto(4, "Cheesecake", "Frutos rojos", "Tortas", 4200).toEntity()
        assertEquals(4L, p.id)
        assertEquals("Tortas", p.categoria)
        assertEquals(4200, p.precioPorPersona)
    }

    @Test
    fun reservaDto_separaBloqueYObservaciones() {
        val r = dto("Bloque: Mañana (09:00 - 13:00) | alergia al maní | sin lácteos").toEntity(5)
        assertEquals("Mañana (09:00 - 13:00)", r.bloque)
        assertEquals("alergia al maní | sin lácteos", r.observaciones)
        assertEquals(5L, r.usuarioId)
        assertEquals("ABONO_50", r.estado)
        assertEquals(50000, r.abono)
    }

    @Test
    fun reservaDto_sinNotasUsaBloquePorDefinir() {
        val r = dto(null).toEntity(1)
        assertEquals("Por definir", r.bloque)
        assertEquals("", r.observaciones)
        assertNull(r.latitud)
    }

    @Test
    fun reservaDto_convierteLaFechaIsoAMillis() {
        assertEquals(86_400_000L, dto("Bloque: X", "1970-01-02").toEntity(1).fechaEvento)
    }

    @Test
    fun reservaNotas_combinaBloqueYObservaciones() {
        assertEquals("Bloque: Tarde", reservaNotas("Tarde", "  "))
        assertEquals("Bloque: Tarde | sin gluten", reservaNotas("Tarde", " sin gluten "))
    }

    @Test
    fun reservaNotas_seRevierteConElMapper() {
        val r = dto(reservaNotas("Noche", "vegano")).toEntity(1)
        assertEquals("Noche", r.bloque)
        assertEquals("vegano", r.observaciones)
    }

    @Test
    fun fechaMapper_idaYVuelta() {
        assertEquals("1970-01-01", FechaMapper.longToIsoString(0))
        assertEquals("2026-11-26", FechaMapper.longToIsoString(FechaMapper.isoStringToLong("2026-11-26")))
    }

    @Test
    fun fechaMapper_aceptaFechaConHora() {
        assertEquals(86_400_000L, FechaMapper.isoStringToLong("1970-01-02T10:15:00"))
    }

    @Test
    fun fechaMapper_fechaInvalidaRetornaAhora() {
        assertTrue(FechaMapper.isoStringToLong("no-es-fecha") > 1_000_000_000_000L)
    }
}
