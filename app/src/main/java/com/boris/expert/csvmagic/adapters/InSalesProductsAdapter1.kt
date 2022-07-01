package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Product
import com.boris.expert.csvmagic.model.ProductImages
import com.boris.expert.csvmagic.utils.ProductDiff
import com.boris.expert.csvmagic.utils.WrapContentLinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import net.expandable.ExpandableTextView
import java.util.*


class InSalesProductsAdapter1(val context: Context) :
    ListAdapter<Product, InSalesProductsAdapter1.ItemViewHolder>(ProductDiff()) {


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

        fun onItemGetDescriptionClick(position: Int)
        fun onItemCameraIconClick(
            position: Int,
            title: ExpandableTextView,
            description: ExpandableTextView
        )
        fun onItemImageIconClick(
            position: Int,
            title: ExpandableTextView,
            description: ExpandableTextView
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
        val editImageView: AppCompatImageView
        val grammarCheckView: AppCompatImageView
        val grammarStatusView: MaterialTextView
        val titleSizeView: MaterialTextView
        val descriptionSizeView: MaterialTextView
        val totalImagesView: MaterialTextView
        val skuBarcodeView: MaterialTextView
        val collapseExpandImg: AppCompatImageView
        val collapseExpandDescriptionImg: AppCompatImageView
        val insalesItemEditTextview:MaterialTextView
        val getDescriptionBtn: AppCompatImageView
        val cameraIconView: AppCompatImageView
        val imageIconView: AppCompatImageView
//        val titleScrollView:ScrollView
//        val descriptionScrollView:ScrollView

        init {
            productTitle = itemView.findViewById(R.id.insales_p_item_title)
            productDescription = itemView.findViewById(R.id.insales_p_item_description)
            imagesRecyclerView = itemView.findViewById(R.id.products_images_recyclerview)
            editImageView = itemView.findViewById(R.id.insales_p_item_edit_image)
            insalesItemEditTextview = itemView.findViewById(R.id.insales_item_edit_textview)
            grammarCheckView = itemView.findViewById(R.id.grammar_check_icon_view)
            grammarStatusView = itemView.findViewById(R.id.grammar_status_textview)
            titleSizeView = itemView.findViewById(R.id.total_title_size_textview)
            skuBarcodeView = itemView.findViewById(R.id.sku_barcode_textview)
            descriptionSizeView = itemView.findViewById(R.id.total_description_size_textview)
            totalImagesView = itemView.findViewById(R.id.total_images_size_textview)
            collapseExpandImg = itemView.findViewById(R.id.collapse_expand_img)
            collapseExpandDescriptionImg = itemView.findViewById(R.id.collapse_expand_description_img)
            getDescriptionBtn = itemView.findViewById(R.id.get_description_text_view)
            cameraIconView = itemView.findViewById(R.id.insales_item_photo_icon_view)
            imageIconView = itemView.findViewById(R.id.insales_item_image_icon_view)
//            titleScrollView = itemView.findViewById(R.id.title_scrollbar)
//            descriptionScrollView = itemView.findViewById(R.id.description_scrollbar)

//            titleScrollView.setOnTouchListener(OnTouchListener { v, event -> // Disallow the touch request for parent scroll on touch of child view
//                val isLarger: Boolean
//                isLarger = (v as ExpandableTextView).lineCount * v.lineHeight > v.getHeight()
//                if (event.action === MotionEvent.ACTION_MOVE && isLarger) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true)
//                } else {
//                    v.getParent().requestDisallowInterceptTouchEvent(false)
//                }
//                return@OnTouchListener false
//            })
//
//            descriptionScrollView.setOnTouchListener(OnTouchListener { v, event -> // Disallow the touch request for parent scroll on touch of child view
//                val isLarger: Boolean
//                isLarger = (v as ExpandableTextView).lineCount * v.lineHeight > v.getHeight()
//                if (event.action === MotionEvent.ACTION_MOVE && isLarger) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true)
//                } else {
//                    v.getParent().requestDisallowInterceptTouchEvent(false)
//                }
//                return@OnTouchListener false
//            })

            productTitle.setOnClickListener { v ->
                Listener.onItemClick(layoutPosition)
            }

            productDescription.setOnClickListener(View.OnClickListener { v ->

            })


            editImageView.setOnClickListener {
                Listener.onItemEditImageClick(layoutPosition)
            }

            insalesItemEditTextview.setOnClickListener {
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

            getDescriptionBtn.setOnClickListener {
                Listener.onItemGetDescriptionClick(layoutPosition)
            }

            cameraIconView.setOnClickListener {
                Listener.onItemCameraIconClick(layoutPosition, productTitle, productDescription)
            }
            imageIconView.setOnClickListener {
                Listener.onItemImageIconClick(layoutPosition, productTitle, productDescription)
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

        val item = getItem(position)

        if (item.sku.isEmpty()){
            holder.skuBarcodeView.setText("Sku/Barcode: None")
        }
        else{
            holder.skuBarcodeView.setText("Sku/Barcode: ${item.sku}")
        }

        holder.titleSizeView.setText("Title Size: ${item.title.length}")
        holder.descriptionSizeView.setText("Description Size: ${item.fullDesc.length}")
        holder.totalImagesView.setText("Total Images: ${item.productImages!!.size}")
        if (item.title.length > 10) {
            holder.productTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        } else {
            //holder.productTitle.text = context.getString(R.string.product_title_error)
            holder.productTitle.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_red
                )
            )
        }

        holder.productTitle.text = item.title
        holder.productTitle.isExpanded = false
        holder.productTitle.movementMethod = ScrollingMovementMethod.getInstance()

        if (item.fullDesc.length > 10) {
            holder.productDescription.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        } else {
            //holder.productDescription.text = context.getString(R.string.product_description_error)
            holder.productDescription.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_red
                )
            )
        }

        holder.productDescription.text = item.fullDesc
        holder.productDescription.isExpanded = false
        holder.productDescription.movementMethod = ScrollingMovementMethod.getInstance()

        holder.imagesRecyclerView.layoutManager =
            WrapContentLinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.imagesRecyclerView.hasFixedSize()
        val adapter = ProductImagesAdapter(context, item.productImages as ArrayList<ProductImages>)
        holder.imagesRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : ProductImagesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

            }

            override fun onItemEditClick(btn: MaterialButton, imagePosition: Int) {
                mListener!!.onItemEditClick(position, imagePosition)
            }

            override fun onItemRemoveClick(imagePosition: Int) {
                mListener!!.onItemRemoveClick(position, imagePosition)
            }

            override fun onItemAddImageClick(pos: Int) {
                mListener!!.onItemAddImageClick(position)
            }

        })
        if (item.productImages.size > 0) {
            adapter.notifyItemRangeChanged(0, item.productImages.size)
//            val sliderAdapter = ProductImagesSlider(context,item.productImages as ArrayList<ProductImages>)
//            holder.sliderView.setSliderAdapter(sliderAdapter)
//            holder.sliderView.setIndicatorEnabled(true)
//            holder.sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
//            holder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
//            holder.sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH;
//            holder.sliderView.indicatorSelectedColor = Color.WHITE
//            holder.sliderView.indicatorUnselectedColor = Color.GRAY
        } else {
            adapter.notifyDataSetChanged()
//            holder.sliderView.setIndicatorEnabled(false)
        }

        holder.collapseExpandImg.setOnClickListener {
            if (holder.productTitle.isExpanded) {
                holder.productTitle.isExpanded = false
//                    collapseExpandLayout.visibility = View.GONE
                holder.collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
            } else {
                holder.productTitle.isExpanded = true
//                    collapseExpandLayout.visibility = View.VISIBLE
                holder.collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
            }
        }

        holder.collapseExpandDescriptionImg.setOnClickListener {
            if (holder.productDescription.isExpanded) {
                holder.productDescription.isExpanded = false
//                    collapseExpandLayout.visibility = View.GONE
                holder.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_down)
            } else {
                holder.productDescription.isExpanded = true
//                    collapseExpandLayout.visibility = View.VISIBLE
                holder.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_up)
            }
        }

        holder.productTitle.setOnExpandableClickListener(
            onExpand = { // Expand action
                holder.collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
            },
            onCollapse = { // Collapse action
                holder.collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
            }
        )

        holder.productDescription.setOnExpandableClickListener(
            onExpand = { // Expand action
                holder.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_up)
            },
            onCollapse = { // Collapse action
                holder.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_down)
            }
        )


    }

//    override fun getItemCount(): Int {
//        return productsItems.size
//    }
//
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return position
//    }


}