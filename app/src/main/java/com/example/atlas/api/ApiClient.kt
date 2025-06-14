package com.example.atlas.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object ApiClient {
    private const val TAG = "ApiClient"
    private const val API_KEY = "0d0e27fa5c054bef9ad70929251006"
    private const val SIBAYAK_LOCATION = "3.2347,98.5123" // Koordinat Gunung Sibayak

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(WeatherApi.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherService: WeatherApi = retrofit.create(WeatherApi::class.java)

    suspend fun fetchSibayakWeather(): WeatherResponse {
        try {
            Log.d(TAG, "Fetching weather data for Sibayak...")
            val response = weatherService.getCurrentWeather(
                apiKey = API_KEY,
                location = SIBAYAK_LOCATION
            )
            Log.d(TAG, "Weather data received: ${response.current.condition.text}")
            return response
        } catch (e: IOException) {
            Log.e(TAG, "Network error while fetching weather data", e)
            throw WeatherException("Gagal terhubung ke server cuaca. Periksa koneksi internet Anda.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error while fetching weather data", e)
            throw WeatherException("Terjadi kesalahan saat mengambil data cuaca: ${e.message}", e)
        }
    }
}

class WeatherException(message: String, cause: Throwable? = null) : Exception(message, cause) 