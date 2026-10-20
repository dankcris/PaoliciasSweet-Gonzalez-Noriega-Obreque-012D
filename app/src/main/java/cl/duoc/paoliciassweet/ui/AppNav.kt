package cl.duoc.paoliciassweet.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.paoliciassweet.data.Roles
import cl.duoc.paoliciassweet.data.Sesion

object Rutas {
    const val AUTH = "auth"
    const val CATALOGO = "catalogo"
    const val COTIZAR = "cotizar/{productoId}"
    const val RESERVAS = "reservas"
    const val COMPROBANTE = "comprobante/{reservaId}"
    const val ADMIN = "admin"

    fun cotizar(productoId: Long) = "cotizar/$productoId"
    fun comprobante(reservaId: Long) = "comprobante/$reservaId"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Rutas.AUTH,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 5 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 5 } }
    ) {
        composable(Rutas.AUTH) {
            AuthScreen(onAutenticado = {
                val destino = if (Sesion.esAdmin) Rutas.ADMIN else Rutas.CATALOGO
                nav.navigate(destino) { popUpTo(Rutas.AUTH) { inclusive = true } }
            })
        }
        composable(Rutas.CATALOGO) {
            SoloAutenticado(nav) {
                CatalogoScreen(
                    onCotizar = { nav.navigate(Rutas.cotizar(it)) },
                    onReservas = { nav.navigate(Rutas.RESERVAS) },
                    onSalir = { Sesion.cerrar() }
                )
            }
        }
        composable(
            route = Rutas.COTIZAR,
            arguments = listOf(navArgument("productoId") { type = NavType.LongType })
        ) { entrada ->
            val productoId = entrada.arguments?.getLong("productoId") ?: 0L
            SoloAutenticado(nav) {
                CotizarScreen(
                    productoId = productoId,
                    onVolver = { nav.popBackStack() },
                    onReservado = {
                        nav.navigate(Rutas.RESERVAS) { popUpTo(Rutas.CATALOGO) }
                    }
                )
            }
        }
        composable(Rutas.RESERVAS) {
            SoloAutenticado(nav) {
                MisReservasScreen(
                    onVolver = { nav.popBackStack() },
                    onComprobante = { nav.navigate(Rutas.comprobante(it)) }
                )
            }
        }
        composable(
            route = Rutas.COMPROBANTE,
            arguments = listOf(navArgument("reservaId") { type = NavType.LongType })
        ) { entrada ->
            val reservaId = entrada.arguments?.getLong("reservaId") ?: 0L
            SoloAutenticado(nav) {
                ComprobanteScreen(
                    reservaId = reservaId,
                    onVolver = { nav.popBackStack() }
                )
            }
        }
        composable(Rutas.ADMIN) {
            val usuarioId by Sesion.usuarioId.collectAsState()
            val rol by Sesion.rol.collectAsState()
            LaunchedEffect(usuarioId, rol) {
                if (usuarioId == null || rol != Roles.ADMIN) {
                    val destino = if (usuarioId == null) Rutas.AUTH else Rutas.CATALOGO
                    nav.navigate(destino) { popUpTo(Rutas.ADMIN) { inclusive = true } }
                }
            }
            if (rol == Roles.ADMIN) {
                AdminScreen(onSalir = { Sesion.cerrar() })
            }
        }
    }
}

/** Protege las rutas del cliente: si se cierra la sesión vuelve al login. */
@Composable
private fun SoloAutenticado(nav: NavController, contenido: @Composable () -> Unit) {
    val usuarioId by Sesion.usuarioId.collectAsState()
    LaunchedEffect(usuarioId) {
        if (usuarioId == null) {
            nav.navigate(Rutas.AUTH) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }
    if (usuarioId != null) contenido()
}
