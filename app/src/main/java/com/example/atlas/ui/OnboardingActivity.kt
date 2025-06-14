package com.example.atlas.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.atlas.R
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.startButton).setOnClickListener {
            startActivity(Intent(this, EmergencyContactActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.termsButton).setOnClickListener {
            startActivity(Intent(this, TermsOfUsageActivity::class.java))
        }
    }
}