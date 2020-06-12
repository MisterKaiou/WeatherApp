package com.kaiser.weatherapp.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.*

/**
 * Manages everything related to location, checks for permission before executing anything
 * @param activity the activity from which this class has been instantiated
 */
class LocationHelper(private var activity: Activity) {
    companion object {

        /**
         * Last known latitude
         */
        var LAT = "51,476860"

        /**
         * Last known longitude
         */
        var LON = "-0,000499"

        /**
         * Last time un update has been executed
         */
        var lastUpdate = ""
    }

    /**
     * Location Provider, Google Location Services since it's now recommended practice.
     */
    private var mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    /**
     * Responsible for using the location provider to get current user location
     * @return The status of the last execution
     */
    fun getLastLocation() : LocationHelperStatus {
        return if (checkPermissions()) {
            if (isLocationEnabled()) {
                getCoordinates()
                LocationHelperStatus.OK
            } else {
                LocationHelperStatus.LocationUnabled
            }
        } else {
            LocationHelperStatus.NoPermission
        }
    }

    /**
     * Gets the user last Coordinates, request a new coordinate on every update and updates
     * LAT and LON with the result
     */
    @SuppressLint("MissingPermission")
    private fun getCoordinates() {
        mFusedLocationClient.lastLocation.addOnSuccessListener(activity) { location ->
            if (location == null) {
                requestNewLocationData()
            } else {
                requestNewLocationData()
                LAT = location.latitude.toString()
                LON = location.longitude.toString()
            }
        }
    }

    /**
     * Request new coordinates, and updates LAT and LON with the results
     */
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallBack,
            Looper.myLooper()
        )
    }

    /**
     * Callback used when a new location update is received
     */
    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            LAT = mLastLocation.latitude.toString()
            LON = mLastLocation.longitude.toString()
        }
    }

    /**
     * Checks if GPS or Internet is enabled
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager? =
            getSystemService(activity, LocationManager::class.java)

        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!! || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * Checks if user has granted permission for accessing his current location
     */
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }
}

/**
 * Possible return status from LocationHelper Class
 */
enum class LocationHelperStatus {
    NoPermission,
    LocationUnabled,
    OK
}
