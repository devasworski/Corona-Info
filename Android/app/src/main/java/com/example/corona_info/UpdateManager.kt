package com.example.corona_info

import android.R
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat.getSystemService
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
    private lateinit var downloadManager:DownloadManager
    private var downloadId: Long = -2

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(  ctxt: Context, intent: Intent
        ) { // get the refid from the download manager
            val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            onDownloadComplete(referenceId)
        }
    }


    constructor (context:Context){
        this.context=context
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
        if(thisVersionCode < GithubLatestReleaseVersionCode) try {downloadUpdate(GithubLatestReleaseJSON.getString("assets_url")) } catch (e: Exception){ noUpdatesAvailable()}
        else noUpdatesAvailable()
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
                //TODO Ask for Write External Storage Permission
                //move download in a Download Manager
                //context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UrlAPK)))

                var downloadRequest = DownloadManager.Request(Uri.parse(UrlAPK))
                downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/Corona-Info_Update.apk")
                downloadId = downloadManager.enqueue(downloadRequest)

                break
            }
        }
    }

    private fun onDownloadComplete(did: Long){
        if(!downloadId.equals(-2)&&downloadId.equals(did)) {
            //TODO Open APK <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
            val parcelFileDescriptor = downloadManager.openDownloadedFile(downloadId)
            context.unregisterReceiver(onComplete)
        }
    }

    private fun noUpdatesAvailable(){
        context.unregisterReceiver(onComplete)
    }
}