package com.example.corona_info

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.security.AccessController.getContext


class UpdateManager {

    /**
     * The Github API URL to the latest release for the repo
     */
    private val URL_LATEST_RELEASES = "https://api.github.com/repos/devasworski/Corona-Info/releases/latest"
    private val client = OkHttpClient()
    private val thisVersionCode = BuildConfig.VERSION_CODE
    private var context:Context
    private lateinit var UrlAPK:String

    constructor (context:Context){
        this.context=context
    }

    fun getLatestRelease(): String {
        val request = Request.Builder().url(URL_LATEST_RELEASES).build()
        var UrlAPK = "empty"
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {UrlAPK="NA"}
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    UrlAPK = checkForUpdates(response)
                }
                UrlAPK="NA"
            }
        })
        while (UrlAPK=="empty"){}
        return UrlAPK
    }

    private fun checkForUpdates(response: Response): String {
        val GithubLatestReleaseJSON = JSONObject(response.body?.string().toString())
        val GithubLatestReleaseVersionCode = getGithubLatestReleaseVersionCode(GithubLatestReleaseJSON)
        if(thisVersionCode < GithubLatestReleaseVersionCode) try {return downloadUpdate(GithubLatestReleaseJSON.getString("assets_url")) } catch (e: Exception){return "NA"}
        return "NA"
    }

    private fun getGithubLatestReleaseVersionCode(json: JSONObject):Int{
        val tagName = json.getString("tag_name")
        var code = (tagName.removePrefix("v")).filter {it!='.'}
        return code.toInt()
    }

    private fun downloadUpdate(assetsUrl: String): String {
        val GithubAssetsJSON = JSONArray(URL(assetsUrl).readText())
        for(i in 0 until GithubAssetsJSON.length()){
            if(GithubAssetsJSON.getJSONObject(i).getString("name").endsWith(".apk")){
                UrlAPK = GithubAssetsJSON.getJSONObject(i).getString("browser_download_url")
                return UrlAPK
                break
            }
        }
        return "NA"
    }
}