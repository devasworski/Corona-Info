package com.example.corona_info

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okio.IOException
import org.json.JSONObject
import java.util.jar.Attributes

/**
 * the requestcode for calling the [LocationActivity] to get the current gps location
 */
private const val LOCATION_REQUEST = 101

class MainActivity : AppCompatActivity() {

    companion object Location{
        var long = "11.1"
        var lat = "49.5"
    }

    private val client = OkHttpClient()
    private lateinit var attributes: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivityForResult(Intent(this, LocationActivity::class.java), LOCATION_REQUEST)
    }

    /**
     * Loads the obtained values into the View
     */
    fun SetView(){
        country_text.text = attributes.getString("GEN")
        cases_text.text = "Fälle: ${attributes.getString("cases")}"
        death_text.text = "Todesfälle: ${attributes.getString("deaths")}"
        rate_text.text = "Sterberate: ${attributes.getString("death_rate")}%"
    }

    /**
     * called after the MapView closes
     *
     * @param requestCode contains the requestcode, which called the activity
     * @param resultCode shows if the Activity has been executed successfully
     * @param data the Nullable Intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == LOCATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                CallAPI()
            }
        }
    }

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

    fun goToInfoActivity(view: View) {}

}
