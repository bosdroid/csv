package com.boris.expert.csvmagic.utils

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


class ExpandableTextView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null) : AppCompatTextView(context, attrs, 0) {

    private var collapsedLines = 0
    private var expandedLines = 0
    private var collapsedHeight = 0
    private var expandedHeight = 0
    private var collapsed = true
    private var speed = 25
    private var inited = false

    private val textCanvas: Canvas? = null

//    constructor(context: Context, attrs: AttributeSet): this(context) {
//        inited = false
//    }
//
//    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): this(context, attrs) {
//        inited = false
//    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!inited) {
            setCollapsedLines(1)
            setMeasuredDimension(widthMeasureSpec, collapsedHeight)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setExpandedLines(lineCount)
        init()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
    }

    fun init() {
        if (!inited) {
            inited = true
            setOnClickListener { updateState() }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    setExpandedLines(lineCount)
                }
            })
        }
    }

    private fun updateState() {
        if (collapsed) {
            expand()
        } else {
            collapse()
        }
    }

    private fun expand() {
        collapsed = false
        invalidate()
    }

    private fun collapse() {
        collapsed = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (collapsed == false && height < expandedHeight) {
            height = if (height + getSpeed() > expandedHeight) expandedHeight else height + getSpeed()
            invalidate()
        }
        if (collapsed && height > collapsedHeight) {
            height = if (height - getSpeed() < collapsedHeight) collapsedHeight else height - getSpeed()
            invalidate()
        }
    }

    fun setCollapsedLines(collapsedLines: Int) {
        this.collapsedLines = collapsedLines
        collapsedHeight = Math.ceil((collapsedLines * (lineHeight + lineSpacingExtra) + paddingBottom + paddingTop + lastBaselineToBottomHeight).toDouble()).toInt()
    }

    private fun setExpandedLines(expandedLines: Int) {
        this.expandedLines = expandedLines
        expandedHeight = Math.ceil((expandedLines * (lineHeight + lineSpacingExtra) + paddingBottom + paddingTop + lastBaselineToBottomHeight).toDouble()).toInt()
    }

    fun setSpeed(speed: Int) {
        this.speed = speed
    }

    fun getSpeed(): Int {
        return speed
    }

}