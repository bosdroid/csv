package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class ProductImagesAdapter(private val context: Context, private val productImagesList: ArrayList<ProductImages>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemEditClick(btn: MaterialButton, position: Int)
        fun onItemRemoveClick(position: Int)
        fun onItemAddImageClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    open class ItemViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val imageView: AppCompatImageView
        val editBtn: MaterialButton
        val removeBtn: AppCompatImageView

        init {
            imageView = itemView.findViewById(R.id.insales_product_image_view)
            editBtn = itemView.findViewById(R.id.insales_product_image_edit_btn)
            removeBtn = itemView.findViewById(R.id.insales_product_remove_view)

            imageView.setOnClickListener {
                listener.onItemClick(layoutPosition)
            }

            editBtn.setOnClickListener {
                listener.onItemEditClick(editBtn,layoutPosition)
            }

            removeBtn.setOnClickListener {
                listener.onItemRemoveClick(layoutPosition)
            }
        }
    }

    open class AddItemViewHolder(itemView: View, mListener: OnItemClickListener) :
            RecyclerView.ViewHolder(itemView) {
        val addViewBtn: LinearLayout = itemView.findViewById(R.id.insales_add_image_wrapper_layout)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

//        val view = LayoutInflater.from(parent.context).inflate(R.layout.insales_product_images_item_row_design, parent, false)
//        return ItemViewHolder(view, mListener!!)

        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.insales_product_add_image_design,
                    parent,
                    false
            )
            AddItemViewHolder(view, mListener!!)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.insales_product_images_item_row_design,
                    parent,
                    false
            )
            ItemViewHolder(view, mListener!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                if (productImagesList.size == 0){
                   addViewHolder.addViewBtn.setBackgroundResource(R.drawable.empty_image_background)
                }
                else{
                    addViewHolder.addViewBtn.setBackgroundResource(R.drawable.without_empty_image_background)
                }
                addViewHolder.addViewBtn.setOnClickListener {
                    mListener!!.onItemAddImageClick(position)
                }
            }
            else -> {
                val image = productImagesList[position-1]
                val viewHolder = holder as ItemViewHolder
                        Glide.with(context)
            .load(image.imageUrl)
            .thumbnail(Glide.with(context).load(R.drawable.loader))
            .fitCenter()
            .into(viewHolder.imageView)
            }
        }

    }

    override fun getItemCount(): Int {
        return productImagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == 0) viewType = 0 //if zero, it will be a header view
        return viewType
    }
}