package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Sheet
import com.boris.expert.csvmagic.model.SupportTicket
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class TicketsAdapter(val context: Context, val ticketList: ArrayList<SupportTicket>) :
    RecyclerView.Adapter<TicketsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemStatusDropDown(position: Int,status:String)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var titleView: MaterialTextView
        var messageView: MaterialTextView
        //var statusView: MaterialTextView
        var statusSpinner: AppCompatSpinner
        var lastReplyView:MaterialTextView
        var lastReplyByView:MaterialTextView
        var ticketDateView:MaterialTextView

        init {
            titleView = itemView.findViewById(R.id.ticket_item_title_view)
            messageView = itemView.findViewById(R.id.ticket_item_message_view)
            //statusView = itemView.findViewById(R.id.ticket_item_status_view)
            statusSpinner = itemView.findViewById(R.id.ticket_item_status_spinner)
            lastReplyView = itemView.findViewById(R.id.ticket_last_reply_view)
            lastReplyByView = itemView.findViewById(R.id.ticket_last_reply_by_view)
            ticketDateView = itemView.findViewById(R.id.ticket_item_date_view)


            itemView.setOnClickListener {
                Listener.onItemClick(layoutPosition)
            }

            statusSpinner.setSelection(0,false)
            statusSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    //if (view!!.isPressed){
                    val value: String = parent!!.getItemAtPosition(position).toString()
                    Listener.onItemStatusDropDown(layoutPosition,value)
                    //}
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

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
        when(item.status.toLowerCase(Locale.ENGLISH)){
            "open"->{
                holder.statusSpinner.setSelection(0)
            }
            "pending"->{
                holder.statusSpinner.setSelection(1)
            }
            "closed"->{
                holder.statusSpinner.setSelection(2)
            }
            else->{

            }
        }
        //holder.statusView.text = item.status.toUpperCase(Locale.ENGLISH)
        holder.ticketDateView.text = BaseActivity.getDateTimeFromTimeStamp(item.timeStamp)
        if (item.lastReply.toInt() == 0){
            holder.lastReplyView.text = "Last reply on: N/A"
        }
        else{
            holder.lastReplyView.text = "Last reply on: ${BaseActivity.getDateTimeFromTimeStamp(item.lastReply)}"
        }

        if (item.lastReplyBy.isEmpty()){
            holder.lastReplyByView.text = "Last reply by: N/A"
        }
        else{
            holder.lastReplyByView.text = "Last reply by: ${item.lastReplyBy}"
        }

        if (item.status == "closed"){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.light_gray))
            holder.itemView.isEnabled = false
            holder.statusSpinner.isEnabled = false
        }

    }

    override fun getItemCount(): Int {
        return ticketList.size
    }

}