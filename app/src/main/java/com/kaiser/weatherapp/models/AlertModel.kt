package com.kaiser.weatherapp.models

import kotlinx.serialization.Serializable

@Serializable
data class AlertModel(
    val alert : String =  ""
)
