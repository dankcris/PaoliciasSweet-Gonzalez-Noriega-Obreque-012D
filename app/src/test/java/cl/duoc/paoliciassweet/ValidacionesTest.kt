package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.domain.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun email_validoEInvalido() {
        assertNull(Validaciones.email("cliente@correo.cl"))
        assertNotNull(Validaciones.email("cliente@correo"))
        assertNotNull(Validaciones.email(""))
    }

    @Test
    fun telefono_celularChileno() {
        assertNull(Validaciones.telefono("912345678"))
        assertNull(Validaciones.telefono("+56 9 1234 5678"))
        assertNotNull(Validaciones.telefono("12345"))
    }

    @Test
    fun password_requiereLargoYNumero() {
        assertNull(Validaciones.password("Clave123"))
        assertEquals("Mínimo 6 caracteres", Validaciones.password("ab1"))
        assertEquals("Debe incluir al menos un número", Validaciones.password("abcdefg"))
    }

    @Test
    fun asistentes_rangoDiezACien() {
        assertNull(Validaciones.asistentes("10"))
        assertNull(Validaciones.asistentes("100"))
        assertNotNull(Validaciones.asistentes("9"))
        assertNotNull(Validaciones.asistentes("101"))
        assertNotNull(Validaciones.asistentes("abc"))
        assertNotNull(Validaciones.asistentes(""))
    }

    @Test
    fun confirmar_contrasenasDistintas() {
        assertNotNull(Validaciones.confirmar("Clave123", "Clave124"))
        assertNull(Validaciones.confirmar("Clave123", "Clave123"))
    }
}
