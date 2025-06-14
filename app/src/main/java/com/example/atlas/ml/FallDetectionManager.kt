package com.example.atlas.ml

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

class FallDetectionManager(private val context: Context) {
    private val TAG = "FallDetectionManager"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var fallDetector: FallDetector? = null
    private var isMonitoring = false

    val fallState: StateFlow<FallDetector.FallState>?
        get() = fallDetector?.fallState

    fun startMonitoring() {
        if (!isMonitoring) {
            Log.d(TAG, "Starting fall detection monitoring")
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null) {
                Log.d(TAG, "Accelerometer found: ${accelerometer.name}")
                fallDetector = FallDetector(sensorManager, context)
                isMonitoring = true
            } else {
                Log.e(TAG, "No accelerometer sensor found on device")
            }
        }
    }

    fun stopMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Stopping fall detection monitoring")
            fallDetector?.unregister()
            fallDetector = null
            isMonitoring = false
        }
    }

    fun isFallDetected(): Boolean {
        return fallState?.value is FallDetector.FallState.FallDetected
    }

    fun isRunning(): Boolean {
        return fallState?.value is FallDetector.FallState.Running
    }

    fun isActive(): Boolean {
        return isMonitoring
    }
} 