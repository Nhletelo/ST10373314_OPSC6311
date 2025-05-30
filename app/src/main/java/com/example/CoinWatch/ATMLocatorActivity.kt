package com.example.CoinWatch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import org.json.JSONObject
import java.util.*

class ATMLocatorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var progressBar: ProgressBar
    private lateinit var bankSpinner: Spinner
    private lateinit var atmBrandCheckBox: CheckBox
    private lateinit var searchBar: EditText

    private val apiKey = "AIzaSyA34DWpahjywSNLhB0uRLUbw8S2E0iZhdw"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val banks = arrayOf("All Banks", "ABSA", "Standard Bank", "FNB", "Nedbank")
    private val filterBrand = "ATM Brand X"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atm_locator)

        progressBar = findViewById(R.id.progressBar)
        bankSpinner = findViewById(R.id.spinnerBanks)
        atmBrandCheckBox = findViewById(R.id.checkBoxATMBrand)
        searchBar = findViewById(R.id.searchBar)

        // Populate spinner
        val bankAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, banks)
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bankSpinner.adapter = bankAdapter

        // Initialize Places API if needed
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Map Fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            startActivity(Intent(this, HomeScreen::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val query = searchBar.text.toString().trim()
            hideKeyboard()
            if (query.isNotEmpty()) {
                mMap.clear()
                searchLocationAndFindATMs(query)
            } else {
                showToast("Please enter a location.")
            }
        }

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            mMap.clear()
            getLastLocationAndFindATMs()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            mMap.isMyLocationEnabled = true
            getLastLocationAndFindATMs()
        }
    }

    private fun getLastLocationAndFindATMs() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        progressBar.visibility = View.VISIBLE
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                fetchATMs(userLocation)
            } else {
                progressBar.visibility = View.GONE
                showToast("Unable to get current location.")
            }
        }
    }

    private fun fetchATMs(location: LatLng) {
        val radius = 2000
        val type = "atm"
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${location.latitude},${location.longitude}&radius=$radius&type=$type&key=$apiKey"

        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url, { response ->
            progressBar.visibility = View.GONE
            val results = JSONObject(response).optJSONArray("results") ?: return@StringRequest

            val selectedBank = bankSpinner.selectedItem.toString()
            val filterATMBrand = atmBrandCheckBox.isChecked

            for (i in 0 until results.length()) {
                val place = results.optJSONObject(i) ?: continue
                val name = place.optString("name", "ATM")
                val vicinity = place.optString("vicinity", "Unknown")
                val loc = place.getJSONObject("geometry").getJSONObject("location")
                val latLng = LatLng(loc.getDouble("lat"), loc.getDouble("lng"))

                if (selectedBank != "All Banks" && !name.contains(selectedBank, ignoreCase = true)) continue
                if (filterATMBrand && !name.contains(filterBrand, ignoreCase = true)) continue

                val resultDistance = FloatArray(1)
                Location.distanceBetween(
                    location.latitude, location.longitude,
                    latLng.latitude, latLng.longitude,
                    resultDistance
                )
                val distanceText = String.format("%.1f m", resultDistance[0])

                mMap.addMarker(
                    MarkerOptions().position(latLng).title(name)
                        .snippet("$vicinity\n$distanceText")
                )?.tag = "$vicinity|$distanceText"
            }

            mMap.setOnInfoWindowClickListener { marker ->
                val tag = marker.tag as? String ?: return@setOnInfoWindowClickListener
                val (vicinity, distance) = tag.split("|")
                val distanceInMeters = distance.replace(" m", "").toFloatOrNull() ?: 0f

                if (distanceInMeters > 5000) {
                    showToast("Sorry, the selected ATM is out of range.")
                } else {
                    showToast("${marker.title}\n$vicinity\n$distance")
                }
            }

            if (results.length() == 0) {
                showToast("No ATMs found nearby.")
            }

        }, { error ->
            progressBar.visibility = View.GONE
            showToast("Error fetching ATMs: ${error.message}")
        })

        queue.add(request)
    }

    private fun searchLocationAndFindATMs(query: String) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(query, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val searchedLocation = LatLng(address.latitude, address.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 15f))
                fetchATMs(searchedLocation)
            } else {
                showToast("Location not found.")
            }
        } catch (e: Exception) {
            showToast("Error locating place: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
