package com.kaiser.weatherapp.tasks

import android.app.Activity
import android.os.AsyncTask
import com.kaiser.weatherapp.APIKey
import com.kaiser.weatherapp.helpers.LocationHelper
import java.lang.ref.WeakReference
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class WeatherTask {
    companion object {
        class Tasks {

            class TodayResume(
                context: Activity
            ) :
                AsyncTask<String, Void, String>() {

                private val activityReference: WeakReference<Activity> = WeakReference(context)
                private var today = GregorianCalendar(TimeZone.getDefault(), Locale.ENGLISH)

                override fun onPreExecute() {
                    super.onPreExecute()
                    if (shouldReturn(activityReference.get())) {
                        this.cancel(true)
                    }
                }

                override fun doInBackground(vararg params: String?): String? {
                    var response: String?
                    val startDate : String = SimpleDateFormat("yyyy-MM-dd").format(today.time)
                    today.add(Calendar.DATE, 1)
                    val endDate : String = SimpleDateFormat("yyyy-MM-dd").format(today.time)

                    if (shouldReturn(activityReference.get())) {
                        this.cancel(true)
                    }

                    try {
                        response =
                            URL("https://api.weatherapi.com/v1/history.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&dt=${startDate}&end_dt=${endDate}").readText(
                                Charsets.UTF_8
                            )
                    } catch (e: Exception) {
                        response = null
                    }
                    return response
                }

                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    if (shouldReturn(activityReference.get())) {
                        this.cancel(true)
                    }
                }
            }
        }

        private fun shouldReturn(activity: Activity?): Boolean {
            if (activity == null || activity.isDestroyed) {
                return true
            }
            return false
        }
    }
}