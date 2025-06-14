package com.example.atlas.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R
import com.google.android.material.button.MaterialButton

class TermsOfUsageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_usage)
        findViewById<MaterialButton>(R.id.startButton).setOnClickListener {
            startActivity(Intent(this, EmergencyContactActivity::class.java))
            finish()
        }
    }
} 