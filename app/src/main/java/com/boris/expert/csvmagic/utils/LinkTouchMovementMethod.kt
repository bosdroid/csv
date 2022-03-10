package com.boris.expert.csvmagic.utils

import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView


class LinkTouchMovementMethod : LinkMovementMethod() {
    private var mPressedSpan: TouchableSpan? = null

    override fun onTouchEvent(
        textView: TextView?,
        spannable: Spannable,
        event: MotionEvent
    ): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView!!, spannable, event)
            if (mPressedSpan != null) {
                mPressedSpan!!.setPressed(true)
                Selection.setSelection(
                    spannable, spannable.getSpanStart(mPressedSpan),
                    spannable.getSpanEnd(mPressedSpan)
                )
            }
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            val touchedSpan: TouchableSpan = getPressedSpan(textView!!, spannable, event)!!
            if (mPressedSpan != null && touchedSpan !== mPressedSpan) {
                mPressedSpan!!.setPressed(false)
                mPressedSpan = null
                Selection.removeSelection(spannable)
            }
        } else {
            if (mPressedSpan != null) {
                mPressedSpan!!.setPressed(false)
                super.onTouchEvent(textView, spannable, event)
            }
            mPressedSpan = null
            Selection.removeSelection(spannable)
        }
        return true
    }

    private fun getPressedSpan(
        textView: TextView,
        spannable: Spannable,
        event: MotionEvent
    ): TouchableSpan? {
        val x = event.x.toInt() - textView.totalPaddingLeft + textView.scrollX
        val y = event.y.toInt() - textView.totalPaddingTop + textView.scrollY
        val layout: Layout = textView.layout
        val position: Int = layout.getOffsetForHorizontal(layout.getLineForVertical(y), x.toFloat())
        val link = spannable.getSpans(position, position, TouchableSpan::class.java)
        var touchedSpan: TouchableSpan? = null
        if (link.size > 0 && positionWithinTag(position, spannable, link[0])) {
            touchedSpan = link[0]
        }
        return touchedSpan
    }

    private fun positionWithinTag(position: Int, spannable: Spannable, tag: Any): Boolean {
        return position >= spannable.getSpanStart(tag) && position <= spannable.getSpanEnd(tag)
    }

}