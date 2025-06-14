package com.example.atlas.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String = "no"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.weatherapi.com/"
    }
} 