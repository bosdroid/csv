package com.boris.expert.csvmagic.utils

import android.content.ClipData
import android.content.ClipDescription
import android.view.MotionEvent
import android.view.View

class ChoiceTouchListener : View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return if (event!!.action == MotionEvent.ACTION_DOWN) {
            val item = ClipData.Item(v!!.tag as CharSequence)

            val mimeTypes: Array<String> = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)

            val data = ClipData(v.tag.toString(), mimeTypes, item)
            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDrag(data, shadowBuilder, v, 0)

            true
        } else {
            false
        }
    }
}