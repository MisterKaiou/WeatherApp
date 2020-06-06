package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastModel(
    @SerialName("forecastday") val forecastDay: List<ForecastDayModel>
)
