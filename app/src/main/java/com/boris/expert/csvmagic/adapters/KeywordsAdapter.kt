package com.boris.expert.csvmagic.adapters

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.KeywordObject
import com.boris.expert.csvmagic.model.Message
import com.boris.expert.csvmagic.model.SupportTicket
import com.boris.expert.csvmagic.utils.ChoiceTouchListener
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.boris.expert.csvmagic.view.activities.SalesCustomersActivity
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import org.apmem.tools.layouts.FlowLayout
import java.util.*
import kotlin.collections.ArrayList

class KeywordsAdapter(val context: Context, val keywordsList: ArrayList<KeywordObject>) :
    RecyclerView.Adapter<KeywordsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemAddTitleClick(position: Int)
        fun onItemAddDescriptionClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var keywordView: MaterialTextView
        var addTitleView: MaterialTextView
        var addDescriptionView: MaterialTextView


        init {
            keywordView = itemView.findViewById(R.id.keyword_item_name_view)
            addTitleView = itemView.findViewById(R.id.keyword_item_add_title_view)
            addDescriptionView = itemView.findViewById(R.id.keyword_item_add_description_view)

            addTitleView.setOnClickListener {
                listener.onItemAddTitleClick(layoutPosition)
            }

            addDescriptionView.setOnClickListener {
                listener.onItemAddDescriptionClick(layoutPosition)
            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.keyword_item_row_design,
            parent,
            false
        )
        return ItemViewHolder(view,mListener!!)

    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = keywordsList[position]
        holder.keywordView.text = item.keyword

    }

    override fun getItemCount(): Int {
        return keywordsList.size
    }


}