package com.kaiser.weatherapp.tasks

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.kaiser.weatherapp.APIKey
import com.kaiser.weatherapp.helpers.LocationHelper
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Class that handles all calls to WeatherAPI, implements CoroutineScope
 * and receives it's context from the caller to keep everything under the same watch
 */
class WeatherTask(override val coroutineContext: CoroutineContext, private val locale: Locale) :
    CoroutineScope {

    /**
     * Calls History modality of the API, returns the data per hour of two days
     * @param queue The queue to add this request
     * @param locale The Locale to use when parsing dates
     * @return The JSON response
     */
    suspend fun todayResume(queue: RequestQueue)
            : String = suspendCoroutine { cont ->
        val today = GregorianCalendar(TimeZone.getDefault(), locale)
        val startDate: String = SimpleDateFormat("yyyy-MM-dd", locale).format(today.time)
        today.add(Calendar.DATE, 1)
        val endDate: String = SimpleDateFormat("yyyy-MM-dd", locale).format(today.time)
        val url =
            "https://api.weatherapi.com/v1/history.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&dt=${startDate}&end_dt=${endDate}&lang=${locale.language}"

        val stringResponse = StringRequest(Request.Method.GET, url, Response.Listener { result ->
            cont.resume(result)
        }, Response.ErrorListener { cont.resumeWithException(it.fillInStackTrace()) })

        queue.add(stringResponse)
    }

    /**
     * Calls forecast modality of the API, returns the data per hour of two days
     * @param queue The queue to add this request
     * @return The JSON response
     */
    suspend fun dailyResume(queue: RequestQueue)
            : String = suspendCoroutine { cont ->
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&days=8&lang=${locale.language}"

        val stringResponse = StringRequest(Request.Method.GET, url, Response.Listener { result ->
            cont.resume(result)
        }, Response.ErrorListener { cont.resumeWithException(it.fillInStackTrace()) })

        queue.add(stringResponse)
    }

}
