package cl.duoc.paoliciassweet.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.paoliciassweet.ui.components.CampoTexto
import cl.duoc.paoliciassweet.viewmodel.AuthViewModel

@Composable
fun AuthScreen(onAutenticado: () -> Unit, vm: AuthViewModel = viewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(s.autenticado) {
        if (s.autenticado) onAutenticado()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Cake,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text("Paolicias.Sweet", style = MaterialTheme.typography.headlineMedium)
            AnimatedContent(targetState = s.esRegistro, label = "titulo") { registro ->
                Text(
                    if (registro) "Crea tu cuenta" else "Inicia sesión",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(visible = s.esRegistro) {
                Column {
                    CampoTexto(
                        valor = s.nombre,
                        onCambio = vm::onNombre,
                        etiqueta = "Nombre completo",
                        icono = Icons.Default.Person,
                        error = s.nombreError
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            CampoTexto(
                valor = s.email,
                onCambio = vm::onEmail,
                etiqueta = "Correo",
                icono = Icons.Default.Email,
                error = s.emailError,
                teclado = KeyboardType.Email
            )
            AnimatedVisibility(visible = s.esRegistro) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = s.telefono,
                        onCambio = vm::onTelefono,
                        etiqueta = "Teléfono",
                        icono = Icons.Default.Phone,
                        error = s.telefonoError,
                        teclado = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = s.empresa,
                        onCambio = vm::onEmpresa,
                        etiqueta = "Empresa",
                        icono = Icons.Default.Business,
                        error = s.empresaError,
                        opcional = true
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            CampoTexto(
                valor = s.password,
                onCambio = vm::onPassword,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                error = s.passwordError,
                esPassword = true
            )
            AnimatedVisibility(visible = s.esRegistro) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = s.confirmar,
                        onCambio = vm::onConfirmar,
                        etiqueta = "Confirmar contraseña",
                        icono = Icons.Default.Lock,
                        error = s.confirmarError,
                        esPassword = true
                    )
                }
            }

            AnimatedVisibility(visible = s.mensaje != null) {
                Text(
                    text = s.mensaje ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = vm::enviar,
                enabled = !s.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (s.cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (s.esRegistro) "Registrarme" else "Entrar")
                }
            }
            TextButton(onClick = vm::alternarModo) {
                Text(if (s.esRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
            }
        }
    }
}
