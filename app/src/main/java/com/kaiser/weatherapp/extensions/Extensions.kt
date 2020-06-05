package com.kaiser.weatherapp.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.formatToDate(format: String = "dd/MM/yyyy hh:mm a", locale: Locale = Locale.ENGLISH): String? {
    return SimpleDateFormat(format, locale).format(
        Date(this * 1000)
    )
}