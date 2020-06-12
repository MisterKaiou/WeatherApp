package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentForecastModel(
    val location: LocationModel,
    @SerialName("current") val currentDay: CurrentDayModel,
    val forecast: ForecastModel
)
