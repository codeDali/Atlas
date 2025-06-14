package com.example.atlas.api

data class WeatherResponse(
    val current: Current
)

data class Current(
    val condition: Condition
)

data class Condition(
    val text: String
) 