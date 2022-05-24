package com.boris.expert.csvmagic.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.max

class MyViewPager(context: Context, attrs: AttributeSet): ViewPager(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mHeightMeasureSpec = heightMeasureSpec
//        val mode = MeasureSpec.getMode(mHeightMeasureSpec)
//        // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
//        // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
//        // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
//        // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
//        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
//            // super has to be called in the beginning so the child views can be initialized.
//            super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)
//            var height = 0
//            for (i in 0 until childCount) {
//                val child: View = getChildAt(i)
//                child.measure(
//                    widthMeasureSpec,
//                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
//                )
//                val h: Int = child.measuredHeight
//                if (h > height) height = h
//            }
//            mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
//        }
//        // super has to be called again so the new specs are treated as exact measurements
//        // super has to be called again so the new specs are treated as exact measurements
//        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)
        var height = 0
        (0 until childCount).forEach {
            val child = getChildAt(it)
            child.measure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            height = max(height, child.measuredHeight)
        }
        if (height > 0) {
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        //setMeasuredDimension(measuredWidth, measureHeight(mHeightMeasureSpec, view))
    }

    private fun measureHeight(measureSpec: Int, view: View?): Int {
        var result = 0
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            // set the height from the base view if available
            if (view != null) {
                result = view.measuredHeight
            }
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }
}