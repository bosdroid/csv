package com.boris.expert.csvmagic.utils

import androidx.recyclerview.widget.DiffUtil
import com.boris.expert.csvmagic.model.Product

class ProductDiffCallback(oldProductList:List<Product>, newProductList:List<Product>) : DiffUtil.Callback() {

    private var mOldProductList: List<Product>? = null
    private var mNewProductList: List<Product>? = null

    init {
        mOldProductList = oldProductList
        mNewProductList = newProductList
    }

    override fun getOldListSize(): Int {
      return mOldProductList!!.size
    }

    override fun getNewListSize(): Int {
       return mNewProductList!!.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldProductList!![oldItemPosition].id == mNewProductList!![newItemPosition].id;
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = mOldProductList!![oldItemPosition];
        val newProduct = mNewProductList!![newItemPosition];
        return oldProduct == newProduct
    }
}