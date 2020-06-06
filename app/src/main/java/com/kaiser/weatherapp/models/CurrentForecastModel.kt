package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonInput

@Serializable
data class CurrentForecastModel(
    val location: LocationModel,
    @SerialName("current") val currentDay: CurrentDayModel,
    val forecast: ForecastModel
    //val alert: JsonInput? = null
)
