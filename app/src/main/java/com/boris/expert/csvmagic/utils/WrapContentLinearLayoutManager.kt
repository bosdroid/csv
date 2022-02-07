package com.boris.expert.csvmagic.utils

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.IndexOutOfBoundsException

class WrapContentLinearLayoutManager(context: Context,orientation:Int,reverse:Boolean) : LinearLayoutManager(context,orientation,reverse) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("TAG", "meet a IOOBE in RecyclerView")
        }
    }

}