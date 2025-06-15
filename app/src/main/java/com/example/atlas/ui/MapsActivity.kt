package com.example.atlas.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

class MapsActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var tvJalur54: TextView
    private val polyline = Polyline().apply {
        outlinePaint.color = Color.RED
        outlinePaint.strokeWidth = 5f
    }
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        mapView = findViewById(R.id.mapView)
        tvJalur54 = findViewById(R.id.tv_jalur_54)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        loadGpxFile()
    }

    private fun loadGpxFile() {
        sharedPreferences = getSharedPreferences("PredictionPrefs", Context.MODE_PRIVATE)
        val prediction = intent.getStringExtra("PREDICTION") ?: sharedPreferences.getString("last_prediction", "Silahkan kembali ke halaman input")
        
        if (prediction == "Jalur 54") {
            mapView.visibility = View.GONE
            tvJalur54.visibility = View.VISIBLE
            return
        }

        mapView.visibility = View.VISIBLE
        tvJalur54.visibility = View.GONE
        
        try {
            val gpxResource = when (prediction) {
                "via Semangat Gunung" -> R.raw.semangat_gunung
                "via Jaranguda" -> R.raw.jaranguda
                else -> R.raw.jaranguda
            }
            
            val inputStream: InputStream = resources.openRawResource(gpxResource)
            val parser = SAXParserFactory.newInstance().newSAXParser()
            val handler = GpxHandler()
            parser.parse(inputStream, handler)

            polyline.setPoints(handler.points)
            mapView.overlays.add(polyline)

            if (handler.points.isNotEmpty()) {
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(handler.points[0])
            }

            mapView.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}

class GpxHandler : org.xml.sax.helpers.DefaultHandler() {
    val points = mutableListOf<GeoPoint>()
    private var currentLat = 0.0
    private var currentLon = 0.0
    private var isTrackPoint = false

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: org.xml.sax.Attributes?) {
        when (qName) {
            "trkpt", "wpt" -> {
                currentLat = attributes?.getValue("lat")?.toDoubleOrNull() ?: 0.0
                currentLon = attributes?.getValue("lon")?.toDoubleOrNull() ?: 0.0
                isTrackPoint = true
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (isTrackPoint && qName == "trkpt") {
            points.add(GeoPoint(currentLat, currentLon))
            isTrackPoint = false
        }
    }


}