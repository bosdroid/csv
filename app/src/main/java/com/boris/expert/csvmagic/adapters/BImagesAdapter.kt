package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.bumptech.glide.Glide

class BImagesAdapter(private val context: Context, private val imagesList: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemDeleteClick(position: Int)
        fun onAddItemEditClick(position: Int)
        fun onImageClick(position: Int)
        fun onAddImageItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    open class ItemViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val barcodeImageView: AppCompatImageView
        val editImageView: AppCompatImageView
        val deleteImageView: AppCompatImageView

        init {
            barcodeImageView = itemView.findViewById(R.id.barcode_image_item_view)
            editImageView = itemView.findViewById(R.id.barcode_image_edit_btn)
            deleteImageView = itemView.findViewById(R.id.barcode_image_delete_btn)

            barcodeImageView.setOnClickListener {
                listener.onImageClick(layoutPosition)
            }

            editImageView.setOnClickListener {
                listener.onAddItemEditClick(layoutPosition)
            }

            deleteImageView.setOnClickListener {
                listener.onItemDeleteClick(layoutPosition)
            }
        }
    }

    class AddImageItemViewHolder(itemView: View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val addCardViewBtn: CardView = itemView.findViewById(R.id.add_image_card_view)

    }

    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == 0) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.add_image_item_layout,
                    parent,
                    false
            )
            return AddImageItemViewHolder(view, mListener!!)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.barcode_image_item_design, parent, false)
            return ItemViewHolder(view, mListener!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddImageItemViewHolder
                addViewHolder.addCardViewBtn.setOnClickListener {
                    mListener!!.onAddImageItemClick(position)
                }
            }
            else -> {
                val imageHolder = holder as ItemViewHolder
                val image = imagesList[position-1]
                Glide.with(context)
                        .load(image)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(imageHolder.barcodeImageView)
            }
        }

    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

}