package cl.duoc.paoliciassweet.domain

val BLOQUES = listOf(
    "Mañana (09:00 - 13:00)",
    "Tarde (14:00 - 18:00)",
    "Noche (19:00 - 23:00)"
)

val FILTROS_CATALOGO = listOf("Todas", "Tortas", "Banquetería")

object Validaciones {
    private val regexEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val regexTelefono = Regex("^(\\+?56)?9\\d{8}$")
    private val regexNombre = Regex("^[\\p{L} .'-]+$")

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Mínimo 3 caracteres"
        valor.length > 50 -> "Máximo 50 caracteres"
        !regexNombre.matches(valor.trim()) -> "Solo letras y espacios"
        else -> null
    }

    fun email(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !regexEmail.matches(valor.trim()) -> "Ingresa un correo válido"
        else -> null
    }

    fun telefono(valor: String): String? {
        val limpio = valor.replace(" ", "").replace("-", "")
        return when {
            limpio.isBlank() -> "El teléfono es obligatorio"
            !regexTelefono.matches(limpio) -> "Ingresa un celular válido (ej: 912345678)"
            else -> null
        }
    }

    // La empresa es opcional
    fun empresa(valor: String): String? =
        if (valor.length > 40) "Máximo 40 caracteres" else null

    fun password(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 6 -> "Mínimo 6 caracteres"
        valor.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun confirmar(password: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contraseña"
        password != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun fecha(utcMillis: Long?): String? = when {
        utcMillis == null -> "Selecciona la fecha del evento"
        utcMillis < Fechas.hoyUtc() -> "La fecha no puede ser anterior a hoy"
        else -> null
    }

    fun bloque(valor: String): String? =
        if (valor.isBlank()) "Selecciona un horario" else null

    fun asistentes(valor: String): String? {
        if (valor.isBlank()) return "Ingresa la cantidad de asistentes"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            numero < Cotizador.MIN_ASISTENTES -> "Mínimo ${Cotizador.MIN_ASISTENTES} asistentes"
            numero > Cotizador.MAX_ASISTENTES -> "Máximo ${Cotizador.MAX_ASISTENTES} asistentes"
            else -> null
        }
    }

    // Opcional: restricciones alimentarias, alergias, requerimientos de espacio
    fun observaciones(valor: String): String? =
        if (valor.length > 200) "Máximo 200 caracteres" else null
}
