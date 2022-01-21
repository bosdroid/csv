package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class InternetImageAdapter(private val context: Context, private val imagesList: ArrayList<String>) : RecyclerView.Adapter<InternetImageAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemAttachClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    open class ItemViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val imageView: AppCompatImageView
        val attachBtn:MaterialButton

        init {
            imageView = itemView.findViewById(R.id.search_image_view)
            attachBtn = itemView.findViewById(R.id.search_image_attach_btn)

            imageView.setOnClickListener {
                listener.onItemClick(layoutPosition)
            }

            attachBtn.setOnClickListener {
                listener.onItemAttachClick(layoutPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_image_item_row_design, parent, false)
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val image = imagesList[position]
        Glide.with(context)
                .load(image)
                .placeholder(R.drawable.placeholder)
                .centerInside()
                .into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

}