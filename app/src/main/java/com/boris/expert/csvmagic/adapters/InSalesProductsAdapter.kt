package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Product
import com.boris.expert.csvmagic.model.ProductImages
import com.boris.expert.csvmagic.utils.WrapContentLinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import net.expandable.ExpandableTextView
import java.util.*

class InSalesProductsAdapter(val context: Context, val productsItems: ArrayList<Product>) :
    RecyclerView.Adapter<InSalesProductsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemEditClick(position: Int, imagePosition: Int)
        fun onItemAddImageClick(position: Int)
        fun onItemRemoveClick(position: Int, imagePosition: Int)
        fun onItemEditImageClick(position: Int)
        fun onItemGrammarCheckClick(
            position: Int,
            grammarCheckBtn: AppCompatImageView,
            title: ExpandableTextView,
            description: ExpandableTextView,
            grammarStatusView: MaterialTextView
        )
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val productTitle: ExpandableTextView
        val productDescription: ExpandableTextView
        val imagesRecyclerView: RecyclerView
//        val addImageView:AppCompatImageView
        val editImageView:AppCompatImageView
        val grammarCheckView:AppCompatImageView
        val grammarStatusView:MaterialTextView
        val titleSizeView:MaterialTextView
        val descriptionSizeView:MaterialTextView
        val totalImagesView:MaterialTextView
        val collapseExpandImg:AppCompatImageView
        val collapseExpandLayout:LinearLayout
        val sliderView:SliderView
        val addProductCard: CardView

        init {
            productTitle = itemView.findViewById(R.id.insales_p_item_title)
            productDescription = itemView.findViewById(R.id.insales_p_item_description)
            imagesRecyclerView = itemView.findViewById(R.id.products_images_recyclerview)
//            addImageView = itemView.findViewById(R.id.insales_p_item_add_image)
            editImageView = itemView.findViewById(R.id.insales_p_item_edit_image)
            grammarCheckView = itemView.findViewById(R.id.grammar_check_icon_view)
            grammarStatusView = itemView.findViewById(R.id.grammar_status_textview)
            titleSizeView = itemView.findViewById(R.id.total_title_size_textview)
            descriptionSizeView = itemView.findViewById(R.id.total_description_size_textview)
            totalImagesView = itemView.findViewById(R.id.total_images_size_textview)
            collapseExpandLayout = itemView.findViewById(R.id.collapse_expand_layout)
            collapseExpandImg = itemView.findViewById(R.id.collapse_expand_img)
            sliderView = itemView.findViewById(R.id.imageSlider)
            addProductCard = itemView.findViewById(R.id.add_product_card)

            productTitle.setOnClickListener {v->
                //(v as ExpandableTextView).toggle()
                Listener.onItemClick(layoutPosition)
            }

            productDescription.setOnClickListener(View.OnClickListener { v ->
                //(v as ExpandableTextView).toggle()
            })

//            addImageView.setOnClickListener {
//                Listener.onItemAddImageClick(layoutPosition)
//            }

            collapseExpandImg.setOnClickListener {
                if (collapseExpandLayout.visibility == View.VISIBLE){
                    collapseExpandLayout.visibility = View.GONE
                    collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
                }
                else{
                    collapseExpandLayout.visibility = View.VISIBLE
                    collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
                }
            }

            addProductCard.setOnClickListener {
                Listener.onItemAddImageClick(layoutPosition)
            }

            editImageView.setOnClickListener {
                Listener.onItemEditImageClick(layoutPosition)
            }
            grammarCheckView.setOnClickListener {
                Listener.onItemGrammarCheckClick(
                    layoutPosition,
                    grammarCheckView,
                    productTitle,
                    productDescription,
                    grammarStatusView
                )
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

        holder.titleSizeView.setText("Title Size: ${item.title.length}")
        holder.descriptionSizeView.setText("Description Size: ${item.fullDesc.length}")
        holder.totalImagesView.setText("Total Images: ${item.productImages!!.size}")
        if (item.title.length > 10){
            holder.productTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }
        else{
            //holder.productTitle.text = context.getString(R.string.product_title_error)
            holder.productTitle.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_red
                )
            )
        }
//        if (item.title.length < 30){
//            ReadMoreObject.setReadMore(context,holder.productTitle,item.title,1)
//        }
//        else{
//            ReadMoreObject.setReadMore(context,holder.productTitle,item.title,2)
//        }
        holder.productTitle.setText(item.title)

        if (item.fullDesc.length > 10){
            holder.productDescription.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        }
        else{
            //holder.productDescription.text = context.getString(R.string.product_description_error)
            holder.productDescription.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_red
                )
            )
        }
//        if (item.fullDesc.length < 30){
//            ReadMoreObject.setReadMore(context,holder.productDescription,item.fullDesc,1)
//        }
//        else{
//            ReadMoreObject.setReadMore(context,holder.productDescription,item.fullDesc,5)
//        }
        holder.productDescription.setText(item.fullDesc)

//        holder.imagesRecyclerView.layoutManager =
//            WrapContentLinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//        holder.imagesRecyclerView.hasFixedSize()
//        val adapter = ProductImagesAdapter(context, item.productImages as ArrayList<ProductImages>)
//        holder.imagesRecyclerView.adapter = adapter
//        adapter.setOnItemClickListener(object : ProductImagesAdapter.OnItemClickListener {
//            override fun onItemClick(position: Int) {
//
//            }
//
//            override fun onItemEditClick(btn: MaterialButton, imagePosition: Int) {
//                mListener!!.onItemEditClick(position, imagePosition)
//            }
//
//            override fun onItemRemoveClick(imagePosition: Int) {
//                mListener!!.onItemRemoveClick(position, imagePosition)
//            }
//
//            override fun onItemAddImageClick(position: Int) {
//                mListener!!.onItemAddImageClick(position)
//            }
//
//        })
        if (item.productImages.size > 0) {
//            adapter.notifyItemRangeChanged(0, item.productImages.size)
            val sliderAdapter = ProductImagesSlider(context,item.productImages as ArrayList<ProductImages>)
            holder.sliderView.setSliderAdapter(sliderAdapter)
            holder.sliderView.setIndicatorEnabled(true)
            holder.sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
            holder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            holder.sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH;
            holder.sliderView.indicatorSelectedColor = Color.WHITE
            holder.sliderView.indicatorUnselectedColor = Color.GRAY
        } else {
//            adapter.notifyDataSetChanged()
            holder.sliderView.setIndicatorEnabled(false)
        }


    }

    override fun getItemCount(): Int {
        return productsItems.size
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}