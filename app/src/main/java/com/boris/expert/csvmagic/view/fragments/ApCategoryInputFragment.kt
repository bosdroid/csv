package com.boris.expert.csvmagic.view.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.model.Category
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory


class ApCategoryInputFragment : Fragment() {

    private lateinit var categoriesSpinner: AppCompatSpinner
    private var originalCategoriesList = mutableListOf<Category>()
    private var selectedCategoryId = 0
    private lateinit var viewModel: SalesCustomersViewModel
    private lateinit var appSettings: AppSettings
    private var email = ""
    private var password = ""
    private var shopName = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(SalesCustomersViewModel()).createFor()
        )[SalesCustomersViewModel::class.java]
        shopName = appSettings.getString("INSALES_SHOP_NAME") as String
        email = appSettings.getString("INSALES_EMAIL") as String
        password = appSettings.getString("INSALES_PASSWORD") as String
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_ap_category_input, container, false)
        initViews(v)
        return v
    }


    private fun initViews(view: View) {

        categoriesSpinner =
            view.findViewById(R.id.ap_cate_spinner)

        val cateSpinnerAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            originalCategoriesList
        )
        cateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = cateSpinnerAdapter

        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = originalCategoriesList[position]
                selectedCategoryId = selectedItem.id
                appSettings.putInt("AP_PRODUCT_CATEGORY", selectedCategoryId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        if (originalCategoriesList.isEmpty()) {
            getCategories(cateSpinnerAdapter)
        }


    }

    private fun getCategories(adapter: ArrayAdapter<Category>) {
//        BaseActivity.startLoading(requireActivity())
        viewModel.callCategories(requireActivity(), shopName, email, password)
        viewModel.getCategoriesResponse().observe(requireActivity(), Observer { response ->
            if (response != null) {
                BaseActivity.dismiss()
                if (response.get("status").asString == "200") {
                    val categories = response.get("categories").asJsonArray
                    if (categories.size() > 0) {
                        originalCategoriesList.clear()
                        for (i in 0 until categories.size()) {
                            val category = categories[i].asJsonObject
                            originalCategoriesList.add(
                                Category(
                                    category.get("title").asString,
                                    category.get("id").asInt
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                        if (originalCategoriesList.size > 0) {
                            setDefaultValue(originalCategoriesList)
                        }
                    }
                } else {
                    BaseActivity.dismiss()
                }
            } else {
                BaseActivity.dismiss()
            }
        })
    }

    private fun setDefaultValue(originalCategoriesList: MutableList<Category>) {
       var itemFound = false
       var foundItem : Category?=null
       var foundItemPosition = 0
        for (i in 0 until originalCategoriesList.size) {
            val item = originalCategoriesList[i]
            if (item.id == appSettings.getInt("AP_PRODUCT_CATEGORY")) {
                foundItemPosition = i
                foundItem = item
                itemFound = true
                break
            }
        }
        if (itemFound){
            categoriesSpinner.setSelection(foundItemPosition)
            selectedCategoryId = foundItem!!.id
            appSettings.putInt("AP_PRODUCT_CATEGORY", selectedCategoryId)
        }
        else{
            categoriesSpinner.setSelection(0)
            selectedCategoryId = originalCategoriesList[0].id
            appSettings.putInt("AP_PRODUCT_CATEGORY", selectedCategoryId)
        }
    }
}