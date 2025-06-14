package com.example.atlas.ml

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.atlas.ui.FallConfirmationActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class FallDetector(
    private val sensorManager: SensorManager,
    private val context: Context
) : SensorEventListener {
    private val TAG = "FallDetector"
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val _fallState = MutableStateFlow<FallState>(FallState.Idle)
    val fallState: StateFlow<FallState> = _fallState

    private val TH1 = 2.16f
    private val TH2 = 1.43f
    private val T1 = 1450L
    private val T2 = 890L

    private var isMonitoring = false
    private var startTime = 0L
    private var cnt = 0
    private var isAboveTH1 = false
    private var isAboveTH2 = false
    private var lastAboveTH2Time = 0L

    sealed class FallState {
        object Idle : FallState()
        object Running : FallState()
        object FallDetected : FallState()
    }

    init {
        if (accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Accelerometer registered successfully")
        } else {
            Log.e(TAG, "No accelerometer sensor found")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            if (System.currentTimeMillis() % 10 == 0L) {
                Log.d(TAG, "Accelerometer values - X: $x, Y: $y, Z: $z")
            }

            val acceleration = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
            Log.d(TAG, "Current acceleration: $acceleration g")

            if (!isMonitoring) {
                if (acceleration > TH1) {
                    isMonitoring = true
                    startTime = System.currentTimeMillis()
                    cnt = 1
                    isAboveTH1 = true
                    Log.d(TAG, "Started monitoring - acceleration: $acceleration")
                }
            } else {
                val currentTime = System.currentTimeMillis()
                val timeSinceStart = currentTime - startTime

                if (timeSinceStart <= T1) {
                    if (acceleration > TH1) {
                        cnt++
                        Log.d(TAG, "Impact detected - count: $cnt")
                    }
                } else if (timeSinceStart <= T1 + T2) {
                    if (acceleration > TH2) {
                        isAboveTH2 = true
                        lastAboveTH2Time = currentTime
                        Log.d(TAG, "Post-impact activity detected")
                    }
                } else {
                    if (cnt >= 2 && isAboveTH2) {
                        _fallState.value = FallState.FallDetected
                        Log.d(TAG, "Fall detected! Opening confirmation screen")
                        val intent = Intent(context, FallConfirmationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        _fallState.value = FallState.Running
                        Log.d(TAG, "No fall detected, continuing monitoring")
                    }
                    isMonitoring = false
                    cnt = 0
                    isAboveTH1 = false
                    isAboveTH2 = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
        _fallState.value = FallState.Idle
    }
}
