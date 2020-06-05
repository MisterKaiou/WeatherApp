package com.kaiser.weatherapp.models

import kotlinx.serialization.Serializable

@Serializable
data class HistoryModel(
    val location: LocationModel,
    val forecast: ForecastModel
) {
}