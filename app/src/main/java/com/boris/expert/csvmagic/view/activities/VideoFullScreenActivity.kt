package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import androidx.core.widget.ContentLoadingProgressBar
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.boris.expert.csvmagic.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class VideoFullScreenActivity : BaseActivity() {

    private lateinit var context: Context
    private var videoId:String = ""
    private lateinit var playerView:PlayerView
    private var exoPlayer:ExoPlayer?=null
    private lateinit var progressBar:ContentLoadingProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_full_screen)

        initViews()

    }


    private fun initViews(){
        context = this
        playerView = findViewById(R.id.playerview)
        progressBar = findViewById(R.id.progress_bar)
        if (intent != null && intent.hasExtra("VIDEO_ID")){
            videoId = intent.getStringExtra("VIDEO_ID") as String

            initExoplayer()
        }


    }

    private fun initExoplayer(){
        exoPlayer = ExoPlayer.Builder(context).build()

        progressBar.show()
            object : YouTubeExtractor(this){
                override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
                    if (ytFiles != null) {
                        val itag = 22
                        val downloadUrl = ytFiles[itag].url


                        val userAgent: String = Util.getUserAgent(context, getString(R.string.app_name))
                        val defdataSourceFactory = DefaultDataSourceFactory(context, userAgent)
                        val uriOfContentUrl: Uri = Uri.parse(downloadUrl)
                        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(defdataSourceFactory)
                            .createMediaSource(uriOfContentUrl) // creating a media source

                        exoPlayer!!.prepare(mediaSource)
                        exoPlayer!!.setPlayWhenReady(true) // start loading video and play it at the moment a chunk of it is available offline
                        exoPlayer!!.addListener(object : Player.EventListener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_READY) {
                                    progressBar.hide()
                                }
                                else if (state == Player.STATE_BUFFERING){
                                    progressBar.show()
                                }
                            }
                        })

                        playerView.setPlayer(exoPlayer!!) // attach surface to the view

                    }
                }

            }.extract(videoId)


    }




    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}