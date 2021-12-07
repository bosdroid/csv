package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.HelpObject
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import java.util.*


class HelpsVideoAdapter(
    private val context: Context,
    val helpVideoList: ArrayList<HelpObject>,
    lifecycle: Lifecycle
) : RecyclerView.Adapter<HelpsVideoAdapter.VideoItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemFullScreenView(position: Int)

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
        var thumbnailView: AppCompatImageView

        init {

            thumbnailView = itemView.findViewById(R.id.playerview)
            typeTitle = itemView.findViewById(R.id.help_video_item_title)

            thumbnailView.setOnClickListener {
                mListener!!.onItemFullScreenView(layoutPosition)
            }

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

        Glide.with(context).load(help.thumbnail).into(holder.thumbnailView)

    }

    override fun getItemCount(): Int {
        return helpVideoList.size
    }

}