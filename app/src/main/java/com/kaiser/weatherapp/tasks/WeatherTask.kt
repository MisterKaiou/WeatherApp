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

class WeatherTask(override val coroutineContext: CoroutineContext) : CoroutineScope {

    suspend fun todayResume(queue: RequestQueue, func: (String) -> Unit): String = suspendCoroutine { cont ->
        val today = GregorianCalendar(TimeZone.getDefault(), Locale.ENGLISH)
        val startDate: String = SimpleDateFormat("yyyy-MM-dd").format(today.time)
        today.add(Calendar.DATE, 1)
        val endDate: String = SimpleDateFormat("yyyy-MM-dd").format(today.time)
        val url =
            "https://api.weatherapi.com/v1/history.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&dt=${startDate}&end_dt=${endDate}"

        val stringResponse = StringRequest(Request.Method.GET, url, Response.Listener { result ->
            func(result)
        }, Response.ErrorListener { cont.resume("") })

        queue.add(stringResponse)
    }
}
