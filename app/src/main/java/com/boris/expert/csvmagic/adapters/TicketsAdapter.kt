package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Sheet
import com.boris.expert.csvmagic.model.SupportTicket
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class TicketsAdapter(val context: Context, val ticketList: ArrayList<SupportTicket>) :
    RecyclerView.Adapter<TicketsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var titleView: MaterialTextView
        var messageView: MaterialTextView
        var statusView: MaterialTextView

        init {
            titleView = itemView.findViewById(R.id.ticket_item_title_view)
            messageView = itemView.findViewById(R.id.ticket_item_message_view)
            statusView = itemView.findViewById(R.id.ticket_item_status_view)

            itemView.setOnClickListener {
                Listener.onItemClick(layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.ticket_item_row_design,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = ticketList[position]
        holder.titleView.text = item.title
        holder.messageView.text = item.message
        holder.statusView.text = item.status.toUpperCase(Locale.ENGLISH)

    }

    override fun getItemCount(): Int {
        return ticketList.size
    }

}