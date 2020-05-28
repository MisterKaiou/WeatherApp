package com.kaiser.weatherapp.tasks

import android.os.AsyncTask
import android.view.View
import androidx.core.view.isVisible
import com.kaiser.weatherapp.APIKey
import com.kaiser.weatherapp.databinding.ActivityMainBinding
import com.kaiser.weatherapp.extensions.formatToDate
import com.kaiser.weatherapp.helpers.LocationHelper
import com.kaiser.weatherapp.helpers.LocationHelper.Companion.lastUpdate
import org.json.JSONObject
import java.net.URL

class WeatherTask(var binding: ActivityMainBinding) : AsyncTask<String, Void, String>() {
	var days = 1
	
	override fun doInBackground(vararg params: String?): String? {
		var response: String?
		
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
			val updatetAt = current.getLong("last_updated_epoch")
			val updatedAtText = "Update At: " + updatetAt.formatToDate()
			lastUpdate = updatetAt.toString()
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
			binding.loader.isVisible = false
			binding.mainContainer.isVisible = true
			
			
		} catch (e: Exception) {
			null
		}
	}
}