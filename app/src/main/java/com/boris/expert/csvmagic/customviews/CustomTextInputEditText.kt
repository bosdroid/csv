package com.boris.expert.csvmagic.customviews

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textfield.TextInputEditText

class CustomTextInputEditText :
    TextInputEditText {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, android.R.attr.editTextStyle) {

    }


}