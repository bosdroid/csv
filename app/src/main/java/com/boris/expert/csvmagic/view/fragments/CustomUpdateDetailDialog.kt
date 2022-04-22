package com.boris.expert.csvmagic.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.boris.expert.csvmagic.R
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import org.apmem.tools.layouts.FlowLayout

class CustomUpdateDetailDialog : DialogFragment() {

    private lateinit var getDescriptionView1: MaterialTextView
    private lateinit var getDescriptionView: MaterialTextView
    private lateinit var productShortDescriptionBox: TextInputEditText
    private lateinit var fullDescriptionBox: TextInputEditText
    private lateinit var titleBox: TextInputEditText
    private lateinit var dynamicFullDescTextViewWrapper: FlowLayout
    private lateinit var dynamicShortDescTextViewWrapper: FlowLayout
    private lateinit var dynamicTitleTextViewWrapper: FlowLayout
    private lateinit var secondLinearLayout: LinearLayout
    private lateinit var firstLinearLayout: LinearLayout
    private lateinit var swapLayoutBtn: MaterialCheckBox
    private lateinit var dialogHeading: MaterialTextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.insales_product_detail_update_dialog_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
    }

    private fun initViews(view: View) {
        dialogHeading = view.findViewById<MaterialTextView>(R.id.dialog_heading)
        swapLayoutBtn = view.findViewById<MaterialCheckBox>(R.id.layout_swap)
        firstLinearLayout =
            view.findViewById<LinearLayout>(R.id.first_linear_layout)
        secondLinearLayout =
            view.findViewById<LinearLayout>(R.id.second_linear_layout)
        dynamicTitleTextViewWrapper =
            view.findViewById(R.id.dynamic_insales_title_textview_wrapper)

        dynamicShortDescTextViewWrapper =
            view.findViewById<FlowLayout>(R.id.dynamic_insales_short_description_textview_wrapper)
        dynamicFullDescTextViewWrapper =
            view.findViewById(R.id.dynamic_insales_full_description_textview_wrapper)
        titleBox =
            view.findViewById<TextInputEditText>(R.id.insales_product_title_input_field)
        productShortDescriptionBox =
            view.findViewById<TextInputEditText>(R.id.insales_product_short_desc_input_field)
        fullDescriptionBox =
            view.findViewById(R.id.insales_product_full_desc_input_field)
        getDescriptionView =
            view.findViewById<MaterialTextView>(R.id.get_description_text_view)
        getDescriptionView1 =
            view.findViewById<MaterialTextView>(R.id.get_description_text_view1)
    }


}