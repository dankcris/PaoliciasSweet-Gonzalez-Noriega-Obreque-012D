package cl.duoc.paoliciassweet.data

import java.security.MessageDigest

object Roles {
    const val ADMIN = "ADMIN"
    const val USER = "USER"

    // Cuenta de la dueña (datos sintéticos, solo para uso académico)
    const val ADMIN_EMAIL = "admin@paoliciassweet.cl"
    const val ADMIN_PASSWORD = "Admin123"
}

fun hashPassword(texto: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }
