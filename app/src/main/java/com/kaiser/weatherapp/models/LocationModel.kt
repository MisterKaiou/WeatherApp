package com.kaiser.weatherapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationModel(
    val name: String,
    val region: String,
    val country: String,
    @SerialName("lat") val latitude: Double,
    @SerialName("lon") val longitude: Double,
    @SerialName("tz_id") val timeZone: String,
    @SerialName("localtime_epoch") val localTimeEpoch: Long,
    @SerialName("localtime") val localTime: String
)
