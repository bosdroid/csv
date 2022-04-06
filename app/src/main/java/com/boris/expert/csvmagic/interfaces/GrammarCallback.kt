package com.boris.expert.csvmagic.interfaces

import android.text.SpannableStringBuilder

interface GrammarCallback {
    fun onSuccess(response:SpannableStringBuilder?,errors:Boolean)
}