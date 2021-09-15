package com.boris.expert.csvmagic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.model.Sheet
import com.google.android.material.textview.MaterialTextView

class FeaturesAdapter(val context: Context, val featuresList: ArrayList<Feature>) :
    RecyclerView.Adapter<FeaturesAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemPurchaseBtnClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
            var featureNameView:MaterialTextView
            var memoryUsageView:MaterialTextView
            var timeDuration:MaterialTextView
        var memoryUsageHeadingView:MaterialTextView
        var timeDurationHeading:MaterialTextView
            var creditPriceView:MaterialTextView
            var purchaseBtn:AppCompatButton

            init {
                featureNameView = itemView.findViewById(R.id.feature_name_item_view)
                memoryUsageView = itemView.findViewById(R.id.memory_usage_size_view)
                timeDuration = itemView.findViewById(R.id.time_duration_view)
                memoryUsageHeadingView = itemView.findViewById(R.id.memory_usage_size_heading_view)
                timeDurationHeading = itemView.findViewById(R.id.time_duration_heading_view)
                creditPriceView = itemView.findViewById(R.id.credit_price_view)
                purchaseBtn = itemView.findViewById(R.id.purchase_package_btn)

                purchaseBtn.setOnClickListener {
                    Listener.onItemPurchaseBtnClick(layoutPosition)
                }

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.feature_item_row_design,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val feature = featuresList[position]
        holder.featureNameView.text = feature.name
        if (feature.name.contains("storage")){
            holder.memoryUsageView.text = "${feature.memory} MB"
            holder.timeDuration.visibility = View.GONE
            holder.timeDurationHeading.visibility = View.GONE
        }

        if (feature.name.contains("time")){
            holder.timeDuration.text = "${feature.duration} Days"
            holder.memoryUsageView.visibility = View.GONE
            holder.memoryUsageHeadingView.visibility = View.GONE
        }


        holder.creditPriceView.text = "${feature.credit_price}$"
    }

    override fun getItemCount(): Int {
        return featuresList.size
    }

}