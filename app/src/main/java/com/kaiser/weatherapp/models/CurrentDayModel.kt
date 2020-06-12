package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentDayModel(
    @SerialName("last_updated") val lastUpdated: String,
    @SerialName("last_updated_epoch") val lastUpdatedEpoch: Long,
    @SerialName("temp_c") val tempCelsius: Double,
    @SerialName("temp_f") val tempFahrenheit: Double,
    @SerialName("feelslike_c") val feelsLikeCelsius: Double,
    @SerialName("feelslike_f") val feelsLikeFahrenheit: Double,
    @SerialName("is_day") val isDay: Int,
    val condition: ConditionModel,
    @SerialName("wind_mph") val windMph: Double,
    @SerialName("wind_kph") val windKph: Double,
    @SerialName("wind_degree") val windDegree: Int,
    @SerialName("wind_dir") val windDir: String,
    @SerialName("pressure_mb") val pressureMillibars: Double,
    @SerialName("pressure_in") val pressureInches: Double,
    @SerialName("precip_mm") val precipMilim: Double,
    @SerialName("precip_in") val precipInches: Double,
    val humidity: Int,
    val cloud: Int,
    @SerialName("vis_km") val visibilityKm: Double,
    @SerialName("vis_miles") val visibilityMiles: Double,
    @SerialName("gust_mph") val gustMph: Double,
    @SerialName("gust_kph") val gustKph: Double,
    val uv: Double
)
