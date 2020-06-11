package com.kaiser.weatherapp.ui.activities

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.kaiser.weatherapp.R
import com.kaiser.weatherapp.databinding.ActivityMainBinding
import com.kaiser.weatherapp.databinding.ForecastNodeBinding
import com.kaiser.weatherapp.databinding.NextDaysNodeBinding
import com.kaiser.weatherapp.extensions.formatToDate
import com.kaiser.weatherapp.helpers.LocationHelper
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.LAT
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.LON
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.lastUpdate
import com.kaiser.weatherapp.helpers.LocationHelperStatus
import com.kaiser.weatherapp.models.*
import com.kaiser.weatherapp.tasks.WeatherTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

/**
 * Application starting point
 * Also handles permission request for the user before updating the UI
 */
class MainActivity : AppCompatActivity(), CoroutineScope {

    /**
     * This coroutine context, keeps track of launched tasks
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * Binds to the main activity
     */
    private lateinit var activityBinding: ActivityMainBinding

    /**
     * Binds to the horizontal Scroll View with the details per hour
     */

    /**
     * Class that manages all actions involving user Location
     */
    private lateinit var helper: LocationHelper

    /**
     * Volley queue variable, manages requests and responses.
     * Must be sent to other methods, and those methods make use of it
     * since this activity is responsible for stopping the queue when destroyed
     */
    private lateinit var queue: RequestQueue

    /**
     * Represents the coroutine attached to the activity.
     * Must be stopped when activity is DESTROYED so it can continue if on background
     */
    private lateinit var job: Job

    /**
     * Class responsible for all the calls to the API.
     * Receives this coroutine context so everything starts under the same scope
     */
    private lateinit var task: WeatherTask

    /**
     * Identifies the data saved from the application on Shared Preferences (Cache).
     * Is private, need a key to be accessed
     */
    private val savedWeatherData = "saved_weather_data"

    /**
     * Key to unlock this app saved data
     */
    private val weatherDataKey = "key_weather_data"

    /**
     * JSON parser, used on serialization/deserialization
     */
    private val json = Json(JsonConfiguration.Stable.copy(isLenient = true, ignoreUnknownKeys = true))

    /**
     * permission id used in all location requests if needed
     */
    private val locationPermissionId = 7

    /**
     * This phone Locale, sent through the app for formatting dates
     */
    private val defaultLocale = Locale.getDefault()

    /**
     * Defines if the user has already been prompted to turn on location is this session
     */
    private var hasUserBeenRedirected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        queue = Volley.newRequestQueue(this)
        task = WeatherTask(coroutineContext)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        helper = LocationHelper(this)

        loadSavedData()

        showDetails(UIVisibilityEnum.Hide)

        setContentView(activityBinding.root)

        activityBinding.reloadButton.setOnClickListener {

            getWeatherData()

            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.rotate_around_center_point
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()

        getWeatherData()
    }

    override fun onStop() {
        super.onStop()
        savedApplicationData()
    }

    override fun onDestroy() {
        super.onDestroy()

        queue.stop()
        job.cancel()
    }

    /**
     * Saves this application usage data to cache
     */
    private fun savedApplicationData() {
        val savedData = StringBuilder()
        savedData.append("$LAT,")
        savedData.append("$LON,")
        savedData.append("$lastUpdate,")

        getSharedPreferences(savedWeatherData, Context.MODE_PRIVATE).edit()
            .putString(weatherDataKey, savedData.toString()).apply()
    }

    /**
     * Loads all data judged relevant to this app operation that has been previously saved
     */
    private fun loadSavedData() {
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
    }

    /**
     * Starts the execution of weather tasks. Prompts the user for location use. Request the user for access to location services
     */
    private fun getWeatherData() {
        when (helper.getLastLocation()) {
            LocationHelperStatus.LocationUnabled -> {
                promptEnableLocation()
            }
            LocationHelperStatus.NoPermission -> {
                requestPermissions()
            }
            LocationHelperStatus.OK -> {
                launch {
                    task.todayResume(queue, defaultLocale, ::updateTodayDetails)
                }
                launch {
                    task.dailyResume(queue, ::updateDailyResume)
                }
            }
        }
    }

    /**
     * Prompts the user to turn on location
     */
    private fun promptEnableLocation() {
        if (!hasUserBeenRedirected) {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            ContextCompat.startActivity(this, intent, null)
            hasUserBeenRedirected = true
        } else {
            showDetails(UIVisibilityEnum.ShowError)
        }
    }

    private fun updateDailyResume(result: String) {
        val forecastModel = json.parse(DailyForecastModel.serializer(), result)

        activityBinding.NextDaysForecast.removeAllViews()

        for (day in forecastModel.forecast.forecastDay) {
            createViewForDay(day)
        }
    }

