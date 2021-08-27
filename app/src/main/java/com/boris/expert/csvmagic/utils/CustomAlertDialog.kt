package com.boris.expert.csvmagic.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import com.boris.expert.csvmagic.R
import com.google.android.material.button.MaterialButton

class CustomAlertDialog : AppCompatDialogFragment() {

    private lateinit var eraseBtn:LinearLayout
    private lateinit var imageRecognitionBtn:LinearLayout
    private lateinit var photoRecognitionBtn:LinearLayout
    private lateinit var dismissBtn:MaterialButton
    private var listener:CustomDialogListener?=null

    interface CustomDialogListener{
        fun onEraseBtnClick(alertDialog: CustomAlertDialog)
        fun onImageRecognitionBtnClick(alertDialog: CustomAlertDialog)
        fun onPhotoRecognitionBtnClick(alertDialog: CustomAlertDialog)
        fun onDismissBtnClick(alertDialog: CustomAlertDialog)

    }

    public fun setFocusListener(listener: CustomDialogListener?) {
        this.listener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val view = layoutInflater.inflate(R.layout.custom_alert_dialog_layout, null)
        eraseBtn = view.findViewById(R.id.erase_btn)
        imageRecognitionBtn = view.findViewById(R.id.image_recognition_btn)
        photoRecognitionBtn = view.findViewById(R.id.photo_recognition_btn)
        dismissBtn = view.findViewById(R.id.custom_dialog_layout_dismiss_btn)
        builder.setView(view)
        builder.setCancelable(false)


        eraseBtn.setOnClickListener {
            listener!!.onEraseBtnClick(this)
        }

        imageRecognitionBtn.setOnClickListener {
            listener!!.onImageRecognitionBtnClick(this)
        }

        photoRecognitionBtn.setOnClickListener {
            listener!!.onPhotoRecognitionBtnClick(this)
        }

        dismissBtn.setOnClickListener {
            listener!!.onDismissBtnClick(this)
        }


        return builder.create()
    }

}