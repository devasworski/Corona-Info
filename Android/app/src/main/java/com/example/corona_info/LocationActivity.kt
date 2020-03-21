package com.example.corona_info

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import kotlinx.android.synthetic.main.activity_location.*

/**
 * the requestcode for calling the permission to access the gps location
 */
private const val PERMISSION_REQUEST = 10

/**
 * Activtiy where the location of the IoT Device can be selected on a Map assited by the current GPS location
 *
 * TODO E/Mbgl-NativeMapView: You're calling `cancelTransitions` after the `MapView` was destroyed, were you invoking it after `onDestroy()`? You're calling `flyTo` after the `MapView` was destroyed, were you invoking it after `onDestroy()`?
 *
 */
@Suppress("UNUSED_PARAMETER")
class LocationActivity : AppCompatActivity() {
    /**
     * Location Mangager, listens to current Location
     */
    private lateinit var locationManager: LocationManager
    /**
     * Boolean if GPS Signal is availiabe
     */
    private var hasGps = false
    /**
     * Array of permissions required in this Activity
     */
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    /**
     * Boolean inizial location for the device has been found and map has been inizialised
     */
    private var inizialised: Boolean = false
    /**
     * Mapview
     */
    private lateinit var mapView: MapView
    /**
     * Mapbox Map element
     */
    private lateinit var map: MapboxMap
    /**
     * Boolean if the Marker and Map Camera should follow the location of the Phone
     */
    private var followLocation: Boolean = true
    /**
     * Manages the Marker on the Map
     */
    private lateinit var markerViewManager: MarkerViewManager
    /**
     * Currently selected Location on the Map
     */
    private lateinit var selectedLocation: Location
    /**
     * the Location of this device
     */
    private lateinit var myLocation: Location
    /**
     * Marker on the Map
     */
    private var marker: MarkerView? = null

    /**
     * Creates the Activity [GetLocationActivity]
     * Asks for the permission to access the GPS information
     * Loads the Mapbox Map into the [mapView]
     * @param savedInstanceState containing the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_location)

        if (checkPermission(permissions)) { getLocation() } else { requestPermissions(permissions, PERMISSION_REQUEST) }

        //region MapBox

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            }
            map = mapboxMap
            markerViewManager = MarkerViewManager(mapView, map)
            map.addOnMapClickListener { point ->
                onMapClick(point)
                true
            }
        }

    }

    //region MapBox Lifecycle
    /**
     * Lifecycle method for [mapView]
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    /**
     * Lifecycle method for [mapView]
     */
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    /**
     * Lifecycle method for [mapView]
     */
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    /**
     * Lifecycle method for [mapView]
     * @param outState the saved InstanceState
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    /**
     * Lifecycle method for [mapView]
     */
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /**
     * Lifecycle method for [mapView]
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    //endregion

    //region Mapbox

    /**
     * Added the Location Marker to the Map
     * @param latitude The latitude where the marker should appear
     * @param longitude The longitude where the marker should appear
     */
    private fun inizialiseMarker(latitude: Double,longitude: Double) {
        //https://github.com/mapbox/mapbox-plugins-android/blob/master/app/src/main/java/com/mapbox/mapboxsdk/plugins/testapp/activity/markerview/MarkerViewActivity.kt
        markerViewManager.let {
            val imageView = ImageView(this)
            imageView.setImageResource(R.drawable.pin_icon)
            imageView.layoutParams = FrameLayout.LayoutParams(56, 56)
            marker = MarkerView(LatLng(latitude,longitude), imageView)
            marker?.let {
                markerViewManager.addMarker(it)
            }
        }
    }

    /**
     * Updates the position of the Location Marker on the Map
     * @param latitude The latitude where the marker should appear
     * @param longitude The longitude where the marker should appear
     */
    private fun updateSelectedLocation(latitude: Double,longitude: Double){
        selectedLocation.longitude=longitude
        selectedLocation.latitude=latitude
    }

