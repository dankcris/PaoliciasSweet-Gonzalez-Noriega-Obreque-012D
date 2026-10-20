package cl.duoc.paoliciassweet.data

import kotlinx.coroutines.flow.MutableStateFlow

object Sesion {
    val usuarioId = MutableStateFlow<Long?>(null)
    val rol = MutableStateFlow<String?>(null)

    val esAdmin: Boolean get() = rol.value == Roles.ADMIN

    fun iniciar(id: Long, rolUsuario: String) {
        rol.value = rolUsuario
        usuarioId.value = id
    }

    fun cerrar() {
        usuarioId.value = null
        rol.value = null
    }
}
