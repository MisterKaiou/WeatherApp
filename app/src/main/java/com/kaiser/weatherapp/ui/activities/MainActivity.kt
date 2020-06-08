package com.kaiser.weatherapp.ui.activities

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kaiser.weatherapp.R
import com.kaiser.weatherapp.databinding.ActivityMainBinding
import com.kaiser.weatherapp.databinding.ForecastNodeBinding
import com.kaiser.weatherapp.extensions.formatToDate
import com.kaiser.weatherapp.helpers.LocationHelper
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.LAT
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.LON
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.lastUpdate
import com.kaiser.weatherapp.models.HistoryModel
import com.kaiser.weatherapp.models.HourModel
import com.kaiser.weatherapp.tasks.WeatherTask
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var binding: ActivityMainBinding
    private lateinit var nodeBind: ForecastNodeBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var helper: LocationHelper
    private lateinit var queue: RequestQueue
    private lateinit var job: Job
    private lateinit var task: WeatherTask

    private val savedWeatherData = "saved_weather_data"
    private val weatherDataKey = "key_weather_data"
    private val json = Json(JsonConfiguration.Stable.copy())
    private val PERMISSION_ID = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        queue = Volley.newRequestQueue(this)
        task = WeatherTask(coroutineContext)

        val savedData = getSharedPreferences(savedWeatherData, Context.MODE_PRIVATE).getString(
            weatherDataKey,
            null
        )

        if (!savedData.isNullOrEmpty()) {
            val data = savedData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (!data.isNullOrEmpty() && data.size == 3) {
                LAT = data[0]
                LON = data[1]
                lastUpdate = data[2]
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        helper = LocationHelper(this, mFusedLocationClient)

        /* Showing the ProgressBar, Making the main design GONE */
        showDetails(false)

        setContentView(binding.root)

        binding.reloadButton.setOnClickListener {
            launch {
                getWeatherData()
            }

            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.rotate_around_center_point
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        val savedData = StringBuilder()
        savedData.append("$LAT,")
        savedData.append("$LON,")
        savedData.append("$lastUpdate,")

        getSharedPreferences(savedWeatherData, Context.MODE_PRIVATE).edit()
            .putString(weatherDataKey, savedData.toString()).apply()

        mFusedLocationClient.flushLocations()
    }

    override fun onStart() {
        super.onStart()

        getWeatherData()
    }

    override fun onDestroy() {
        super.onDestroy()

        queue.stop()
        job.cancel()
    }

    private fun getWeatherData() {
        if (helper.checkPermissions()) {
            launch { task.todayResume(queue, ::updateDetails) }
        } else {
            requestPermissions()
        }
    }

    private fun updateDetails(result: String) {
        val historyModel: HistoryModel = json.parse(HistoryModel.serializer(), result)

        try {
            if (helper.getLastLocation()) {
                populateHorizontalScrollView(historyModel)
            }
        } catch (e: Exception) {
            when (e.message) {
                R.string.LocationError_Permission.toString() -> {
                    requestPermissions()
                }
            }
        }
    }

    private fun showDetails(should: Boolean) {
        if (should) {
            binding.loader.isVisible = false
            binding.mainContainer.isVisible = true
            binding.errorText.isVisible = false
        } else if (!should) {
            binding.loader.isVisible = true
            binding.mainContainer.isVisible = false
            binding.errorText.isVisible = false
        }
    }

    private fun populateHorizontalScrollView(model: HistoryModel) {
        val validHour = mutableListOf<HourModel>()
        val epochAtExecution = System.currentTimeMillis() / 1000
        val hourBefore = epochAtExecution - 3540
        val dayAfter = epochAtExecution + 86400

        binding.TodayHourlyForecast.removeAllViews()

        for (day in model.forecast.forecastDay) {
            for (hour in day.hour) {
                if (hour.timeEpoch in hourBefore..dayAfter) {
                    validHour.add(hour)
                }
            }
        }

        for (hour in validHour) {
            createViewForHour(hour)
        }

        val thisLocation = model.location
        val thisDay = model.forecast.forecastDay[0]
        val thisDayOverall = thisDay.day
        val thisHour = thisDay.hour[0]

        binding.temp.text = getString(R.string.hourTempC, thisHour.tempCelsius)
        binding.status.text = thisHour.condition.text
        binding.updatedAt.text = getString(
            R.string.lastUpdated,
            thisLocation.localTimeEpoch.formatToDate("dd MMMM yyyy kk:mm")
        )
        binding.address.text = thisLocation.name
        binding.tempMin.text = getString(R.string.dayTempC, thisDayOverall.minTempCelsius)
        binding.tempMax.text = getString(R.string.dayTempC, thisDayOverall.maxTempCelsius)

        lastUpdate = thisLocation.localTimeEpoch.toString()
        showDetails(true)
    }

    private fun createViewForHour(hour: HourModel) {
        nodeBind = ForecastNodeBinding.inflate(layoutInflater)
        nodeBind.nodeHour.text = hour.timeEpoch.formatToDate("kk:mm ")
        nodeBind.nodeTemperature.text = getString(R.string.hourTempC, hour.tempCelsius)
        val space = Space(this)
        val params = LinearLayout.LayoutParams(30, LinearLayout.LayoutParams.WRAP_CONTENT)
        space.layoutParams = params
        binding.TodayHourlyForecast.addView(space)
        binding.TodayHourlyForecast.addView(nodeBind.root)
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
                    launch { task.todayResume(queue, ::updateDetails) }
                    return
                } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    binding.loader.isVisible = false
                    binding.errorText.text = getText(R.string.Location_PermissionDeniedRationale)
                    binding.errorText.isVisible = true
                }
            }
        }
    }
}
