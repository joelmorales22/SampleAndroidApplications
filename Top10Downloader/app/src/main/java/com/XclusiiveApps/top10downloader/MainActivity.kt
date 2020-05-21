package com.XclusiiveApps.top10downloader

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates.notNull
import kotlin.time.milliseconds

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var downloadData : DownloadData? =null

    private var feedURL :String ="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit : Int = 10

    private var feedCachedURL = "INVALIDATED"
    private val STATE_URL = "feedUrl"
    private val STATE_LIMIT = "feedLimit"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState != null)
        {
            feedURL = savedInstanceState.getString(STATE_URL,"")
            feedLimit = savedInstanceState.getInt(STATE_LIMIT)
        }
        downloadUrl(feedURL.format(feedLimit))
        Log.d(TAG,"onCreate done")

    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu,menu)
        if(feedLimit==10){
            menu?.findItem(R.id.mnu10)?.isChecked = true
        }
        else
            menu?.findItem(R.id.mnu25)?.isChecked = true
        return true
    }
    private fun downloadUrl(feedUrl:String){
        if(feedUrl != feedCachedURL) {
            Log.d(TAG, "download Url starting AsyncTask")
            downloadData = DownloadData(this, xmlListView)
            downloadData?.execute(feedUrl)
            feedCachedURL = feedUrl
            Log.d(TAG, "Download done")
        }
        else
            Log.d(TAG,"downloadUrl - URL not changed")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, feedURL)
        outState.putInt(STATE_LIMIT, feedLimit)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId){
            R.id.mnuFree->
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid->
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs->
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10,R.id.mnu25 -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(
                        TAG,
                        "opOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit"
                    )
                } else {
                    Log.d(TAG, "opOptionsItemSelected: ${item.title} setting feedLimit unchanged")
                }
            }
            R.id.refresh ->
                feedCachedURL = "INVALIDATED"

            else->
                return super.onOptionsItemSelected(item)

        }
        downloadUrl(feedURL.format(feedLimit))
        return true
    }

    companion object {
        private class DownloadData(context:Context, listView: ListView) : AsyncTask<String, Void, String>(){
            private val TAG = "DownloadData"
            var propContext : Context by notNull()
            var propListView: ListView by notNull()
            init {
                propContext = context
                propListView = listView
            }
            override fun doInBackground(vararg params: String?): String {
                Log.d(TAG, "doInBackground starts with ${params[0]}")
                val rssFeed = downloadXML(params[0])
                if(rssFeed.isEmpty())
                {
                    Log.e(TAG, "doinBackground: Error downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplication = ParseApplications()
                parseApplication.parse(result)
                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplication.applications)
                propListView.adapter = feedAdapter
            }

            private fun downloadXML(urlPath: String?): String{
                return URL(urlPath).readText()
            }

        }

    }

}
class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageURL: String =""
    override fun toString(): String {
        return """
            name=$name
            artist = $artist
            releaseDate = $releaseDate
            imageURL = $imageURL
        """.trimIndent()
    }
}