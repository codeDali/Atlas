package com.example.atlas.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R

class SosActivity : AppCompatActivity() {
    private lateinit var tvCountdownSosTime: TextView
    private lateinit var btnHelpPendaki: Button
    private var countUpTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sos)

        tvCountdownSosTime = findViewById(R.id.tv_countdown_sos_time)
        btnHelpPendaki = findViewById(R.id.btn_help_pendaki)

        startTime = System.currentTimeMillis()
        startCountUpTimer()

        btnHelpPendaki.setOnClickListener {
            navigateToFinish()
        }

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
                // TODO
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

    private fun navigateToFinish() {
        val intent = Intent(this, FinishActivity::class.java)
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