    /**
     * Updates the currently selected Location [selectedLocation] on the Map
     * @param location the selected Location
     */
    private fun updateSelectedLocation(location: Location){
        selectedLocation=location
    }

    /**
     * Intent called by [goToMyLocation], which updates the camera position on the [map] by a Location
     * @param location the selected location
     */
    private fun setCameraPosition(location:Location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),18.0))
        updateSelectedLocation(location)
    }

    /**
     * Intent called by [onMapClick], which updates the camera position on the [map] by latitude and longitude
     * @param latitude The latitude of the new camera position
     * @param longitude The longitude of the new camera position
     */
    private fun setCameraPosition(latitude: Double,longitude: Double){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude,longitude),18.0))
        updateSelectedLocation(latitude,longitude)
    }

    /**
     * Intent called by [mapView] when clicked on the map
     * @param point The point where clicked on the map
     */
    private fun onMapClick(point: LatLng){
        if(inizialised) {
            followLocation = false
            setCameraPosition(point.latitude, point.longitude)
            marker?.setLatLng(point)
        }
    }

    /**
     * Intent called by the mylocation_btn
     * Set [followLocation] to true
     * Calls [setCameraPosition] and set the camera position the gps location
     * @param view the View Element, that calls this Method
     */
    fun goToMyLocation(view: View) {
        followLocation = true
        setCameraPosition(myLocation)
    }

    //endregion


    //region GetLocation
    /**
     * intent called [getLocation] on the onLocationChanged intent
     * Actives the btn_usepin, mylocation_btn and deactivates the progressBar and textView_searching
     * Set [myLocation] to [location]
     * If [followLocation] si true, the [setCameraPosition] will be called and set the camera position to [myLocation]
     * @param location the  current gps location
     */
    private fun onGotLocation(location: Location) {
        progressBar.isVisible=false
        textView_searching.isVisible=false
        myLocation = location
        btn_usepin.isEnabled=true
        btn_usepin.alpha=1.0f
        mylocation_btn.isEnabled=true
        mylocation_btn.alpha=1.0f
        if(!inizialised)
            inizialiseMarker(location.latitude,location.longitude)
        inizialised=true
        if(followLocation) {
            setCameraPosition(location)
        }
    }

    /**
     * Called when the Activity has an executing Error or the System Return Button has been clicked
     */
    private fun onError(){
        this.setResult(Activity.RESULT_CANCELED)
        this.finish()
    }

    /**
     * Called when exit_btn clicked and closes the intent and goes back to [AddDeviceActivity]
     * @param view the View Element, that calls this Method
     */
    fun leave(view: View) {
        this.setResult(Activity.RESULT_CANCELED)
        this.finish()
    }

    /**
     * Starts the Location listener
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            onGotLocation(location)
                        }
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { onError() }
                    override fun onProviderEnabled(provider: String?) { onError() }
                    override fun onProviderDisabled(provider: String?) { onError() }
                })

        } else {
            Toast.makeText(this, "GPS Deactivated", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    /**
     * Checks the system permissions
     * @param permissionArray permissions that should be checked
     * @return true if all permissions set, false is not all permission have been given
     */
    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    /**
     * Intent called when the permission request has been executed.
     * Checks is all permissions have been given and then calls [getLocation]
     * Otherwise exits this Activity with resultcode RESULT.CANCELED
     * @param requestCode [PERMISSION_REQUEST]
     * @param permissions The permissions requested
     * @param grantResults the permission request results
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    this.setResult(Activity.RESULT_CANCELED)
                    this.finish()
                }
            }
            if (allSuccess)
                getLocation()

        }
    }
    //endregion

    /**
     * Set the [selectedLocation] as [IoTDeviceInfos.location]
     * Exits the Activity with resultcode RESULT_OK
     * @param view the View Element, that calls this Method
     */
    fun setLocation(view: View) {
        MainActivity.lat=selectedLocation.latitude.toString()
        MainActivity.long=selectedLocation.longitude.toString()
        this.setResult(Activity.RESULT_OK)
        this.finish()
    }

}