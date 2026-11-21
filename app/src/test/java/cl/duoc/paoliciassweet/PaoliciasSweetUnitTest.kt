package cl.duoc.paoliciassweet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaoliciasSweetUnitTest {

    // --- 1. Pruebas de Cotización ---

    private fun calcularTotalCotizacion(
        precioBase: Int,
        cantidad: Int,
        recargoDiseno: Int = 0
    ): Int {
        require(precioBase > 0) { "El precio base debe ser mayor a 0" }
        require(cantidad > 0) { "La cantidad debe ser al menos 1" }
        require(recargoDiseno >= 0) { "El recargo no puede ser negativo" }
        return (precioBase * cantidad) + recargoDiseno
    }

    @Test
    fun calculoDeCotizacionMultiplicaPrecioPorCantidadCorrectamente() {
        val total = calcularTotalCotizacion(precioBase = 25000, cantidad = 2)
        assertEquals(50000, total)
    }

    @Test
    fun calculoDeCotizacionSumaRecargoPorDisenoPersonalizado() {
        val total = calcularTotalCotizacion(
            precioBase = 28000,
            cantidad = 1,
            recargoDiseno = 5000
        )
        assertEquals(33000, total)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cotizacionConCantidadCeroLanzaExcepcion() {
        calcularTotalCotizacion(precioBase = 20000, cantidad = 0)
    }

    // --- 2. Pruebas de Validación de Reserva ---

    private fun esReservaValida(
        nombreCliente: String,
        emailCliente: String,
        fechaEntrega: String,
        cantidad: Int
    ): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return nombreCliente.isNotBlank() &&
                emailCliente.matches(emailRegex) &&
                fechaEntrega.isNotBlank() &&
                cantidad > 0
    }

    @Test
    fun validacionDeReservaAceptaDatosCompletosYCorreoValido() {
        val valida = esReservaValida(
            nombreCliente = "Camila Gonzalez",
            emailCliente = "camila@duocuc.cl",
            fechaEntrega = "2026-11-28",
            cantidad = 2
        )
        assertTrue(valida)
    }

    @Test
    fun validacionDeReservaRechazaCorreoInvalidoOCamposVacios() {
        val correoSinArroba = esReservaValida(
            nombreCliente = "Camila Gonzalez",
            emailCliente = "camiladuocuc.cl",
            fechaEntrega = "2026-11-28",
            cantidad = 1
        )
        val nombreVacio = esReservaValida(
            nombreCliente = "   ",
            emailCliente = "camila@duocuc.cl",
            fechaEntrega = "2026-11-28",
            cantidad = 1
        )
        assertFalse(correoSinArroba)
        assertFalse(nombreVacio)
    }

    // --- 3. Pruebas de Flujo de Estados (Cliente / Administrador) ---

    private fun puedeCambiarEstado(estadoActual: String, nuevoEstado: String): Boolean {
        return when (estadoActual.uppercase()) {
            "PENDIENTE" -> nuevoEstado.uppercase() in listOf("APROBADA", "RECHAZADA")
            "APROBADA" -> nuevoEstado.uppercase() == "ENTREGADA"
            else -> false
        }
    }

    @Test
    fun flujoDeEstadosPermiteAprobarORechazarDesdePendiente() {
        assertTrue(puedeCambiarEstado("PENDIENTE", "APROBADA"))
        assertTrue(puedeCambiarEstado("PENDIENTE", "RECHAZADA"))
        assertTrue(puedeCambiarEstado("APROBADA", "ENTREGADA"))
    }

    @Test
    fun flujoDeEstadosImpideEntregarReservaSinAprobacionPrevia() {
        assertFalse(puedeCambiarEstado("PENDIENTE", "ENTREGADA"))
        assertFalse(puedeCambiarEstado("RECHAZADA", "ENTREGADA"))
        }

    // --- 4. Pruebas de Filtrado de Catálogo ---

    private fun filtrarProductos(productos: List<String>, consulta: String): List<String> {
        if (consulta.isBlank()) return productos
        return productos.filter { it.contains(consulta.trim(), ignoreCase = true) }
    }

    @Test
    fun filtradoDeCatalogoEncuentraCoincidenciasSinImportarMayusculas() {
        val menu = listOf(
            "Torta Tres Leches",
            "Torta Selva Negra",
            "Kuchen de Frambuesa",
            "Pie de Limón"
        )
        val resultado = filtrarProductos(menu, "torta")
        assertEquals(2, resultado.size)
        assertTrue(resultado.contains("Torta Tres Leches"))
    }
    }