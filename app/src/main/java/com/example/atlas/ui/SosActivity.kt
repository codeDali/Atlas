package com.example.atlas.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R
import java.text.SimpleDateFormat
import java.util.*

class SosActivity : AppCompatActivity() {
    private lateinit var tvCountdownSosTime: TextView
    private lateinit var btnHelpPendaki: Button
    private var countUpTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        // Initialize views
        tvCountdownSosTime = findViewById(R.id.tv_countdown_sos_time)
        btnHelpPendaki = findViewById(R.id.btn_help_pendaki)

        // Start count up timer
        startTime = System.currentTimeMillis()
        startCountUpTimer()

        // Setup help button
        btnHelpPendaki.setOnClickListener {
            navigateToRescued()
        }

        // Start alarm sound
        startAlarmSound()
    }

    private fun startCountUpTimer() {
        countUpTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000).toInt()
                val minutes = seconds / 60
                val hours = minutes / 60
                
                val timeString = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
                tvCountdownSosTime.text = timeString
            }

            override fun onFinish() {
                // This won't be called as we're using Long.MAX_VALUE
            }
        }.start()
    }

    private fun startAlarmSound() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.sos_alarm)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateToRescued() {
        val intent = Intent(this, RescuedActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countUpTimer?.cancel()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
}