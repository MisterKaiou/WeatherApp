package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DayModel(
    @SerialName("maxtemp_c") val maxTempCelsius: Double,
    @SerialName("maxtemp_f") val maxTempFahrenheit: Double,
    @SerialName("mintemp_c") val minTempCelsius: Double,
    @SerialName("mintemp_f") val minTempFahrenheit: Double,
    @SerialName("avgtemp_c") val avgTempCelsius: Double,
    @SerialName("avgtemp_f") val avgTempFahrenheit: Double,
    @SerialName("maxwind_mph") val maxWindMph: Double,
    @SerialName("maxwind_kph") val maxWindKph: Double,
    @SerialName("totalprecip_mm") val totalPrecipitationMm: Double,
    @SerialName("totalprecip_in") val totalPrecipitationIn: Double,
    @SerialName("avgvis_km") val avgVisibilityKm: Double,
    @SerialName("avgvis_miles") val avgVisibilityMiles: Double,
    @SerialName("avghumidity") val avgHumidity: Double,
    @SerialName("daily_will_it_rain") val dailyWillItRain: Int = 0,
    @SerialName("daily_chance_of_rain") val dailyChanceOfRain: Int = 0,
    @SerialName("daily_will_it_snow") val dailyWillItSnow: Int = 0,
    @SerialName("daily_chance_of_snow") val dailyChanceOfSnow: Int = 0,
    val condition: ConditionModel,
    val uv: Double
)
