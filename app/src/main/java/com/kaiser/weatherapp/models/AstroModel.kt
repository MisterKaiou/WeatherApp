package com.kaiser.weatherapp.models

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AstroModel(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    @SerialName("moon_phase") val moonPhase: String = "",
    @SerialName("moon_illumination") val moonIllumination: Double = 0.0
) {

}
