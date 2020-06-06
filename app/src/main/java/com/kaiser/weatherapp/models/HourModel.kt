package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HourModel(
    @SerialName("time_epoch") val timeEpoch: Long,
    val time: String,
    @SerialName("temp_c") val tempCelsius: Double,
    @SerialName("temp_f") val tempFahrenheit: Double,
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
    @SerialName("feelslike_c") val feelsLikeCelsius: Double,
    @SerialName("feelslike_f") val feelsLikeFahrenheit: Double,
    @SerialName("windchill_c") val windChillCelsius: Double,
    @SerialName("windchill_f") val windChillFahrenheit: Double,
    @SerialName("heatindex_c") val heatIndexCelsius: Double,
    @SerialName("heatindex_f") val heatIndexFahrenheit: Double,
    @SerialName("dewpoint_c") val dewPointCelsius: Double,
    @SerialName("dewpoint_f") val dewPointFahrenheit: Double,
    @SerialName("will_it_rain") val willItRain: Int,
    @SerialName("will_it_snow") val willItSnow: Int,
    @SerialName("is_day") val isDay: Int,
    @SerialName("vis_km") val visibilityKm: Double,
    @SerialName("vis_miles") val visibilityMiles: Double,
    @SerialName("chance_of_rain") val chanceOfRain: Int,
    @SerialName("chance_of_snow") val chanceOfSnow: Int,
    @SerialName("gust_mph") val gustMph: Double,
    @SerialName("gust_kph") val gustKph: Double
)
