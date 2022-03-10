package com.boris.expert.csvmagic.utils

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan


abstract class TouchableSpan : ClickableSpan() {

    private var mIsPressed = false
    private var mPressedBackgroundColor = Color.parseColor("#6301EF")
    private var mNormalTextColor = 0
    private var mPressedTextColor = 0

    fun TouchableSpan(normalTextColor: Int, pressedTextColor: Int, pressedBackgroundColor: Int) {
        mNormalTextColor = normalTextColor
        mPressedTextColor = pressedTextColor
        mPressedBackgroundColor = pressedBackgroundColor
    }

    fun setPressed(isSelected: Boolean) {
        mIsPressed = isSelected
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = if (mIsPressed) mPressedTextColor else mNormalTextColor
        ds.bgColor = if (mIsPressed) mPressedBackgroundColor else Color.parseColor("#FFFFFF")
        ds.isUnderlineText = false
    }

}