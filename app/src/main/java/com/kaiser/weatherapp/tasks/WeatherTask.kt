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
import kotlin.coroutines.suspendCoroutine

/**
 * Class that handles all calls to WeatherAPI, implements CoroutineScope
 * and receives it's context from the caller to keep everything under the same watch
 */
class WeatherTask(override val coroutineContext: CoroutineContext) : CoroutineScope {

    /**
     * Calls History modality of the API, returns the data per hour of two days
     * @param queue The queue to add this request
     * @param locale The Locale to use when parsing dates
     * @param functionToCall The function to call when request is completed, must accept a string
     * @return The JSON response
     */
    suspend fun todayResume(queue: RequestQueue, locale: Locale, functionToCall: (String) -> Unit)
            : String = suspendCoroutine { cont ->
        val today = GregorianCalendar(TimeZone.getDefault(), locale)
        val startDate: String = SimpleDateFormat("yyyy-MM-dd", locale).format(today.time)
        today.add(Calendar.DATE, 1)
        val endDate: String = SimpleDateFormat("yyyy-MM-dd", locale).format(today.time)
        val url =
            "https://api.weatherapi.com/v1/history.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&dt=${startDate}&end_dt=${endDate}"

        val stringResponse = StringRequest(Request.Method.GET, url, Response.Listener { result ->
            functionToCall(result)
        }, Response.ErrorListener { cont.resume("") })

        queue.add(stringResponse)
    }

    /**
     * Calls forecast modality of the API, returns the data per hour of two days
     * @param queue The queue to add this request
     * @param functionToCall The function to call when request is completed, must accept a string
     * @return The JSON response
     */
    suspend fun dailyResume(queue: RequestQueue, functionToCall: (String) -> Unit)
            : String = suspendCoroutine { cont ->
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&days=7"

        val stringResponse = StringRequest(Request.Method.GET, url, Response.Listener { result ->
            functionToCall(result)
        }, Response.ErrorListener { cont.resume("") })

        queue.add(stringResponse)
    }

}
