package cl.duoc.paoliciassweet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.paoliciassweet.data.AppDatabase
import cl.duoc.paoliciassweet.data.Repositorio
import cl.duoc.paoliciassweet.data.Roles
import cl.duoc.paoliciassweet.data.Sesion
import cl.duoc.paoliciassweet.data.Usuario
import cl.duoc.paoliciassweet.domain.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val empresa: String = "",
    val password: String = "",
    val confirmar: String = "",
    val esRegistro: Boolean = false,
    val nombreError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null,
    val empresaError: String? = null,
    val passwordError: String? = null,
    val confirmarError: String? = null,
    val mensaje: String? = null,
    val cargando: Boolean = false,
    val autenticado: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(AppDatabase.get(app))
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onNombre(valor: String) = _state.update {
        it.copy(nombre = valor, nombreError = Validaciones.nombre(valor), mensaje = null)
    }

    fun onEmail(valor: String) = _state.update {
        it.copy(email = valor, emailError = Validaciones.email(valor), mensaje = null)
    }

    fun onTelefono(valor: String) = _state.update {
        it.copy(telefono = valor, telefonoError = Validaciones.telefono(valor), mensaje = null)
    }

    fun onEmpresa(valor: String) = _state.update {
        it.copy(empresa = valor, empresaError = Validaciones.empresa(valor), mensaje = null)
    }

    fun onPassword(valor: String) = _state.update {
        it.copy(
            password = valor,
            passwordError = Validaciones.password(valor),
            confirmarError = if (it.esRegistro && it.confirmar.isNotEmpty())
                Validaciones.confirmar(valor, it.confirmar) else it.confirmarError,
            mensaje = null
        )
    }

    fun onConfirmar(valor: String) = _state.update {
        it.copy(confirmar = valor, confirmarError = Validaciones.confirmar(it.password, valor), mensaje = null)
    }

    fun alternarModo() = _state.update {
        AuthUiState(email = it.email, esRegistro = !it.esRegistro)
    }

    fun enviar() {
        val s = _state.value
        val errorNombre = if (s.esRegistro) Validaciones.nombre(s.nombre) else null
        val errorEmail = Validaciones.email(s.email)
        val errorTelefono = if (s.esRegistro) Validaciones.telefono(s.telefono) else null
        val errorEmpresa = if (s.esRegistro) Validaciones.empresa(s.empresa) else null
        val errorPassword = Validaciones.password(s.password)
        val errorConfirmar = if (s.esRegistro) Validaciones.confirmar(s.password, s.confirmar) else null
        _state.update {
            it.copy(
                nombreError = errorNombre,
                emailError = errorEmail,
                telefonoError = errorTelefono,
                empresaError = errorEmpresa,
                passwordError = errorPassword,
                confirmarError = errorConfirmar
            )
        }
        val hayErrores = listOf(
            errorNombre, errorEmail, errorTelefono, errorEmpresa, errorPassword, errorConfirmar
        ).any { it != null }
        if (hayErrores) return

        viewModelScope.launch {
            _state.update { it.copy(cargando = true, mensaje = null) }
            val usuario: Usuario? = if (s.esRegistro) {
                repo.registrar(s.nombre, s.email, s.telefono, s.empresa, s.password)?.let { id ->
                    Usuario(
                        id = id, nombre = s.nombre, email = s.email, telefono = s.telefono,
                        empresa = s.empresa, passwordHash = "", rol = Roles.USER
                    )
                }
            } else {
                repo.login(s.email, s.password)
            }
            if (usuario == null) {
                val texto = if (s.esRegistro) "Ese correo ya está registrado"
                else "Correo o contraseña incorrectos"
                _state.update { it.copy(cargando = false, mensaje = texto) }
            } else {
                Sesion.iniciar(usuario.id, usuario.rol)
                _state.update { it.copy(cargando = false, autenticado = true) }
            }
        }
    }
}
