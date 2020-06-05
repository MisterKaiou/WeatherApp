package com.kaiser.weatherapp.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.location.*
import com.kaiser.weatherapp.R

class LocationHelper(
		private var activity: Activity, private var mFusedLocationClient : FusedLocationProviderClient
) {
	companion object {
		var LAT = "51,476860"
		var LON = "-0,000499"
		var lastUpdate = ""
	}
	
	private var context: Context = activity.baseContext

	fun getLastLocation(): Boolean {
		if (checkPermissions()) {
			return if (isLocationEnabled()) {
				getCoordinates()
				true
			} else {
				Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show()
				val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
				startActivity(context, intent, null)
				false
			}
		} else {
			throw SecurityException(R.string.LocationError_Permission.toString())
		}
	}
	
	private fun getCoordinates() {
		mFusedLocationClient.lastLocation.addOnSuccessListener(activity) { location ->
			if (location == null) {
				requestNewLocationData()
			} else {
				LAT = location.latitude.toString()
				LON = location.longitude.toString()
                requestNewLocationData()
			}
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun requestNewLocationData() {
		val mLocationRequest = LocationRequest()
		mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
		mFusedLocationClient.requestLocationUpdates(
				mLocationRequest, mLocationCallBack,
				Looper.myLooper()
		)
	}
	
	private val mLocationCallBack = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult) {
			val mLastLocation: Location = locationResult.lastLocation
			LAT = mLastLocation.latitude.toString()
			LON = mLastLocation.longitude.toString()
		}
	}
	
	private fun isLocationEnabled(): Boolean {
		val locationManager: LocationManager? =
				getSystemService(context, LocationManager::class.java)
		
		return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!! || locationManager.isProviderEnabled(
				LocationManager.NETWORK_PROVIDER
		)
	}
	
	private fun checkPermissions(): Boolean {
		if (ActivityCompat.checkSelfPermission(
						context,
						Manifest.permission.ACCESS_COARSE_LOCATION
				) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(
						context,
						Manifest.permission.ACCESS_FINE_LOCATION
				) == PackageManager.PERMISSION_GRANTED
		) {
			return true
		}
		
		return false
	}
}