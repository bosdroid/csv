package com.boris.expert.csvmagic.adapters

import android.content.ClipData
import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.KeywordObject
import com.google.android.material.textview.MaterialTextView
import java.util.*

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
//        var addTitleView: MaterialTextView
//        var addDescriptionView: MaterialTextView


        init {
            keywordView = itemView.findViewById(R.id.keyword_item_name_view)
//            addTitleView = itemView.findViewById(R.id.keyword_item_add_title_view)
//            addDescriptionView = itemView.findViewById(R.id.keyword_item_add_description_view)

//            addTitleView.setOnClickListener {
//                listener.onItemAddTitleClick(layoutPosition)
//            }
//
//            addDescriptionView.setOnClickListener {
//                listener.onItemAddDescriptionClick(layoutPosition)
//            }

            keywordView.setOnLongClickListener(Longpress())

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.keyword_item_row_design,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)

    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = keywordsList[position]
        holder.keywordView.text = item.keyword

    }

    override fun getItemCount(): Int {
        return keywordsList.size
    }


    private class MyTouchListener : View.OnTouchListener{
        override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
            return if (motionEvent!!.getAction() == MotionEvent.ACTION_MOVE) {
                val data = ClipData.newPlainText("", "")
                val shadowBuilder = View.DragShadowBuilder(
                    view)
                view!!.startDrag(data, shadowBuilder, view, 0)

                //view.setVisibility(View.INVISIBLE);
                // Toast.makeText(context, "true", Toast.LENGTH_SHORT).show();
                // view.setOnTouchListener(null);
                true
            } else {
                //Toast.makeText(context, "false", Toast.LENGTH_SHORT).show();
                view!!.setOnTouchListener(null)
                false
            }
        }
    }



    private class Longpress : View.OnLongClickListener{
        override fun onLongClick(view: View?): Boolean {
            view!!.setOnTouchListener(MyTouchListener())

            return true
        }

    }

}