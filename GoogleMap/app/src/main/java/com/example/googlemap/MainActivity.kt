package com.example.googlemap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var currentPolyline: Polyline? = null

    private val taipei101 = LatLng(25.033611, 121.565000)
    private val taipeiMainStation = LatLng(25.047924, 121.517081)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnDriving = findViewById<Button>(R.id.btnDriving)
        val btnWalking = findViewById<Button>(R.id.btnWalking)
        val btnBicycling = findViewById<Button>(R.id.btnBicycling)

        btnDriving.setOnClickListener {
            drawRoute(TravelMode.DRIVING)
        }
        btnWalking.setOnClickListener {
            drawRoute(TravelMode.WALKING)
        }
        btnBicycling.setOnClickListener {
            drawRoute(TravelMode.BICYCLING)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val isAccessFineLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val isAccessCoarseLocationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isAccessFineLocationGranted && isAccessCoarseLocationGranted) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }

        // Add markers for Taipei 101 and Taipei Main Station
        googleMap.addMarker(MarkerOptions().position(taipei101).title("台北101"))
        googleMap.addMarker(MarkerOptions().position(taipeiMainStation).title("台北車站"))

        // Move camera to initial position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.04, 121.54), 13f))

        // Draw initial walking route
        drawRoute(TravelMode.WALKING)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap?.isMyLocationEnabled = true
                }
            }
        }
    }

    private fun drawRoute(travelMode: TravelMode) {
        val googleMap = mMap ?: return

        val apiKeyFromInfo = try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }

        val apiKey = if (!apiKeyFromInfo.isNullOrEmpty()) apiKeyFromInfo else "AIzaSyDamFrnLDKt0TaXg61m5vbh_JicJ0N6ADM"

        lifecycleScope.launch(Dispatchers.IO) {
            var encodedPolyline: String? = null
            var lastError: Exception? = null

            // 1. 嘗試 Legacy Directions API
            try {
                val geoApiContext = GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build()

                val directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .origin(com.google.maps.model.LatLng(taipei101.latitude, taipei101.longitude))
                    .destination(com.google.maps.model.LatLng(taipeiMainStation.latitude, taipeiMainStation.longitude))
                    .mode(travelMode)
                    .await()

                if (directionsResult.routes != null && directionsResult.routes.isNotEmpty()) {
                    encodedPolyline = directionsResult.routes[0].overviewPolyline.encodedPath
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "Directions API (Legacy) 失敗，嘗試切換至 Routes API (New): ${e.message}")
                lastError = e
            }

            // 2. 若 Legacy API 失敗 (例如出前 LegacyApiNotActivatedMapError)，自動備援使用新版 Routes API
            if (encodedPolyline.isNullOrEmpty()) {
                try {
                    encodedPolyline = fetchRoutesApi(apiKey, travelMode)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Routes API (New) 亦失敗: ${e.message}", e)
                    if (lastError == null) lastError = e
                }
            }

            withContext(Dispatchers.Main) {
                if (!encodedPolyline.isNullOrEmpty()) {
                    val decodedPath = PolyUtil.decode(encodedPolyline)

                    currentPolyline?.remove()

                    val polylineOptions = PolylineOptions()
                        .addAll(decodedPath)
                        .color(Color.RED)
                        .width(15f)

                    currentPolyline = googleMap.addPolyline(polylineOptions)

                    try {
                        val boundsBuilder = LatLngBounds.Builder()
                        decodedPath.forEach { boundsBuilder.include(it) }
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Camera animation error: ${e.message}")
                    }
                } else {
                    val errorMsg = lastError?.localizedMessage ?: lastError?.message ?: "無路線資料"
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("路線抓取失敗")
                        .setMessage("錯誤訊息：\n\n$errorMsg\n\n請在 Google Cloud Console 中搜尋並啟用：\n1. 「Directions API」 或\n2. 「Routes API」")
                        .setPositiveButton("確定", null)
                        .show()
                }
            }
        }
    }

    private fun fetchRoutesApi(apiKey: String, travelMode: TravelMode): String? {
        val modeString = when (travelMode) {
            TravelMode.DRIVING -> "DRIVE"
            TravelMode.WALKING -> "WALK"
            TravelMode.BICYCLING -> "BICYCLE"
            else -> "WALK"
        }

        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("X-Goog-Api-Key", apiKey)
        conn.setRequestProperty("X-Goog-FieldMask", "routes.polyline.encodedPolyline")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val jsonBody = JSONObject().apply {
            put("origin", JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", taipei101.latitude)
                        put("longitude", taipei101.longitude)
                    })
                })
            })
            put("destination", JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", taipeiMainStation.latitude)
                        put("longitude", taipeiMainStation.longitude)
                    })
                })
            })
            put("travelMode", modeString)
        }

        conn.outputStream.use { os ->
            os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
        }

        val responseCode = conn.responseCode
        if (responseCode == 200) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            val routes = jsonResponse.optJSONArray("routes")
            if (routes != null && routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val polyline = route.optJSONObject("polyline")
                return polyline?.optString("encodedPolyline")
            }
        } else {
            val errorResponse = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.e("MainActivity", "Routes API HTTP $responseCode: $errorResponse")
            throw Exception("Google API 傳回錯誤 (HTTP $responseCode):\n$errorResponse")
        }
        return null
    }
}
