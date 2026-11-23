
package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.ProductoDao
import cl.duoc.paoliciassweet.data.Reserva
import cl.duoc.paoliciassweet.data.ReservaAgenda
import cl.duoc.paoliciassweet.data.ReservaDao
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Roles
import cl.duoc.paoliciassweet.data.Usuario
import cl.duoc.paoliciassweet.data.UsuarioDao
import cl.duoc.paoliciassweet.data.hashPassword
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositorioTest {

    private lateinit var db: AppDatabase
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var productoDao: ProductoDao
    private lateinit var reservaDao: ReservaDao
    private lateinit var repositorio: Repositorio

    @Before
    fun preparar() {
        db = mockk()
        usuarioDao = mockk()
        productoDao = mockk()
        reservaDao = mockk()

        every { db.usuarioDao() } returns usuarioDao
        every { db.productoDao() } returns productoDao
        every { db.reservaDao() } returns reservaDao

        repositorio = Repositorio(db)
    }

    private fun usuario(
        email: String = "usuario@correo.cl",
        password: String = "Clave123"
    ) = Usuario(
        id = 1L,
        nombre = "Juan Perez",
        email = email,
        telefono = "912345678",
        empresa = "Empresa",
        passwordHash = hashPassword(password),
        rol = Roles.USER
    )

    private fun producto() = Producto(
        id = 1L,
        nombre = "Torta",
        descripcion = "Torta de prueba",
        categoria = "Pasteleria",
        precioPorPersona = 5000
    )

    private fun reserva() = Reserva(
        id = 1L,
        usuarioId = 1L,
        productoId = 1L,
        productoNombre = "Torta",
        fechaEvento = 1000L,
        bloque = "Tarde",
        asistentes = 20,
        observaciones = "Sin gluten",
        total = 100000,
        abono = 50000,
        creadaEn = 500L
    )

    @Test
    fun registrar_guardaUsuarioConDatosNormalizados() = runTest {
        coEvery { usuarioDao.insertar(any()) } returns 10L

        val resultado = repositorio.registrar(
            nombre = "  Juan Perez  ",
            email = " JUAN@CORREO.CL ",
            telefono = " 912-345-678 ",
            empresa = " Empresa ",
            password = "Clave123"
        )

        assertEquals(10L, resultado)

        coVerify {
            usuarioDao.insertar(match {
                it.nombre == "Juan Perez" &&
                        it.email == "juan@correo.cl" &&
                        it.telefono == "912345678" &&
                        it.empresa == "Empresa" &&
                        it.passwordHash == hashPassword("Clave123") &&
                        it.rol == Roles.USER
            })
        }
    }

    @Test
    fun login_devuelveUsuarioConCredencialesCorrectas() = runTest {
        val esperado = usuario()
        coEvery {
            usuarioDao.buscarPorEmail("usuario@correo.cl")
        } returns esperado

        val resultado = repositorio.login(
            " USUARIO@CORREO.CL ",
            "Clave123"
        )

        assertEquals(esperado, resultado)
        coVerify { usuarioDao.buscarPorEmail("usuario@correo.cl") }
    }

    @Test
    fun login_devuelveNullCuandoUsuarioNoExiste() = runTest {
        coEvery {
            usuarioDao.buscarPorEmail("desconocido@correo.cl")
        } returns null

        val resultado = repositorio.login(
            "desconocido@correo.cl",
            "Clave123"
        )

        assertNull(resultado)
    }

    @Test
    fun login_devuelveNullCuandoPasswordEsIncorrecta() = runTest {
        coEvery {
            usuarioDao.buscarPorEmail("usuario@correo.cl")
        } returns usuario()

        val resultado = repositorio.login(
            "usuario@correo.cl",
            "Incorrecta"
        )

        assertNull(resultado)
    }

    @Test
    fun productos_devuelveFlujoDelDao() {
        val esperado = flowOf(listOf(producto()))
        every { productoDao.observarTodos() } returns esperado

        val resultado = repositorio.productos()

        assertSame(esperado, resultado)
        verify { productoDao.observarTodos() }
    }

    @Test
    fun producto_buscaPorId() = runTest {
        val esperado = producto()
        coEvery { productoDao.buscarPorId(1L) } returns esperado

        val resultado = repositorio.producto(1L)

        assertEquals(esperado, resultado)
    }

    @Test
    fun producto_devuelveNullCuandoNoExiste() = runTest {
        coEvery { productoDao.buscarPorId(99L) } returns null

        val resultado = repositorio.producto(99L)

        assertNull(resultado)
    }

    @Test
    fun reservas_devuelveFlujoDelUsuario() {
        val esperado = flowOf(listOf(reserva()))
        every { reservaDao.observarPorUsuario(1L) } returns esperado

        val resultado = repositorio.reservas(1L)

        assertSame(esperado, resultado)
        verify { reservaDao.observarPorUsuario(1L) }
    }

    @Test
    fun reserva_devuelveFlujoDeReservaEspecifica() {
        val esperado = flowOf(reserva())
        every {
            reservaDao.observarUna(1L, 1L)
        } returns esperado

        val resultado = repositorio.reserva(1L, 1L)

        assertSame(esperado, resultado)
        verify { reservaDao.observarUna(1L, 1L) }
    }

    @Test
    fun estaOcupado_devuelveTrueCuandoHayReservasOcupadas() = runTest {
        coEvery {
            reservaDao.contarOcupadas(1000L, "Tarde")
        } returns 1

        assertTrue(repositorio.estaOcupado(1000L, "Tarde"))
    }

    @Test
    fun estaOcupado_devuelveFalseCuandoNoHayReservasOcupadas() = runTest {
        coEvery {
            reservaDao.contarOcupadas(1000L, "Tarde")
        } returns 0

        assertFalse(repositorio.estaOcupado(1000L, "Tarde"))
    }

    @Test
    fun crearReserva_devuelveIdInsertado() = runTest {
        val nuevaReserva = reserva()
        coEvery { reservaDao.insertar(nuevaReserva) } returns 25L

        val resultado = repositorio.crearReserva(nuevaReserva)

        assertEquals(25L, resultado)
        coVerify { reservaDao.insertar(nuevaReserva) }
    }

    @Test
    fun adjuntarComprobante_delegaEnDao() = runTest {
        coEvery {
            reservaDao.adjuntarComprobante(1L, 2L, "archivo://comprobante")
        } returns Unit

        repositorio.adjuntarComprobante(
            1L,
            2L,
            "archivo://comprobante"
        )

        coVerify {
            reservaDao.adjuntarComprobante(
                1L,
                2L,
                "archivo://comprobante"
            )
        }
    }

    @Test
    fun eliminarReserva_delegaEnDao() = runTest {
        coEvery { reservaDao.eliminar(1L, 2L) } returns Unit

        repositorio.eliminarReserva(1L, 2L)

        coVerify { reservaDao.eliminar(1L, 2L) }
    }

    @Test
    fun agenda_devuelveFlujoDelDao() {
        val esperado = flowOf(
            emptyList<ReservaAgenda>()
        )
        every { reservaDao.observarAgenda() } returns esperado

        val resultado = repositorio.agenda()

        assertSame(esperado, resultado)
        verify { reservaDao.observarAgenda() }
    }

    @Test
    fun cambiarEstado_delegaEnDao() = runTest {
        coEvery {
            reservaDao.actualizarEstado(1L, "COMPLETADO")
        } returns Unit

        repositorio.cambiarEstado(1L, "COMPLETADO")

        coVerify {
            reservaDao.actualizarEstado(1L, "COMPLETADO")
        }
    }

    @Test
    fun rechazarComprobante_delegaEnDao() = runTest {
        coEvery { reservaDao.rechazarComprobante(1L) } returns Unit

        repositorio.rechazarComprobante(1L)

        coVerify { reservaDao.rechazarComprobante(1L) }
    }
}
