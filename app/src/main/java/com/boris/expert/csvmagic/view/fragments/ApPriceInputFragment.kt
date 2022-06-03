package com.boris.expert.csvmagic.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.FieldListsAdapter
import com.boris.expert.csvmagic.model.ListItem
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.TableGenerator
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.boris.expert.csvmagic.view.activities.FieldListsActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView


class ApPriceInputFragment : Fragment() {

    private lateinit var apPriceActiveListNameView: MaterialTextView
    private lateinit var appSettings: AppSettings
    private lateinit var apPriceView: TextInputEditText
    private lateinit var tableGenerator: TableGenerator
    private var listId: Int? = null
    private lateinit var adapter: FieldListsAdapter
    private lateinit var apPriceListSpinner:AppCompatSpinner

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
        tableGenerator = TableGenerator(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_ap_price_input, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View){

        apPriceView = view.findViewById(R.id.ap_price)
        val apPriceSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_price_options_spinner)
        val apPriceListBtn = view.findViewById<MaterialButton>(R.id.ap_price_list_with_fields_btn)
        val apPriceDefaultInputBox = view.findViewById<TextInputEditText>(R.id.ap_price_non_changeable_default_text_input)
        val apPriceDefaultValueMessage =
            view.findViewById<MaterialTextView>(R.id.ap_price_default_value_message)
        apPriceListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_price_list_spinner)
        apPriceActiveListNameView = view.findViewById<MaterialTextView>(R.id.ap_price_active_list_name)
        val apPriceSpinnerSelectedPosition = appSettings.getInt("AP_PRICE_SPINNER_SELECTED_POSITION")
        val apPriceDefaultValue = appSettings.getString("AP_PRICE_DEFAULT_VALUE")
        val apPriceListId = appSettings.getInt("AP_PRICE_LIST_ID")
        val apPriceActiveListName = appSettings.getString("AP_PRICE_LIST_NAME")
        if (apPriceActiveListName!!.isEmpty()){
            apPriceActiveListNameView.text = "Active List: None"
        }
        else{
            apPriceActiveListNameView.text = "Active List: $apPriceActiveListName"
        }
        apPriceSpinner.setSelection(apPriceSpinnerSelectedPosition)
        apPriceListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_price")
        }
        when (apPriceSpinnerSelectedPosition) {
            1 -> {
                apPriceListSpinner.visibility = View.GONE
                apPriceListBtn.visibility = View.GONE
                apPriceActiveListNameView.visibility = View.GONE
                apPriceDefaultInputBox.visibility = View.VISIBLE
                apPriceDefaultValueMessage.visibility = View.VISIBLE
                apPriceView.visibility = View.VISIBLE
                apPriceDefaultInputBox.setText(apPriceDefaultValue)
                apPriceView.setText(apPriceDefaultValue)
//                BaseActivity.showSoftKeyboard(requireActivity(),apPriceDefaultInputBox)
            }
            2 -> {
                apPriceDefaultInputBox.visibility = View.GONE
                apPriceDefaultValueMessage.visibility = View.GONE
                apPriceListBtn.visibility = View.VISIBLE
                apPriceActiveListNameView.visibility = View.VISIBLE
                apPriceView.visibility = View.GONE
                apPriceListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apPriceListId)
                val listValues = listOptions.split(",")
                if (listValues.isNotEmpty()){
                    appSettings.putString("AP_PRODUCT_PRICE",listValues[0])
                }
                val apPriceSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apPriceListSpinner.adapter = apPriceSpinnerAdapter

                apPriceListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        appSettings.putString("AP_PRODUCT_PRICE",parent!!.selectedItem.toString())
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            }
            else -> {
                apPriceView.visibility = View.VISIBLE
                apPriceListBtn.visibility = View.GONE
                apPriceActiveListNameView.visibility = View.GONE
                apPriceDefaultInputBox.visibility = View.GONE
                apPriceDefaultValueMessage.visibility = View.VISIBLE
                apPriceListSpinner.visibility = View.GONE
//                BaseActivity.showSoftKeyboard(requireActivity(),apPriceView)
            }
        }

        apPriceDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                apPriceView.setText(s.toString())
                appSettings.putString("AP_PRICE_DEFAULT_VALUE", s.toString())
                appSettings.putString("AP_PRODUCT_PRICE",s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        apPriceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appSettings.putInt("AP_PRICE_SPINNER_SELECTED_POSITION", position)
                when (position) {
                    1 -> {
                        apPriceListSpinner.visibility = View.GONE
                        apPriceListBtn.visibility = View.GONE
                        apPriceActiveListNameView.visibility = View.GONE
                        apPriceDefaultInputBox.visibility = View.VISIBLE
                        apPriceDefaultValueMessage.visibility = View.VISIBLE
                        apPriceView.visibility = View.VISIBLE
                        apPriceDefaultInputBox.setText(apPriceDefaultValue)
                        apPriceView.setText(apPriceDefaultValue)
//                        BaseActivity.showSoftKeyboard(requireActivity(),apPriceDefaultInputBox)
                    }
                    2 -> {
                        apPriceDefaultValueMessage.visibility = View.GONE
                        apPriceDefaultInputBox.visibility = View.GONE
                        apPriceListBtn.visibility = View.VISIBLE
                        apPriceActiveListNameView.visibility = View.VISIBLE
                        apPriceView.visibility = View.GONE
                        apPriceListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apPriceListId)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()){
                            appSettings.putString("AP_PRODUCT_PRICE",listValues[0])
                        }
                        val apPriceSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apPriceListSpinner.adapter = apPriceSpinnerAdapter

                        apPriceListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                appSettings.putString("AP_PRODUCT_PRICE",parent!!.selectedItem.toString())
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }
                    }
                    else -> {
                        apPriceView.visibility = View.VISIBLE
                        apPriceListBtn.visibility = View.GONE
                        apPriceActiveListNameView.visibility = View.GONE
                        apPriceDefaultValueMessage.visibility = View.GONE
                        apPriceDefaultInputBox.visibility = View.GONE
                        apPriceListSpinner.visibility = View.GONE
//                        BaseActivity.showSoftKeyboard(requireActivity(),apPriceView)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun openListWithFieldsDialog(fieldType: String) {

        val listItems = mutableListOf<ListItem>()
        val layout =
            LayoutInflater.from(context).inflate(
                R.layout.list_with_fields_value_layout,
                null
            )
        val listWithFieldsValueRecyclerView =
            layout.findViewById<RecyclerView>(R.id.list_with_fields_recycler_view)
        listWithFieldsValueRecyclerView.layoutManager = LinearLayoutManager(context)
        listWithFieldsValueRecyclerView.hasFixedSize()
        adapter = FieldListsAdapter(requireActivity(), listItems as ArrayList<ListItem>)
        listWithFieldsValueRecyclerView.adapter = adapter


        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(layout)
        builder.setCancelable(true)
        val alert = builder.create()
        alert.show()
        val tempList = tableGenerator.getList()
        if (tempList.isNotEmpty()) {
            listItems.clear()
            listItems.addAll(tempList)
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyDataSetChanged()
        }


        adapter.setOnItemClickListener(object : FieldListsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val listValue = listItems[position]
                listId = listValue.id
                val list = tableGenerator.getListValues(listId!!)
                if (list.isNotEmpty()) {
                    //selectedListTextView.text = listValue.value
                    appSettings.putInt("AP_PRICE_LIST_ID", listId!!)
                    appSettings.putString("AP_PRICE_LIST_NAME",listValue.value)
                    apPriceActiveListNameView.text = "Active List: ${listValue.value}"
                    //appSettings.putString("AP_PRODUCT_PRICE",list.split(",")[0])
                    val listOptions: String = tableGenerator.getListValues(listId!!)
                    val listValues = listOptions.split(",")
                    if (listValues.isNotEmpty()){
                        appSettings.putString("AP_PRODUCT_PRICE",listValues[0])
                    }
                    val apPriceSpinnerAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        listValues
                    )
                    apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    apPriceListSpinner.adapter = apPriceSpinnerAdapter

                    apPriceListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            appSettings.putString("AP_PRODUCT_PRICE",parent!!.selectedItem.toString())
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
                    alert.dismiss()
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage(getString(R.string.field_list_value_empty_error_text))
                        .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.add_text)) { dialog, which ->
                            dialog.dismiss()
                            addTableDialog(listId!!)
                        }
                        .create().show()
                }

            }

            override fun onAddItemClick(position: Int) {
                alert.dismiss()
                val intent = Intent(context, FieldListsActivity::class.java)
//                    intent.putExtra("TABLE_NAME", tableName)
//                    intent.putExtra("FLAG", "yes")
                requireActivity().startActivity(intent)
            }
        })
    }

    private fun addTableDialog(id: Int) {
        val listValueLayout = LayoutInflater.from(context).inflate(
            R.layout.add_list_value_layout,
            null
        )
        val heading = listValueLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
        heading.text = getString(R.string.list_value_hint_text)
        val listValueInputBox = listValueLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
        val listValueAddBtn = listValueLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(listValueLayout)
        val alert = builder.create()
        alert.show()
        listValueAddBtn.setOnClickListener {
            if (listValueInputBox.text.toString().isNotEmpty()) {
                val value = listValueInputBox.text.toString().trim()
                tableGenerator.insertListValue(id, value)
                alert.dismiss()
            } else {
                BaseActivity.showAlert(
                    requireActivity(),
                    getString(R.string.add_list_value_error_text)
                )
            }
        }
    }
}