    private fun createViewForDay(forecastDay: ForecastDayModel) {
        val iconId: Int
        val iconName = forecastDay.day.condition.icon.substringAfterLast('/').substringBeforeLast('.')
        val nextDaysNodeBinding = NextDaysNodeBinding.inflate(layoutInflater)

        nextDaysNodeBinding.nextDaysDesc.text = forecastDay.day.condition.text
        nextDaysNodeBinding.nextDaysFullDay.text = forecastDay.dateEpoch.formatToDate("EEEE - MMM dd")
        nextDaysNodeBinding.nextDaysMin.text = forecastDay.day.minTempCelsius.roundToInt().toString()
        nextDaysNodeBinding.nextDaysMax.text = forecastDay.day.maxTempCelsius.roundToInt().toString()

        iconId = resources.getIdentifier("day_$iconName", "drawable", packageName)
        nextDaysNodeBinding.nextDaysIcon.setImageResource(iconId)

        activityBinding.NextDaysForecast.addView(nextDaysNodeBinding.root)
    }

    /**
     * Called when the data should be updated on the UI
     * @param result The string to be parsed from JSON
     */
    private fun updateTodayDetails(result: String) {
        val historyModel = json.parse(HistoryModel.serializer(), result)
        val validHour = mutableListOf<HourModel>()
        val epochAtExecution = System.currentTimeMillis() / 1000
        val hourBefore = epochAtExecution - 3540
        val dayAfter = epochAtExecution + 86400

        activityBinding.TodayHourlyForecast.removeAllViews()

        for (day in historyModel.forecast.forecastDay) {
            for (hour in day.hour) {
                if (hour.timeEpoch in hourBefore..dayAfter) {
                    validHour.add(hour)
                }
            }
        }

        for (hour in validHour) {
            createViewForHour(hour)
        }

        val thisLocation = historyModel.location
        val thisDay = historyModel.forecast.forecastDay[0]
        val thisDayOverall = thisDay.day
        val thisHour = thisDay.hour[0]

        activityBinding.temp.text = getString(R.string.hourTempC, thisHour.tempCelsius)
        activityBinding.status.text = thisHour.condition.text
        activityBinding.updatedAt.text = getString(
            R.string.lastUpdated,
            thisLocation.localTimeEpoch.formatToDate("dd MMMM yyyy kk:mm", defaultLocale)
        )
        activityBinding.address.text = thisLocation.name
        activityBinding.tempMin.text = getString(R.string.dayTempC, thisDayOverall.minTempCelsius)
        activityBinding.tempMax.text = getString(R.string.dayTempC, thisDayOverall.maxTempCelsius)
        activityBinding.ScrollViewHeader.text =
            getString(
                R.string.dayOfWeek,
                SimpleDateFormat("EEEE", defaultLocale).format(Date())
            ).capitalize()

        lastUpdate = thisLocation.localTimeEpoch.toString()
    }

    /**
     * Takes care of UI visibility through the execution
     * @param option Defines what to show to the user
     */
    private fun showDetails(option: UIVisibilityEnum) {
        when (option) {
            UIVisibilityEnum.Show -> {
                activityBinding.loader.isVisible = false
                activityBinding.mainContainer.isVisible = true
                activityBinding.errorText.isVisible = false
            }
            UIVisibilityEnum.Hide -> {
                activityBinding.loader.isVisible = true
                activityBinding.mainContainer.isVisible = false
                activityBinding.errorText.isVisible = false
            }
            UIVisibilityEnum.ShowError -> {
                activityBinding.loader.isVisible = false
                activityBinding.mainContainer.isVisible = false
                activityBinding.errorText.text =
                    getText(R.string.Location_PermissionDeniedRationale)
                activityBinding.errorText.isVisible = true
            }
        }
    }

    /**
     * For every hour, creates the node on the screen
     * @param hour This HourModel to take the data from
     */
    private fun createViewForHour(hour: HourModel) {
        val iconId: Int
        val iconName = hour.condition.icon.substringAfterLast('/').substringBeforeLast('.')
        val horizontalViewBind = ForecastNodeBinding.inflate(layoutInflater)

        horizontalViewBind.nodeHour.text = hour.timeEpoch.formatToDate("kk:mm ", defaultLocale)
        horizontalViewBind.nodeTemperature.text = getString(R.string.hourTempC, hour.tempCelsius)
        val space = Space(this)
        val params = LinearLayout.LayoutParams(30, LinearLayout.LayoutParams.WRAP_CONTENT)
        space.layoutParams = params

        val iconPrefix = if (hour.isDay == 0) {
            "night_"
        } else {
            "day_"
        }

        iconId = resources.getIdentifier(iconPrefix + iconName, "drawable", packageName)
        horizontalViewBind.nodeIcon.setImageResource(iconId)
        activityBinding.TodayHourlyForecast.addView(space)
        activityBinding.TodayHourlyForecast.addView(horizontalViewBind.root)

    }

    /**
     * Request permission to the user, current only location is requested
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION),
            locationPermissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            locationPermissionId -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getWeatherData()
                    return
                } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showDetails(UIVisibilityEnum.ShowError)
                }
            }
        }
    }
}

/**
 * Possible UI Visibility statuses
 */
enum class UIVisibilityEnum {
    Show,
    Hide,
    ShowError
}
