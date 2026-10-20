package cl.duoc.paoliciassweet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de formulario reutilizable: ícono inicial, ícono de estado (válido / error)
 * y mensaje de error animado. No contiene lógica de validación: la recibe ya resuelta.
 */
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    esPassword: Boolean = false,
    lineas: Int = 1,
    opcional: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    var ultimoError by remember { mutableStateOf("") }
    if (error != null) ultimoError = error

    Column(modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            label = { Text(if (opcional) "$etiqueta (opcional)" else etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                when {
                    esPassword -> IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar u ocultar contraseña"
                        )
                    }
                    error != null -> Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    valor.isNotBlank() -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Campo válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    else -> {}
                }
            },
            isError = error != null,
            singleLine = lineas == 1,
            minLines = lineas,
            visualTransformation = if (esPassword && !visible) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esPassword) KeyboardType.Password else teclado
            ),
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(visible = error != null) {
            Text(
                text = ultimoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
