package com.example.corona_info

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okio.IOException
import org.json.JSONObject
import java.util.*

/**
 * the requestcode for calling the permission to access the gps location
 */
private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity() {

    /**
     * Array of permissions required in this Activity
     */
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private val client = OkHttpClient()
    private lateinit var attributes: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkPermission(permissions)) {
            getLocation()
        }
        else
        { requestPermissions(permissions, PERMISSION_REQUEST) }
        checkForUpdate()
    }

    //region API

    /**
     * Calls the API from [https://npgeo-corona-npgeo-de.hub.arcgis.com/]
     */
    fun CallAPI() {


        var Uri =
            "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?where=1%3D1&outFields=death_rate,cases,deaths,cases_per_100k,cases_per_population,GEN,BEZ,county,BL&geometry=${long}%2C${lat}&geometryType=esriGeometryPoint&inSR=4326&spatialRel=esriSpatialRelIntersects&outSR=4326&f=json"


        val request = Request.Builder()
            .url(Uri)
            .build()

        var loading = 1

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loading = 2
            }

            override fun onResponse(call: Call, response: Response) {
                loading = if (response.code == 200) {
                    interpretJSON(JSONObject(response.body?.string().toString()))
                    0
                } else
                    3
            }
        })
        while (loading == 1) {
        }
        SetView()
    }

    /**
     * Interpretes the Response JSON
     *
     * @param json response JSON Object
     */
    fun interpretJSON(json: JSONObject){
        var features = json.getJSONArray("features")
        var featuresarray = features.getJSONObject(0)
        var attributes = featuresarray.getJSONObject("attributes")
        this.attributes = attributes
    }


    //endregion

    //region Updates

    fun checkForUpdate(){
        val downloadUrl = UpdateManager(this).getLatestRelease()
        if(downloadUrl!="NA")
            openUpdatePopup(downloadUrl)
    }

    fun openUpdatePopup(UrlAPK:String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Es ist ein Update verfügbar")
        builder.setMessage("Soll das Update installiert werden?")
        builder.setPositiveButton("Installieren") { dialog, _ ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UrlAPK)))
        }
        builder.setNegativeButton("Später") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()

    }


    //endregion

    //region UI

    /**
     * Loads the obtained values into the View
     */
    fun SetView(){
        country_text.text = attributes.getString("GEN")
        cases_text.text = "Fälle: ${attributes.getString("cases")}"
        death_text.text = "Todesfälle: ${attributes.getString("deaths")}"
        // death rate may be to long to be displayed. Therfore it will be trimmed to show max 6 chars (one character is the dot)
        var deathRateOriginal = attributes.getString("death_rate")
        var deathRate = if((deathRateOriginal.length > 6)) { "~${deathRateOriginal.substring(0,6)}" }else{ deathRateOriginal}
        rate_text.text = "Sterberate: ${deathRate}%"
        progressBar.isVisible=false
    }

    fun goToInfoActivity(view: View) {
        startActivity(Intent(this,InfoActivity::class.java))
    }


    //endregion

    //region Location
    companion object Location{
        var long = "11.1"
        var lat = "49.5"
    }

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient


    private fun getLocation(){
        progressBar.isVisible = true
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location : android.location.Location? ->
            if (location != null) {
                lat = location.latitude.toString()
                long = location.longitude.toString()
                CallAPI()
            } else{
                Toast.makeText(this, "GPS Deactivated", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    //endregion

    //region Permissions

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

    //region Lifecycle

    //endregion


}
