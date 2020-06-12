package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDayModel(
    val date: String,
    @SerialName("date_epoch") val dateEpoch: Long,
    val day: DayModel,
    val astro: AstroModel,
    val hour: MutableList<HourModel> = mutableListOf()
)
