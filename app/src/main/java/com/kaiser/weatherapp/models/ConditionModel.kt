package com.kaiser.weatherapp.models

import kotlinx.serialization.Serializable

@Serializable
data class ConditionModel(
    val text: String,
    val icon: String,
    val code: Int
) {

}
