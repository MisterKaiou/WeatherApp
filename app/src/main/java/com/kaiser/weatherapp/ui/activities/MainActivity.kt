package com.kaiser.weatherapp.ui.activities

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kaiser.weatherapp.R
import com.kaiser.weatherapp.databinding.ActivityMainBinding
import com.kaiser.weatherapp.extensions.formatToDate
import com.kaiser.weatherapp.helpers.LocationHelper
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.LAT
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.LON
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.lastUpdate
import com.kaiser.weatherapp.tasks.WeatherTask
import org.json.JSONObject
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
	
	private lateinit var binding: ActivityMainBinding
	private lateinit var mFusedLocationClient: FusedLocationProviderClient
	private lateinit var helper: LocationHelper
	
	private val SAVED_WEATHER_DATA = "saved_weather_data"
	private val KEY_WEATHER_DATA = "key_weather_data"
	
	var isStartUp: Boolean = true
	var PERMISSION_ID = 7
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val savedData = getSharedPreferences(SAVED_WEATHER_DATA, Context.MODE_PRIVATE).getString(
				KEY_WEATHER_DATA,
				null
		)
		if (!savedData.isNullOrEmpty()) {
            val data = savedData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (!data.isNullOrEmpty()){
                LAT = data[0]
                LON = data[1]
                lastUpdate = data[2]
            }
		}
  
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		binding = ActivityMainBinding.inflate(layoutInflater)
		helper = LocationHelper(this, mFusedLocationClient)
		binding.reloadButton.setOnClickListener {
			if (helper.getLastLocation()) {
				populateScreen(WeatherTask.Companion.Task(this).execute().get())
			}
			it.startAnimation(
					AnimationUtils.loadAnimation(
							this,
							R.anim.rotate_around_center_point
					)
			)
		}
		setContentView(binding.root)
		
		/* Showing the ProgressBar, Making the main design GONE */
		if (isStartUp) {
			binding.loader.isVisible = true
			binding.mainContainer.isVisible = false
			binding.errorText.isVisible = false
			isStartUp = false
		}
		
		try {
			if (helper.getLastLocation()) {
				populateScreen(WeatherTask.Companion.Task(this).execute().get())
			}
		} catch (e: Exception) {
			when (e.message) {
				R.string.LocationError_Permission.toString() -> {
					requestPermissions()
				}
			}
		}
	}
	
	override fun onStop() {
		super.onStop()
		val savedData = StringBuilder()
        savedData.append("$LAT,")
        savedData.append("$LON,")
        savedData.append("$lastUpdate,")
		
		getSharedPreferences(SAVED_WEATHER_DATA, Context.MODE_PRIVATE).edit()
				.putString(KEY_WEATHER_DATA, savedData.toString()).apply()
        
        mFusedLocationClient.flushLocations()
	}

	override fun onDestroy() {
		super.onDestroy()

		this.finish()
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
					helper.getLastLocation()
					WeatherTask.Companion.Task(this).execute()
					return
				} else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
					binding.loader.isVisible = false
					binding.errorText.text = getText(R.string.Location_PermissionDeniedRationale)
					binding.errorText.isVisible = true
				}
			}
		}
	}

	private fun populateScreen(result: String) {
		val jsonObj = JSONObject(result)
		val location = jsonObj.getJSONObject("location")
		val current = jsonObj.getJSONObject("current")
		val condition = current.getJSONObject("condition")
		val forecast =
			jsonObj.getJSONObject("forecast").getJSONArray("forecastday")
				.getJSONObject(0)
		val forecastToday = forecast.getJSONObject("day")
		val forecastAstro = forecast.getJSONObject("astro")

		val address = location.getString("name") + ", " + location.getString("country")
		val updatetAt = current.getLong("last_updated_epoch")
		val updatedAtText = "Update At: " + updatetAt.formatToDate()
		lastUpdate = updatetAt.toString()
		val weatherDescription = condition.getString("text")
		val temp = current.getDouble("temp_c").toInt().toString() + "ºC"
		val tempMin =
			"Min Temp: " + forecastToday.getDouble("mintemp_c").toInt()
				.toString() + "ºC"
		val tempMax =
			"Max Temp: " + forecastToday.getDouble("maxtemp_c").toInt()
				.toString() + "ºC"
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
		binding.loader.isVisible = false
		binding.mainContainer.isVisible = true
	}
}
