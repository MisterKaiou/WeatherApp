package com.kaiser.weatherapp.extensions

import java.text.SimpleDateFormat
import java.util.*

/**
 * Formats this Long to date
 * @param format Default is "dd/MM/yyyy hh:mm a"
 * @param locale Default is the current application locale
 */
fun Long.formatToDate(format: String = "dd/MM/yyyy hh:mm a", locale: Locale = Locale.getDefault()): String? {
    return SimpleDateFormat(format, locale).format(
        Date(this * 1000)
    )
}
