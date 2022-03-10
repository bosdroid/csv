package com.boris.expert.csvmagic.utils

import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import com.google.android.material.textview.MaterialTextView

class LinkMovementMethodOverride : View.OnTouchListener  {

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        val widget = v as MaterialTextView

        val text = widget.text

        if (text is Spanned ){
            val buffer:Spanned = text
            val action = event!!.action
            if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_DOWN) {
                var x:Int = event.x.toInt()
                var y:Int = event.y.toInt()

                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop

                x += widget.scrollX
                y += widget.scrollY

                val layout = widget.layout

                val line:Int = layout.getLineForVertical(y)
                val off:Int = layout.getOffsetForHorizontal(line, x.toFloat())

                val link = buffer.getSpans(off, off, ClickableSpan::class.java)

                if (link.isNotEmpty()) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {

                    }
                    return true
                }

            }
        }
        return false
    }


}