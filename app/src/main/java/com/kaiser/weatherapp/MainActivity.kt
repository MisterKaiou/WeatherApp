package com.kaiser.weatherapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.Manifest.*
import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.os.Looper
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.kaiser.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.Exception
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var LAT: String = ""
    var LON: String = ""
    var isStartUp: Boolean = true
    var days = 1

    val PERMISSION_ID = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.reloadButton.setOnClickListener {
            getLastLocation()
            isStartUp = false
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_around_center_point))
        }

        setContentView(binding.root)
        getLastLocation()
    }

    private inner class WeatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            if (isStartUp) {
                binding.loader.visibility = View.VISIBLE
                binding.mainContainer.visibility = View.GONE
                binding.errorText.visibility = View.GONE
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?

            try {
                response =
                    URL("https://api.weatherapi.com/v1/forecast.json?key=${APIKey.Key}&q=$LAT,$LON&days=$days").readText(
                        Charsets.UTF_8
                    )
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                /*Extracting JSON returns from the API*/
                val jsonObj = JSONObject(result)
                val location = jsonObj.getJSONObject("location")
                val current = jsonObj.getJSONObject("current")
                val condition = current.getJSONObject("condition")
                val forecast =
                    jsonObj.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0)
                val forecastToday = forecast.getJSONObject("day")
                val forecastAstro = forecast.getJSONObject("astro")

                val address = location.getString("name") + ", " + location.getString("country")
                val updatedAtText = "Update At: " + formatTicksToDate(
                    current.getLong("last_updated_epoch"),
                    "dd/MM/yyyy hh:mm a",
                    Locale.ENGLISH
                )
                val weatherDescription = condition.getString("text")
                val temp = current.getDouble("temp_c").toInt().toString() + "ºC"
                val tempMin =
                    "Min Temp: " + forecastToday.getDouble("mintemp_c").toInt().toString() + "ºC"
                val tempMax =
                    "Max Temp: " + forecastToday.getDouble("maxtemp_c").toInt().toString() + "ºC"
                val sunset = forecastAstro.getString("sunset")
                val sunrise = forecastAstro.getString("sunrise")
                val windSpeed = current.getDouble("wind_kph").toString() + " Km/h"
                val pressure = current.getString("pressure_mb").toDouble().toInt().toString()
                val humidity = current.getString("humidity")

                /* Populating extracted data */
                binding.address.text = address
                binding.updatedAt.text = updatedAtText
                binding.status.text = weatherDescription.capitalize()
                binding.temp.text = temp
                binding.tempMin.text = tempMin
                binding.tempMax.text = tempMax
                binding.sunset.text = sunset
                binding.sunrise.text = sunrise
                binding.wind.text = windSpeed
                binding.pressure.text = pressure
                binding.humidity.text = humidity

                /* Views populated, Hiding loader, showing main design */
                binding.loader.visibility = View.GONE
                binding.mainContainer.visibility = View.VISIBLE

            } catch (e: Exception) {
                null
            }
        }
    }

    fun formatTicksToDate(timeTicks: Long, format: String, locale: Locale): String {
        return SimpleDateFormat(format, locale).format(
            Date(timeTicks * 1000)
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                getCoordinates()
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun getCoordinates() {
        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            var location: Location? = task.result
            if (location == null) {
                requestNewLocationData()
            } else {
                LAT = location.latitude.toString()
                LON = location.longitude.toString()

                WeatherTask().execute()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallBack,
            Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            LAT = mLastLocation.latitude.toString()
            LON = mLastLocation.longitude.toString()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this@MainActivity,
                permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
        }
    }
}
