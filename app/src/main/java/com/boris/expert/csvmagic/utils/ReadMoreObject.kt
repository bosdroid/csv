package com.boris.expert.csvmagic.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.boris.expert.csvmagic.R
import com.devs.readmoreoption.ReadMoreOption
import com.google.android.material.textview.MaterialTextView


object ReadMoreObject {

    fun setReadMore(context: Context,textView: MaterialTextView, text: String,lines:Int){
        val readMoreOption = ReadMoreOption.Builder(context)
            .textLength(lines, ReadMoreOption.TYPE_LINE) // OR
            //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
            .moreLabel("See More")
            .lessLabel("See Less")
            .moreLabelColor(ContextCompat.getColor(context, R.color.primary_positive_color))
            .lessLabelColor(ContextCompat.getColor(context, R.color.primary_positive_color))
            .labelUnderLine(true)
            .expandAnimation(true)
            .build()

        readMoreOption.addReadMoreTo(textView, text)
    }

}