package cl.duoc.paoliciassweet.network

import cl.duoc.paoliciassweet.data.Producto
import cl.duoc.paoliciassweet.data.Reserva

private const val PREFIJO_BLOQUE = "Bloque:"

fun ProductoDto.toEntity(): Producto = Producto(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria,
    precioPorPersona = precioPorPersona
)

/** Las notas del backend guardan "Bloque: X | observaciones"; aquí se separan. */
fun ReservaDto.toEntity(usuarioId: Long): Reserva {
    val partes = (notas ?: "").split("|").map { it.trim() }
    val bloque = partes.firstOrNull { it.startsWith(PREFIJO_BLOQUE) }
        ?.removePrefix(PREFIJO_BLOQUE)
        ?.trim()
        ?: "Por definir"
    val observaciones = partes
        .filterNot { it.startsWith(PREFIJO_BLOQUE) }
        .filter { it.isNotEmpty() }
        .joinToString(" | ")

    return Reserva(
        id = id,
        usuarioId = usuarioId,
        productoId = productoId,
        productoNombre = productoNombre,
        fechaEvento = FechaMapper.isoStringToLong(fechaEvento),
        bloque = bloque,
        asistentes = asistentes,
        observaciones = observaciones,
        total = total,
        abono = abono,
        estado = estado,
        latitud = null,
        longitud = null,
        creadaEn = System.currentTimeMillis()
    )
}

fun reservaNotas(bloque: String, observaciones: String): String = buildString {
    append("$PREFIJO_BLOQUE $bloque")
    if (observaciones.isNotBlank()) append(" | ${observaciones.trim()}")
}
