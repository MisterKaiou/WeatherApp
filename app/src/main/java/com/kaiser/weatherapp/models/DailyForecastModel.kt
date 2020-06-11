package com.kaiser.weatherapp.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyForecastModel(
    val location: LocationModel,
    val current: CurrentDayModel,
    val forecast: ForecastModel
)
