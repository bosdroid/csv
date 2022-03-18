package com.boris.expert.csvmagic.utils

import android.content.ClipDescription
import android.content.Context
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.boris.expert.csvmagic.R
import com.google.android.material.textview.MaterialTextView
import org.apmem.tools.layouts.FlowLayout

class ChoiceDragListener : View.OnDragListener {
    override fun onDrag(v: View?, event: DragEvent?): Boolean {

        when (event!!.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

                    return true
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                v!!.invalidate()
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                v!!.invalidate()
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                return true
            }
            DragEvent.ACTION_DROP -> {

                val dragged = event.localState as MaterialTextView
                val targetView = v as MaterialTextView

                val oldOwner  = dragged.parent as ViewGroup
                val newOwner  = v.parent as ViewGroup

                val draggedPosition = oldOwner.indexOfChild(dragged)
                val targetPosition = oldOwner.indexOfChild(dragged)
                dragged.setTextColor(ContextCompat.getColor(dragged.context,R.color.white))
                dragged.setBackgroundColor(ContextCompat.getColor(dragged.context,R.color.secondary_positive_color))
                oldOwner.removeView(dragged)
                newOwner.addView(dragged,targetPosition)

                newOwner.removeView(v)
                oldOwner.addView(v,draggedPosition)

                return true

            }
            DragEvent.ACTION_DRAG_ENDED -> {
                v!!.invalidate()
                return true
            }
            else -> {
            }
        }
        return false
    }
}