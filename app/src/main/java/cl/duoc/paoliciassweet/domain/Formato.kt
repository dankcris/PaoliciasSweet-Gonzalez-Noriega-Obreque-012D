package cl.duoc.paoliciassweet.domain

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val formatoMoneda: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))

fun moneda(valor: Int): String = formatoMoneda.format(valor.toLong())

object Fechas {
    /** Hoy a las 00:00 UTC (el DatePicker de Material trabaja en UTC). */
    fun hoyUtc(): Long {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        return utc.timeInMillis
    }

    fun formatear(utcMillis: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return formato.format(Date(utcMillis))
    }
}
