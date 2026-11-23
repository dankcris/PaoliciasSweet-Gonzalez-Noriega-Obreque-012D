package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.data.EstadoReserva
import cl.duoc.paoliciassweet.data.Roles
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.data.hashPassword
import cl.duoc.paoliciassweet.domain.Fechas
import cl.duoc.paoliciassweet.domain.Validaciones
import cl.duoc.paoliciassweet.domain.moneda
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DominioTest {

    @After
    fun limpiarSesion() = Sesion.cerrar()

    @Test
    fun hashPassword_sha256Conocido() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            hashPassword("abc")
        )
    }

    @Test
    fun hashPassword_esDeterministaYDistingueEntradas() {
        assertEquals(hashPassword("Clave123"), hashPassword("Clave123"))
        assertFalse(hashPassword("Clave123") == hashPassword("clave123"))
    }

    @Test
    fun estadoReserva_etiquetas() {
        assertEquals("Pendiente de abono", EstadoReserva.etiqueta(EstadoReserva.PENDIENTE))
        assertEquals("Comprobante en revisión", EstadoReserva.etiqueta(EstadoReserva.EN_REVISION))
        assertEquals("Abono 50% pagado", EstadoReserva.etiqueta(EstadoReserva.ABONO_50))
        assertEquals("Completado", EstadoReserva.etiqueta(EstadoReserva.COMPLETADO))
        assertEquals("OTRO", EstadoReserva.etiqueta("OTRO"))
        assertEquals(4, EstadoReserva.todos.size)
    }

    @Test
    fun sesion_iniciaYCierra() {
        Sesion.iniciar(3, Roles.USER)
        assertEquals(3L, Sesion.usuarioId.value)
        assertFalse(Sesion.esAdmin)
        Sesion.iniciar(1, Roles.ADMIN)
        assertTrue(Sesion.esAdmin)
        Sesion.cerrar()
        assertNull(Sesion.usuarioId.value)
        assertNull(Sesion.rol.value)
    }

    @Test
    fun moneda_formateaPesosChilenos() {
        assertTrue(moneda(1500).contains("1.500"))
    }

    @Test
    fun fechas_formatearUtc() {
        assertEquals("01/01/1970", Fechas.formatear(0L))
        assertEquals("02/01/1970", Fechas.formatear(86_400_000L))
    }

    @Test
    fun validaciones_fecha() {
        assertNotNull(Validaciones.fecha(null))
        assertNotNull(Validaciones.fecha(0L))
        assertNull(Validaciones.fecha(Fechas.hoyUtc()))
    }

    @Test
    fun validaciones_bloqueYObservaciones() {
        assertNotNull(Validaciones.bloque(" "))
        assertNull(Validaciones.bloque("Tarde"))
        assertNull(Validaciones.observaciones("sin gluten"))
        assertNotNull(Validaciones.observaciones("x".repeat(201)))
    }

    @Test
    fun validaciones_nombreYEmpresa() {
        assertNull(Validaciones.nombre("María Pérez"))
        assertNotNull(Validaciones.nombre(""))
        assertNotNull(Validaciones.nombre("ab"))
        assertNotNull(Validaciones.nombre("Juan123"))
        assertNull(Validaciones.empresa(""))
        assertNotNull(Validaciones.empresa("x".repeat(41)))
    }
}
