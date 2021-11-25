package com.anatame.webviewajax

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

import okhttp3.*
import retrofit2.HttpException
import java.io.IOException
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {


    lateinit var webview: WebView
    var begin: Long = 0;
    var end: Long = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webview = findViewById(R.id.mWebView)
        val textView: TextView = findViewById(R.id.textView)




        webview.webViewClient = WebViewClient()
        webview.settings.javaScriptEnabled = true
        webview?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36"
//        val map = HashMap<String, String>()
//        map.put("referer", "https://fmoviesto.cc")

        webview.loadUrl("https://fmoviesto.cc/watch-movie/venom-let-there-be-carnage-2021-full-66669.4792180")

        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT)
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setAppCachePath(applicationContext.getFilesDir().getAbsolutePath() + "/cache");
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDatabasePath(applicationContext.getFilesDir().getAbsolutePath() + "/databases");


        fun getTextWebResource(data: InputStream): WebResourceResponse {
            return WebResourceResponse("text/plain", "UTF-8", data);
        }

        webview.webViewClient = object : WebViewClient() {
//            override fun shouldInterceptRequest(
//                view: WebView,
//                request: WebResourceRequest
//            ): WebResourceResponse? {
//                if(request.url.lastPathSegment == "list.m3u8"){
//                    Log.d("webReq", request.url.path!!)
//                    Log.d("webReq", request.url.host!!)
//                    src = "https://${request.url.host}${request.url.path}"
//                }
//                return super.shouldInterceptRequest(view, request)
//            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                begin = System.currentTimeMillis()
                Log.d("webReq", begin.toString())
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {

                if (BlockHosts().hosts.contains(request!!.url.host)) {
                    val textStream: InputStream = ByteArrayInputStream("".toByteArray())
                    return getTextWebResource(textStream)
                }

//                Log.d("webReq", request.url.host.toString())

                if(request.url.lastPathSegment == "playlist.m3u8"){
                    Log.d("webReq", request.url.path!!)
                    Log.d("webReq", request.url.host!!)
                    val src = "https://${request.url.host}${request.url.path}"
                    Log.d("webReq", src)

                    val intent = Intent(this@MainActivity, MainActivity2::class.java)
                    // To pass any data to next activity
                    intent.putExtra("hlsSrc", src)
                    // start your next activity
                    startActivity(intent)
                }


                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("webReq", "finished")
                super.onPageFinished(view, url)
            }


            override fun onLoadResource(view: WebView?, url: String?) {
                if(url!!.contains("fmoviesto.cc/ajax/get_link")){
                    Log.d("webReq", url)
                    textView.setText(url)

                    view?.loadUrl("javascript:(function(){"+
                            "let timer = setInterval(function(){"+
                            "l=document.querySelector('iframe-embed');"+
                            "e=document.createEvent('HTMLEvents');"+
                            "e.initEvent('click',true,true);"+
                            "l.dispatchEvent(e);"+
                            "}, 100)" +
                            "})()")

//                    getVidSrc(url!!, view)



                    end = System.currentTimeMillis()
                    Log.d("webReq", end.toString())
                    Log.d("webReq", "total time taken ${end.minus(begin)}")
                  //  webview.visibility = View.GONE
                    val mainlayout: ConstraintLayout = findViewById(R.id.mainLayout)
                 //  mainlayout.visibility = View.VISIBLE
                }
                super.onLoadResource(view, url)
            }


        }

    }

    fun getVidSrc(url: String, webview: WebView?){

        lifecycleScope.launchWhenCreated {
            val response = try {
                RetrofitInstance.api.getVidData(url)
            } catch(e: IOException) {
                Log.e("retrofitRequest", "IOException, you might not have internet connection")
                return@launchWhenCreated
            } catch (e: HttpException) {
                Log.e("retrofitRequest", "HttpException, unexpected response")
                return@launchWhenCreated
            }
            if(response.isSuccessful && response.body() != null) {
                Log.d("retrofitRequest", response.body().toString())
                val vidSrc = response.body()!!.link
                webview?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36"
                val map = HashMap<String, String>()
                map.put("referer", "https://fmoviesto.cc")

                webview?.loadUrl(vidSrc, map)

                webview?.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {

                        if(request!!.url.lastPathSegment == "playlist.m3u8"){
                            Log.d("webReq", request.url.path!!)
                            Log.d("webReq", request.url.host!!)
                            val src = "https://${request.url.host}${request.url.path}"
                            Log.d("webReq", src)

                            end = System.currentTimeMillis()
                            Log.d("webReq", "total time taken ${end.minus(begin)}")

                        }

                        return super.shouldInterceptRequest(view, request)
                    }
                }

                // streamrapid
            } else {
                Log.e("retrofitRequest", "Response not successful")
            }
        }
    }

}

// jw-icon jw-icon-display jw-button-color jw-reset
// Play
// aria-label

//"""let timer = setInterval(function(){
//    if(document.querySelector('.jw-icon-display') != null){
//        document.querySelector('.jw-icon-display').click();
//        clearInterval(timer);
//    }
//}, 100)""







      //  https://mcloud.to/
    //  embed/
    //  k1v106?
    //  sub.info=https%3A%2F%2Ffmovies.
    //  love%
    //  2Fajax%
    //  2Fepisode%
    //  2Fsubtitles%
    //  2F81d1f4e148b94b9ec82262fec6286211%3F&autostart=true

//        https://videovard.sx/
    //        e/
    //        jhqd4gqy3kl3?
    //        subtitle_json=https%3A%2F%2Ffmovies.
    //        love%
    //        2Fajax%2
    //        Fepisode%2
    //        Fsubtitles%
    //        2F77bbbcd6c2f628aa720195c4afa01b12%3F&format=videovard
    //        &autostart=true

//        https://vwko.mcloud.to/12a3d521fb0f515df6db255ecef8ae19d373f70b15a7fdb2435a30e4e779d8c06543d4104d7e2df5a434845fa44dfc88fadbdd1b8f7dd4ebd1ecf56b0e23a292679946f30b1cbd5e4298a2bd7c452246323391251c003396d199bf0ed4a22d867916a05f5a389b29f9e73918a94540d9af7fccaa2e4f03fb9291d558c4abe9e0ce12d86289489b/r/list.m3u8


//https://fmoviesto.cc/ajax/get_link/1612515?_token=03AGdBq24WA8x1L60bl5p1UpHjL42Q7huswB8s4Qd8UJOhfB8f9G0pJPjNtONZ9wYk3tP7HI4xMygf9w6KtHGHPc9TECO5MBLE2Rhf56y_rkjLZ9qubrDDW2tDXoOnAhQpBtJzm-sWuXNm9chE3FoK9MVeeYGOf3fpOgKPsijzlUGLVGRMy6FrnTlrEWjfXk_yhSy27Jzocz3iqOZ3LrTRE_Zf9Lnh7PaJfjsjYm8Y1RI0MpOnb54Sn53dhGup4BcxR2gP8OY6NG516kQmfxdvsUczzBSXBWeliNM0pGrsvnReF5Pxi3YeG7pf8D6RZwLZI2QFQz3ibdpUIeZL3aut8SFLit3zAsK0n7vIGiAkK4QPEkNGiEIueGedWUWfLSti0wy0kWdQ-pwc7NQE-4YKJ33c6YnHEj7bs3Vbw9P3-jps0dstrtTsa8Omu1G4bDF5QRDru3aSTrpO