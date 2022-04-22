package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter
import java.util.*


class ProductImagesSlider(
    private val context: Context,
    private val productList: ArrayList<ProductImages>
) : SliderViewAdapter<ProductImagesSlider.SliderAdapterVH>() {

      open class SliderAdapterVH(itemView: View):SliderViewAdapter.ViewHolder(itemView){

          var imageViewBackground: ImageView
          var textViewDescription: TextView? = null

          init {
              imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider)
              textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider)

          }
      }

    override fun getCount(): Int {
        return productList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderAdapterVH {
        val inflate: View =
            LayoutInflater.from(parent!!.context).inflate(R.layout.image_slider_layout_item, null)
          return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH?, position: Int) {
          val sliderItem = productList[position]
        Glide.with(viewHolder!!.itemView)
            .load(sliderItem.imageUrl)
            .fitCenter()
            .into(viewHolder.imageViewBackground)
    }

}