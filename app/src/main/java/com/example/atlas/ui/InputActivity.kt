package com.example.atlas.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.atlas.R
import com.example.atlas.api.ApiClient
import com.example.atlas.ml.RoutePredictor
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.launch

class InputActivity : AppCompatActivity() {
    private val TAG = "InputActivity"
    private lateinit var physicalConditionDropdown: AutoCompleteTextView
    private lateinit var experienceDropdown: AutoCompleteTextView
    private lateinit var weatherLoading: ProgressBar
    private lateinit var weatherStatus: TextView
    private lateinit var predictButton: Button
    private lateinit var refreshWeatherButton: Button
    private lateinit var routePredictor: RoutePredictor
    private var weatherValue: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)
        physicalConditionDropdown = findViewById(R.id.physicalConditionDropdown)
        experienceDropdown = findViewById(R.id.experienceDropdown)
        weatherLoading = findViewById(R.id.weatherLoading)
        weatherStatus = findViewById(R.id.weatherStatus)
        predictButton = findViewById(R.id.predictButton)
        refreshWeatherButton = findViewById(R.id.refreshWeatherButton)

        routePredictor = RoutePredictor(this)
        Log.d(TAG, "RoutePredictor initialized")

        setupDropdowns()
        setupWeatherData()

        refreshWeatherButton.setOnClickListener {
            setupWeatherData()
        }

        predictButton.setOnClickListener {
            if (validateInput()) {
                val physicalCondition = getPhysicalConditionValue()
                val experience = getExperienceValue()

                Log.d(TAG, "Starting route prediction with values: physical=$physicalCondition, experience=$experience, weather=$weatherValue")
                val recommendedRoute = routePredictor.predictRoute(
                    physicalCondition,
                    experience,
                    weatherValue
                )
                Log.d(TAG, "Route prediction completed: $recommendedRoute")
                ResultActivity.start(this, recommendedRoute)
            }
        }
    }

    private fun setupDropdowns() {
        val physicalConditions = arrayOf("Segar bugar, semua medan bisa dilaluin", "Cukup fit, kondisi biasa aja, ga segar bugar, tapi cukup semangat lah", "Kurang fit, baru sembuh sakit, kecapean di perjalanan")
        val physicalAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, physicalConditions)
        physicalConditionDropdown.setAdapter(physicalAdapter)

        val experiences = arrayOf("Ini pengalaman pertama naik gunung", "Udah 1-3 kali naik gunung", "Udah 4+ kali naik gunung")
        val experienceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, experiences)
        experienceDropdown.setAdapter(experienceAdapter)
    }

    private fun setupWeatherData() {
        weatherLoading.visibility = ProgressBar.VISIBLE
        weatherStatus.text = "Mengambil data cuaca..."
        refreshWeatherButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val weatherResponse = ApiClient.fetchSibayakWeather()
                weatherValue = when (weatherResponse.current.condition.text.lowercase()) {
                    "sunny", "clear", "partly cloudy" -> 1
                    "rain", "light rain", "moderate rain", "heavy rain" -> 2
                    else -> {
                        weatherStatus.text = "Kondisi cuaca : ${weatherResponse.current.condition.text}"
                        return@launch
                    }
                }
                weatherStatus.text = "Kondisi cuaca: ${weatherResponse.current.condition.text}"
            } catch (e: Exception) {
                weatherStatus.text = "Gagal mendapatkan data cuaca"
                Toast.makeText(this@InputActivity, "Gagal mengambil data cuaca. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            } finally {
                weatherLoading.visibility = ProgressBar.GONE
                refreshWeatherButton.isEnabled = true
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (physicalConditionDropdown.text.isNullOrEmpty()) {
            physicalConditionDropdown.error = "Pilih kondisi fisik"
            isValid = false
        }

        if (experienceDropdown.text.isNullOrEmpty()) {
            experienceDropdown.error = "Pilih pengalaman mendaki"
            isValid = false
        }

        return isValid
    }

    private fun getPhysicalConditionValue(): Int {
        return when (physicalConditionDropdown.text.toString()) {
            "Segar bugar, semua medan bisa dilaluin" -> 3
            "Cukup fit, kondisi biasa aja, ga segar bugar, tapi cukup semangat lah" -> 2
            "Kurang fit, baru sembuh sakit, kecapean di perjalanan" -> 1
            else -> 1
        }
    }

    private fun getExperienceValue(): Int {
        return when (experienceDropdown.text.toString()) {
            "Udah 4+ kali naik gunung" -> 3
            "Udah 1-3 kali naik gunung" -> 2
            "Ini pengalaman pertama naik gunung" -> 1
            else -> 1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routePredictor.close()
        Log.d(TAG, "RoutePredictor closed")
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, InputActivity::class.java)
            context.startActivity(intent)
        }
    }
}