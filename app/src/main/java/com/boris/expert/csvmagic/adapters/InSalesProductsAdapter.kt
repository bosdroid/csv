package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Product
import com.boris.expert.csvmagic.model.ProductImages
import com.boris.expert.csvmagic.utils.ReadMoreObject
import com.boris.expert.csvmagic.utils.WrapContentLinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

class InSalesProductsAdapter(val context: Context, val productsItems: ArrayList<Product>) :
    RecyclerView.Adapter<InSalesProductsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemEditClick(position: Int,imagePosition:Int)
        fun onItemAddImageClick(position: Int)
        fun onItemRemoveClick(position: Int,imagePosition:Int)
        fun onItemEditImageClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val productTitle: MaterialTextView
        val productDescription: MaterialTextView
        val imagesRecyclerView: RecyclerView
//        val addImageView:AppCompatImageView
        val editImageView:AppCompatImageView

        init {
            productTitle = itemView.findViewById(R.id.insales_p_item_title)
            productDescription = itemView.findViewById(R.id.insales_p_item_description)
            imagesRecyclerView = itemView.findViewById(R.id.products_images_recyclerview)
//            addImageView = itemView.findViewById(R.id.insales_p_item_add_image)
            editImageView = itemView.findViewById(R.id.insales_p_item_edit_image)

            productTitle.setOnClickListener {
                Listener.onItemClick(layoutPosition)
            }

//            addImageView.setOnClickListener {
//                Listener.onItemAddImageClick(layoutPosition)
//            }

            editImageView.setOnClickListener {
                Listener.onItemEditImageClick(layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.insales_products_item_row_design,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = productsItems[position]

        if (item.title.length > 10){
            holder.productTitle.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
        }
        else{
            //holder.productTitle.text = context.getString(R.string.product_title_error)
            holder.productTitle.setBackgroundColor(ContextCompat.getColor(context,R.color.light_red))
        }
        if (item.title.length < 30){
            ReadMoreObject.setReadMore(context,holder.productTitle,item.title,1)
        }
        else{
            ReadMoreObject.setReadMore(context,holder.productTitle,item.title,2)
        }

        if (item.fullDesc.length > 10){
            holder.productDescription.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
        }
        else{
            //holder.productDescription.text = context.getString(R.string.product_description_error)
            holder.productDescription.setBackgroundColor(ContextCompat.getColor(context,R.color.light_red))
        }
        if (item.fullDesc.length < 30){
            ReadMoreObject.setReadMore(context,holder.productDescription,item.fullDesc,1)
        }
        else{
            ReadMoreObject.setReadMore(context,holder.productDescription,item.fullDesc,5)
        }

        holder.imagesRecyclerView.layoutManager =
            WrapContentLinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.imagesRecyclerView.hasFixedSize()
        val adapter = ProductImagesAdapter(context, item.productImages as ArrayList<ProductImages>)
        holder.imagesRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : ProductImagesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

            }

            override fun onItemEditClick(btn: MaterialButton, imagePosition: Int) {
               mListener!!.onItemEditClick(position,imagePosition)
            }

            override fun onItemRemoveClick(imagePosition: Int) {
                mListener!!.onItemRemoveClick(position,imagePosition)
            }

            override fun onItemAddImageClick(position: Int) {
                mListener!!.onItemAddImageClick(position)
            }

        })
        if (item.productImages.size > 0) {
            adapter.notifyItemRangeChanged(0, item.productImages.size)
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return productsItems.size
    }

}