package com.anatame.webviewajax

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.anatame.webviewajax.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector




class MainActivity2 : AppCompatActivity() {

    var HI_BITRATE = 4500000

    private var mPlayer: SimpleExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var hlsUrl =
        "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        goFullScreen()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        playerView = findViewById(R.id.playerView)
        val message = intent.getStringExtra("hlsSrc")
        hlsUrl = message!!

    }

    private fun initPlayer() {
        val trackSelector = DefaultTrackSelector()
        val defaultTrackParam = trackSelector.buildUponParameters().build()
        trackSelector.parameters = defaultTrackParam

        mPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
        // Bind the player to the view.
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerView.player = mPlayer
        mPlayer!!.playWhenReady = true
        mPlayer!!.seekTo(playbackPosition)
        mPlayer!!.prepare(buildMediaSource(), false, false)

        val parameters = trackSelector.buildUponParameters()
            .setMaxVideoBitrate(HI_BITRATE)
            .setForceHighestSupportedBitrate(true)
            .build()
        trackSelector.setParameters(parameters)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || mPlayer == null) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }


    private fun releasePlayer() {
        if (mPlayer == null) {
            return
        }
        playWhenReady = mPlayer!!.playWhenReady
        playbackPosition = mPlayer!!.currentPosition
        currentWindow = mPlayer!!.currentWindowIndex
        mPlayer!!.release()
        mPlayer = null
    }

    private fun buildMediaSource():MediaSource{
        val userAgent = Util.getUserAgent(playerView.context, playerView.context.getString(R.string.app_name))

        // Create a data source factory.
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        // Create a HLS media source pointing to a playlist uri.
        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(hlsUrl))

        return  hlsMediaSource

    }

    private fun goFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

    }




}