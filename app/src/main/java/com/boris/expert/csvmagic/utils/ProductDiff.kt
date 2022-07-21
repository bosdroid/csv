package com.boris.expert.csvmagic.utils

import androidx.recyclerview.widget.DiffUtil
import com.boris.expert.csvmagic.model.Product

class ProductDiff : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.equals(newItem)
    }
}