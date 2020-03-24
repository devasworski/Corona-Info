package com.example.corona_info

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class UpdateManager {

    /**
     * The Github API URL to the latest release for the repo
     */
    private val URL_LATEST_RELEASES = "https://api.github.com/repos/devasworski/Corona-Info/releases/latest"
    private val client = OkHttpClient()
    private val thisVersionCode = BuildConfig.VERSION_CODE
    private lateinit var context:Context

    constructor (context:Context){
        this.context=context
    }

    fun getLatestRealease(){
        val request = Request.Builder()
            .url(URL_LATEST_RELEASES)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    checkForUpdates(response)
                }
            }
        })
    }

    private fun checkForUpdates(response: Response){
        val GithubLatestReleaseJSON = JSONObject(response.body?.string().toString())
        val GithubLatestReleaseVersionCode = getGithubLatestReleaseVersionCode(GithubLatestReleaseJSON)
        if(thisVersionCode < GithubLatestReleaseVersionCode){
            downloadUpdate(GithubLatestReleaseJSON.getString("assets_url"))
        }
    }

    private fun getGithubLatestReleaseVersionCode(json: JSONObject):Int{
        val tagName = json.getString("tag_name")
        var code = (tagName.removePrefix("v")).filter {it!='.'}
        return code.toInt()
    }

    private fun downloadUpdate(assetsUrl: String){
        val GithubAssetsJSON = JSONArray(URL(assetsUrl).readText())
        for(i in 0 until GithubAssetsJSON.length()){
            if(GithubAssetsJSON.getJSONObject(i).getString("name").endsWith(".apk")){
                val UrlAPK = GithubAssetsJSON.getJSONObject(i).getString("browser_download_url")
                //TODO Ask User if he wants to download the Update
                //TODO move download in a Download Manager
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UrlAPK)))
                break
            }
        }
    }
}