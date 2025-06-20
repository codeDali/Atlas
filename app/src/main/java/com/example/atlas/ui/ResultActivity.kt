package com.example.atlas.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val TAG = "ResultActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("PredictionPrefs", Context.MODE_PRIVATE)

        val prediction = intent.getStringExtra("PREDICTION") ?: sharedPreferences.getString("last_prediction", "Silahkan kembali ke halaman input")
        binding.predictionText.text = prediction

        if (prediction == "Jalur 54") {
            binding.predictionDescription.text = "Visualisasi peta khusus jalur ini adalah estimasi, bukan visualisasi resmi, sebaiknya ikuti rombongan atau gunakan pemandu lokal"
        }


        sharedPreferences.edit().putString("last_prediction", prediction).apply()

        binding.toMapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.testBtn.setOnClickListener{
            val intent = Intent(this, FallConfirmationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }


    }

    companion object {
        fun start(context: Context, prediction: String) {
            val intent = Intent(context, ResultActivity::class.java).apply {
                putExtra("PREDICTION", prediction)
            }
            context.startActivity(intent)
        }
    }
}