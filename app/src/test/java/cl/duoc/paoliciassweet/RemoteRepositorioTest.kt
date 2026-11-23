package cl.duoc.paoliciassweet

import cl.duoc.paoliciassweet.network.CatalogoApi
import cl.duoc.paoliciassweet.network.ComprobanteDto
import cl.duoc.paoliciassweet.network.ComprobantesApi
import cl.duoc.paoliciassweet.network.CotizacionDto
import cl.duoc.paoliciassweet.network.CotizacionRequest
import cl.duoc.paoliciassweet.network.IndicadorDto
import cl.duoc.paoliciassweet.network.IndicadoresApi
import cl.duoc.paoliciassweet.network.IndicadoresDto
import cl.duoc.paoliciassweet.network.ProductoDto
import cl.duoc.paoliciassweet.network.RemoteRepositorio
import cl.duoc.paoliciassweet.network.ReservaDto
import cl.duoc.paoliciassweet.network.ReservaRequest
import cl.duoc.paoliciassweet.network.ReservasApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteRepositorioTest {

    private val catalogoApi = mockk<CatalogoApi>()
    private val reservasApi = mockk<ReservasApi>()
    private val comprobantesApi = mockk<ComprobantesApi>()
    private val indicadoresApi = mockk<IndicadoresApi>()
    private lateinit var repo: RemoteRepositorio

    @Before
    fun setUp() {
        repo = RemoteRepositorio(catalogoApi, reservasApi, comprobantesApi, indicadoresApi)
    }

    private fun reservaDto(
        id: Long = 1,
        fecha: String = "2026-12-05",
        notas: String? = "Bloque: Tarde | sin gluten",
        estado: String = "PENDIENTE"
    ) = ReservaDto(
        id = id, clienteNombre = "Cliente", clienteTelefono = "+56912345678",
        clienteEmail = "usuario7@paoliciassweet.cl", productoId = 2, productoNombre = "Torta",
        asistentes = 30, fechaEvento = fecha, direccionEvento = "Por coordinar",
        notas = notas, total = 96000, abono = 48000, estado = estado
    )

    private fun requestReserva() = ReservaRequest(
        clienteNombre = "Cliente", clienteTelefono = "+56912345678", clienteEmail = null,
        productoId = 2, productoNombre = "Torta", asistentes = 30, fechaEvento = "2026-12-05",
        direccionEvento = null, notas = null, total = 96000, abono = 48000
    )

    // ---- Catálogo ----
    @Test
    fun productos_mapeaDtoAEntidad() = runTest {
        coEvery { catalogoApi.listarProductos() } returns listOf(
            ProductoDto(1, "Torta Tres Leches", "Clásica", "Tortas", 3200),
            ProductoDto(2, "Menú ejecutivo", "Banquetera", "Banquetera", 8500)
        )
        val lista = repo.productos()
        assertEquals(2, lista.size)
        assertEquals("Torta Tres Leches", lista[0].nombre)
        assertEquals(8500, lista[1].precioPorPersona)
    }

    @Test
    fun producto_encuentraPorIdYRetornaNullSiNoExiste() = runTest {
        coEvery { catalogoApi.listarProductos() } returns listOf(ProductoDto(1, "A", "d", "Tortas", 1000))
        assertEquals("A", repo.producto(1)?.nombre)
        assertNull(repo.producto(99))
    }

    @Test
    fun cotizar_enviaProductoYAsistentes() = runTest {
        val esperado = CotizacionDto(30, 30, 5, 96000, 91200, 45600)
        coEvery { catalogoApi.cotizar(CotizacionRequest(1, 30)) } returns esperado
        assertEquals(esperado, repo.cotizar(1, 30))
    }

    // ---- Reservas ----
    @Test
    fun reservasDeUsuario_filtraPorEmailDelUsuarioYMapea() = runTest {
        coEvery { reservasApi.listarReservas("usuario7@paoliciassweet.cl") } returns listOf(reservaDto())
        val lista = repo.reservasDeUsuario(7)
        assertEquals(1, lista.size)
        assertEquals(7L, lista[0].usuarioId)
        assertEquals("Tarde", lista[0].bloque)
        assertEquals("sin gluten", lista[0].observaciones)
        coVerify { reservasApi.listarReservas("usuario7@paoliciassweet.cl") }
    }

    @Test
    fun emailDe_construyeCorreoDelUsuario() {
        assertEquals("usuario12@paoliciassweet.cl", RemoteRepositorio.emailDe(12))
    }

    @Test
    fun fechaOcupada_trueCuandoMismaFechaYBloque() = runTest {
        coEvery { reservasApi.listarReservas(null) } returns listOf(reservaDto())
        assertTrue(repo.fechaOcupada("2026-12-05", "Tarde"))
    }

    @Test
    fun fechaOcupada_falseSiOtraFechaOtroBloqueOCancelada() = runTest {
        coEvery { reservasApi.listarReservas(null) } returns listOf(
            reservaDto(id = 1, fecha = "2026-12-06"),
            reservaDto(id = 2, notas = "Bloque: Noche"),
            reservaDto(id = 3, estado = "CANCELADA"),
            reservaDto(id = 4, notas = null)
        )
        assertFalse(repo.fechaOcupada("2026-12-05", "Tarde"))
    }

    @Test
    fun crearReserva_delegaEnElMicroservicio() = runTest {
        val req = requestReserva()
        coEvery { reservasApi.crearReserva(req) } returns reservaDto(id = 10)
        assertEquals(10L, repo.crearReserva(req).id)
    }

    @Test
    fun actualizarReserva_usaPutConElId() = runTest {
        val req = requestReserva()
        coEvery { reservasApi.actualizarReserva(5, req) } returns reservaDto(id = 5)
        assertEquals(5L, repo.actualizarReserva(5, req).id)
    }

    @Test
    fun cambiarEstado_enviaMapaConElEstado() = runTest {
        coEvery { reservasApi.cambiarEstado(3, mapOf("estado" to "ABONO_50")) } returns
            reservaDto(id = 3, estado = "ABONO_50")
        assertEquals("ABONO_50", repo.cambiarEstado(3, "ABONO_50").estado)
    }

    @Test
    fun eliminarReserva_llamaDeleteConElId() = runTest {
        coEvery { reservasApi.eliminarReserva(4) } returns Unit
        repo.eliminarReserva(4)
        coVerify(exactly = 1) { reservasApi.eliminarReserva(4) }
    }

    @Test
    fun reservas_sinEmailListaTodas() = runTest {
        coEvery { reservasApi.listarReservas(null) } returns listOf(reservaDto(1), reservaDto(2))
        assertEquals(2, repo.reservas().size)
    }

    // ---- Comprobantes ----
    @Test
    fun subirComprobante_enviaReservaYMonto() = runTest {
        val parte = MultipartBody.Part.createFormData(
            "archivo", "c.jpg", byteArrayOf(1, 2).toRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        coEvery { comprobantesApi.subirComprobante(any(), any(), any()) } returns
            ComprobanteDto(1, 9, "c.jpg", 48000, "RECIBIDO")
        val dto = repo.subirComprobante(9, 48000, parte)
        assertEquals(9L, dto.reservaId)
        assertEquals("RECIBIDO", dto.estado)
    }

    @Test
    fun comprobantes_listaPorReserva() = runTest {
        coEvery { comprobantesApi.listarPorReserva(9) } returns
            listOf(ComprobanteDto(1, 9, "c.jpg", 48000, "RECIBIDO"))
        assertEquals(1, repo.comprobantes(9).size)
    }

    // ---- API externa ----
    @Test
    fun indicadores_resumeUfYDolar() = runTest {
        coEvery { indicadoresApi.indicadores() } returns IndicadoresDto(
            uf = IndicadorDto("uf", "UF", "Pesos", "2026-11-23T03:00:00.000Z", 39500.5),
            dolar = IndicadorDto("dolar", "Dólar", "Pesos", "2026-11-23T03:00:00.000Z", 950.25),
            euro = null, utm = null
        )
        val resultado = repo.indicadores()
        assertTrue(resultado.isSuccess)
        assertEquals(39500.5, resultado.getOrNull()?.uf ?: 0.0, 0.001)
        assertEquals(950.25, resultado.getOrNull()?.dolar ?: 0.0, 0.001)
        assertEquals("2026-11-23", resultado.getOrNull()?.fecha)
    }

    @Test
    fun indicadores_siFallaLaApiRetornaFailureSinLanzar() = runTest {
        coEvery { indicadoresApi.indicadores() } throws RuntimeException("sin internet")
        val resultado = repo.indicadores()
        assertTrue(resultado.isFailure)
        assertNotNull(resultado.exceptionOrNull())
    }
}
