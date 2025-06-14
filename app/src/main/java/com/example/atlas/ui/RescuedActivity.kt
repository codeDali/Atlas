package com.example.atlas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R
import java.text.SimpleDateFormat
import java.util.*

class RescuedActivity : AppCompatActivity() {
    private lateinit var tvRescuedTime: TextView
    private lateinit var btnBackToHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rescued)

        btnBackToHome = findViewById(R.id.btn_home)

        val currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        tvRescuedTime.text = currentTime

        btnBackToHome.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 