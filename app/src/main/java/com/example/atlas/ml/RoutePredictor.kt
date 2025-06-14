package com.example.atlas.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import java.nio.channels.FileChannel

class RoutePredictor(private val context: Context) {
    private val TAG = "RoutePredictor"
    private var interpreter: Interpreter? = null
    private val modelPath = "model/model.tflite"

    companion object {
        const val ROUTE_SEMANGAT_GUNUNG = "via Semangat Gunung"
        const val ROUTE_JARANGUDA = "via Jaranguda"
        const val ROUTE_54 = "Jalur 54"
    }

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            Log.d(TAG, "Loading model from $modelPath")
            val model = loadModelFile()
            interpreter = Interpreter(model)
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            throw RuntimeException("Gagal memuat model: ${e.message}")
        }
    }

    private fun loadModelFile(): ByteBuffer {
        try {
            val fileDescriptor = context.assets.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            Log.d(TAG, "Model file size: $declaredLength bytes")
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model file", e)
            throw RuntimeException("Gagal membaca file model: ${e.message}")
        }
    }

    fun predictRoute(physicalCondition: Int, experience: Int, weather: Int): String {
        require(physicalCondition in 1..3) { "Kondisi fisik harus antara 1-3" }
        require(experience in 1..3) { "Pengalaman harus antara 1-3" }
        require(weather in 1..2) { "Cuaca harus antara 1-2" }

        if (interpreter == null) {
            Log.e(TAG, "Interpreter is null, model not loaded")
            return "Model tidak dimuat dengan benar"
        }

        try {
            Log.d(TAG, "Preparing input: physical=$physicalCondition, experience=$experience, weather=$weather")

            val inputArray = floatArrayOf(
                physicalCondition.toFloat(),
                experience.toFloat(),
                weather.toFloat()
            )

            val inputBuffer = ByteBuffer.allocateDirect(4 * 3)
            inputBuffer.order(ByteOrder.nativeOrder())
            for (value in inputArray) {
                inputBuffer.putFloat(value)
            }
            inputBuffer.rewind()


            val outputBuffer = ByteBuffer.allocateDirect(4 * 3)
            outputBuffer.order(ByteOrder.nativeOrder())

            Log.d(TAG, "Running inference...")
            interpreter?.run(inputBuffer, outputBuffer)
            Log.d(TAG, "Inference completed")

            outputBuffer.rewind()
            val outputArray = FloatArray(3)
            for (i in 0 until 3) {
                outputArray[i] = outputBuffer.float
            }

            Log.d(TAG, "Model output: ${outputArray.joinToString()}")

            val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1

            return when (maxIndex) {
                0 -> ROUTE_54
                1 -> ROUTE_JARANGUDA
                2 -> ROUTE_SEMANGAT_GUNUNG
                else -> {
                    Log.e(TAG, "Invalid model output index: $maxIndex")
                    "Tidak dapat menentukan jalur"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during prediction", e)
            return "Error: ${e.message}"
        }
    }

    fun close() {
        try {
            interpreter?.close()
            Log.d(TAG, "Interpreter closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter", e)
        }
    }
} 