package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R

class SwipeAdapter(private val context: Context,private val data:ArrayList<Int>) :BaseAdapter(){



    open class ItemViewHolder(itemView:View) :RecyclerView.ViewHolder(itemView){

    }

    override fun getCount(): Int {
       return 5
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(parent!!.context).inflate(R.layout.ap_item_koloda, parent, false)
        }

        return view!!
    }


}