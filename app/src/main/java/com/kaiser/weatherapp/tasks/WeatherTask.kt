package com.kaiser.weatherapp.tasks

import android.app.Activity
import android.os.AsyncTask
import com.kaiser.weatherapp.APIKey
import com.kaiser.weatherapp.ui.activities.MainActivity
import com.kaiser.weatherapp.helpers.LocationHelper
import java.lang.ref.WeakReference
import java.net.URL

class WeatherTask {
    companion object {
        class Task internal constructor(
			context: Activity
		) :
            AsyncTask<String, Void, String>() {
            private val activityReference: WeakReference<Activity> = WeakReference(context)
			var days = 1

            override fun onPreExecute() {
                super.onPreExecute()
				if (shouldReturn(activityReference.get())) { this.cancel(true) }
            }

            override fun doInBackground(vararg params: String?): String? {
                var response: String?

				if (shouldReturn(activityReference.get())) { this.cancel(true) }

                try {
                    response =
                        URL("https://api.weatherapi.com/v1/forecast.json?key=${APIKey.Key}&q=${LocationHelper.LAT},${LocationHelper.LON}&days=$days").readText(
                            Charsets.UTF_8
                        )
                } catch (e: Exception) {
                    response = null
                }
                return response
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
				if (shouldReturn(activityReference.get())) { this.cancel(true) }
            }
        }

		private fun shouldReturn(activity : Activity?) : Boolean {
			if (activity == null || activity.isDestroyed) {
				return true
			}
			return false
		}
    }
}