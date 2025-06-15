package com.example.atlas.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R
import android.telephony.SmsManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class FallConfirmationActivity : AppCompatActivity() {
    private lateinit var tvCountdown: TextView
    private lateinit var btnFalseFall: Button
    private lateinit var btnTrueFall: Button
    private var countDownTimer: CountDownTimer? = null
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_confirmation)
        tvCountdown = findViewById(R.id.tv_countdown)
        btnFalseFall = findViewById(R.id.false_fall_button)
        btnTrueFall = findViewById(R.id.true_fall_button)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        startCountdown()

        btnFalseFall.setOnClickListener {
            countDownTimer?.cancel()
            navigateToResult()
        }

        btnTrueFall.setOnClickListener {
            countDownTimer?.cancel()
            sendEmergencyMessages()
            navigateToSOS()
        }
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                tvCountdown.text = secondsRemaining.toString()
            }

            override fun onFinish() {
                sendEmergencyMessages()
                navigateToSOS()
            }
        }.start()
    }

    private fun sendEmergencyMessages() {
        val sharedPrefs = getSharedPreferences("EmergencyContacts", MODE_PRIVATE)
        val contact1 = sharedPrefs.getString("contact1", null)
        val contact2 = sharedPrefs.getString("contact2", null)

        val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val location = getCurrentLocation()
        
        val locationInfo = if (location != null) {
            "Lokasi: ${location.latitude}, ${location.longitude} (ketinggian: ${location.altitude} meter)"
        } else {
            "Lokasi tidak tersedia"
        }
        
        val message = "Pendaki ini telah mendaftarkan anda ke kontak darurat, " +
                     "pendaki kemungkinan terjatuh di gunung Sibayak pada titik $locationInfo " +
                     "pada $currentTime.  *pesan ini dikirim secara otomatis*"

        contact1?.let { phoneNumber ->
            Log.d("SMS_DEBUG", "Mencoba mengirim SMS ke: $phoneNumber")
            try {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(this, "Pesan darurat terkirim ke $phoneNumber", Toast.LENGTH_LONG).show()
                Log.i("SMS_SUCCESS", "SMS pesan $message berhasil dikirim ke $phoneNumber pada pukul $currentTime")
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal mengirim SMS: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("SMS_ERROR", "Gagal mengirim SMS ke $phoneNumber: ${e.message}")
                e.printStackTrace()
            }
        }

        contact2?.let { phoneNumber ->
            try {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getCurrentLocation(): Location? {
        try {
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                return lastKnownLocation
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return null
    }

    private fun navigateToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToSOS() {
        val intent = Intent(this, SosActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}