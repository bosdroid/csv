package com.boris.expert.csvmagic.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import com.boris.expert.csvmagic.R
import com.bumptech.glide.Glide


class FullImageFragment(private val path:String) : DialogFragment() {

    private lateinit var closeDialogBtn:AppCompatImageView
    private lateinit var fullImageView:AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_full_image, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View) {
        closeDialogBtn = view.findViewById(R.id.full_image_close_dialog)
        fullImageView = view.findViewById(R.id.full_image_view)

        closeDialogBtn.setOnClickListener {
            dismiss()
        }

        Glide.with(requireActivity())
            .load(path)
            .thumbnail(Glide.with(requireActivity()).load(R.drawable.loader))
            .fitCenter()
            .into(fullImageView)
    }

}