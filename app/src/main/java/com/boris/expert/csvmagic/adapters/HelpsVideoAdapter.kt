package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.HelpObject
import com.google.android.material.textview.MaterialTextView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.*


class HelpsVideoAdapter(
    private val context: Context,
    val helpVideoList: ArrayList<HelpObject>,
    lifecycle: Lifecycle
) : RecyclerView.Adapter<HelpsVideoAdapter.VideoItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)

    }
    var count = 0
    private var mListener: OnItemClickListener? = null
    var lifecycle: Lifecycle? = null

    init {
        this.lifecycle = lifecycle
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    inner class VideoItemViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(
            itemView
        ) {
        var typeTitle: MaterialTextView
        var playerView: YouTubePlayerView
        var youTubePlayer: YouTubePlayer? = null
        var currentVideoId: String = ""
        private var youTubePlayerView: YouTubePlayerView? = null

        init {

            playerView = itemView.findViewById(R.id.exoplayer_view)
            typeTitle = itemView.findViewById(R.id.help_video_item_title)
            youTubePlayerView = playerView
            youTubePlayerView!!.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(initializedYouTubePlayer: YouTubePlayer) {
                    youTubePlayer = initializedYouTubePlayer
                    youTubePlayer!!.cueVideo(currentVideoId, 0F)
                }
            })

        }

        fun cueVideo(videoId: String) {
            currentVideoId = videoId
            if (youTubePlayer == null)
                return
            youTubePlayer!!.cueVideo(videoId, 0F)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.help_video_item_design,
                parent,
                false
            )

            return VideoItemViewHolder(view, mListener!!)

    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        val help = helpVideoList[position]

        holder.typeTitle.text = help.type.toUpperCase(Locale.ENGLISH)
        val parts = help.link.split("=")
        if (parts.size == 2) {
            holder.cueVideo(help.link.split("=")[1])
        }


    }

    override fun getItemCount(): Int {
        return helpVideoList.size
    }

}