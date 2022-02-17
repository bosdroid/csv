package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class ProductImagesAdapter(private val context: Context, private val productImagesList: ArrayList<ProductImages>) : RecyclerView.Adapter<ProductImagesAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemEditClick(btn: MaterialButton, position: Int)
        fun onItemRemoveClick(position: Int)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.insales_product_images_item_row_design, parent, false)
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val image = productImagesList[position]

        Glide.with(context)
            .load(image.imageUrl)
            .thumbnail(Glide.with(context).load(R.drawable.loader))
            .fitCenter()
            .into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return productImagesList.size
    }

}