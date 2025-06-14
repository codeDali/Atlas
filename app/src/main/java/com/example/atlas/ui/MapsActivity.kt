package com.example.atlas.ui

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
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
    private val polyline = Polyline().apply {
        outlinePaint.color = Color.RED
        outlinePaint.strokeWidth = 5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        loadGpxFile()
    }

    private fun loadGpxFile() {
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.jaranguda)
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