package com.boris.expert.csvmagic.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.text.isDigitsOnly
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView


class ApQuantityInputFragment : Fragment() {

    private lateinit var apQuantityListBtn: MaterialButton
    private lateinit var apQuantityDefaultValueMessage: MaterialTextView
    private lateinit var apQuantityDefaultInputWrapper: TextInputLayout
    private lateinit var apQuantityViewWrapper: TextInputLayout
    private lateinit var apQuantityActiveListNameView: MaterialTextView
    private lateinit var appSettings: AppSettings
    private lateinit var apQuantityView: TextInputEditText
    private lateinit var tableGenerator: TableGenerator
    private var listId: Int? = null
    private lateinit var adapter: FieldListsAdapter
    private lateinit var apQuantityListSpinner:AppCompatSpinner

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
        val v = inflater.inflate(R.layout.fragment_ap_quantity_input, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View){

        apQuantityView = view.findViewById(R.id.ap_quantity)
        apQuantityViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_quantity_wrapper)
        val apQuantitySpinner = view.findViewById<AppCompatSpinner>(R.id.ap_quantity_options_spinner)
        apQuantityListBtn = view.findViewById<MaterialButton>(R.id.ap_quantity_list_with_fields_btn)
        val apQuantityDefaultInputBox = view.findViewById<TextInputEditText>(R.id.ap_quantity_non_changeable_default_text_input)
        apQuantityDefaultInputWrapper = view.findViewById<TextInputLayout>(R.id.ap_quantity_non_changeable_default_text_input_wrapper)
        apQuantityDefaultValueMessage =
            view.findViewById<MaterialTextView>(R.id.ap_quantity_default_value_message)
        apQuantityListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_quantity_list_spinner)
        apQuantityActiveListNameView = view.findViewById<MaterialTextView>(R.id.ap_quantity_active_list_name)
        val apQuantitySpinnerSelectedPosition = appSettings.getInt("AP_QUANTITY_SPINNER_SELECTED_POSITION")
        val apQuantityDefaultValue = appSettings.getString("AP_QUANTITY_DEFAULT_VALUE")
        val apQuantityListId = appSettings.getInt("AP_QUANTITY_LIST_ID")
        val apQuantityActiveListName = appSettings.getString("AP_QUANTITY_LIST_NAME")
        if (apQuantityActiveListName!!.isEmpty()){
            apQuantityActiveListNameView.text = "Active List: None"
        }
        else{
            apQuantityActiveListNameView.text = "Active List: $apQuantityActiveListName"
        }
        apQuantitySpinner.setSelection(apQuantitySpinnerSelectedPosition)
        apQuantityListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_quantity")
        }
        when (apQuantitySpinnerSelectedPosition) {
            1 -> {
                apQuantityListSpinner.visibility = View.GONE
                apQuantityListBtn.visibility = View.GONE
                apQuantityActiveListNameView.visibility = View.GONE
                apQuantityDefaultInputWrapper.visibility = View.VISIBLE
                apQuantityDefaultValueMessage.visibility = View.VISIBLE
                apQuantityViewWrapper.visibility = View.VISIBLE
                apQuantityDefaultInputBox.setText(apQuantityDefaultValue)
                apQuantityView.setText(apQuantityDefaultValue)
//                BaseActivity.showSoftKeyboard(requireActivity(),apQuantityDefaultInputBox)
            }
            2 -> {
                apQuantityDefaultValueMessage.visibility = View.GONE
                apQuantityDefaultInputWrapper.visibility = View.GONE
                apQuantityListBtn.visibility = View.VISIBLE
                apQuantityActiveListNameView.visibility = View.VISIBLE
                apQuantityViewWrapper.visibility = View.GONE
                apQuantityListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apQuantityListId)
                val listValues = listOptions.split(",")
                if (listValues.isNotEmpty()){
                    appSettings.putString("AP_PRODUCT_QUANTITY",listValues[0])
                }
                val apQuantitySpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                apQuantityListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        appSettings.putString("AP_PRODUCT_QUANTITY",parent!!.selectedItem.toString())
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            }
            else -> {
                apQuantityViewWrapper.visibility = View.VISIBLE
                apQuantityListBtn.visibility = View.GONE
                apQuantityActiveListNameView.visibility = View.GONE
                apQuantityDefaultInputWrapper.visibility = View.GONE
                apQuantityDefaultValueMessage.visibility = View.GONE
                apQuantityListSpinner.visibility = View.GONE
//                BaseActivity.showSoftKeyboard(requireActivity(),apQuantityView)
            }
        }

        apQuantityDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().substring(0) == "0"){
                    BaseActivity.showAlert(requireActivity(),"Default Quantity value should start from greater then 0!")
                    apQuantityDefaultInputBox.setText(s.toString().substring(1))
                }
                else{
                    apQuantityView.setText(s.toString())
                    appSettings.putString("AP_QUANTITY_DEFAULT_VALUE", s.toString())
                    appSettings.putString("AP_PRODUCT_QUANTITY",s.toString())
                }
            }

        })
        apQuantityDefaultInputBox.setOnEditorActionListener(object :TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    BaseActivity.hideSoftKeyboard(requireActivity(),apQuantityDefaultInputBox)
                    val intent = Intent("move-next")
                    LocalBroadcastManager.getInstance(requireActivity())
                        .sendBroadcast(intent)
                }
                return false
            }

        })

        apQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appSettings.putInt("AP_QUANTITY_SPINNER_SELECTED_POSITION", position)
                when (position) {
                    1 -> {
                        apQuantityListSpinner.visibility = View.GONE
                        apQuantityListBtn.visibility = View.GONE
                        apQuantityActiveListNameView.visibility = View.GONE
                        apQuantityDefaultInputWrapper.visibility = View.VISIBLE
                        apQuantityDefaultValueMessage.visibility = View.VISIBLE
                        apQuantityViewWrapper.visibility = View.VISIBLE
                        apQuantityDefaultInputBox.setText(apQuantityDefaultValue)
                        apQuantityView.setText(apQuantityDefaultValue)
//                        BaseActivity.showSoftKeyboard(requireActivity(),apQuantityDefaultInputBox)
                    }
                    2 -> {
                        apQuantityDefaultValueMessage.visibility = View.GONE
                        apQuantityDefaultInputWrapper.visibility = View.GONE
                        apQuantityListBtn.visibility = View.VISIBLE
                        apQuantityActiveListNameView.visibility = View.VISIBLE
                        apQuantityViewWrapper.visibility = View.GONE
                        apQuantityListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apQuantityListId)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()){
                            appSettings.putString("AP_PRODUCT_QUANTITY",listValues[0])
                        }
                        val apQuantitySpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                        apQuantityListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }
                    }
                    else -> {
                        apQuantityViewWrapper.visibility = View.VISIBLE
                        apQuantityListBtn.visibility = View.GONE
                        apQuantityActiveListNameView.visibility = View.GONE
                        apQuantityDefaultInputWrapper.visibility = View.GONE
                        apQuantityDefaultValueMessage.visibility = View.GONE
                        apQuantityListSpinner.visibility = View.GONE
//                        BaseActivity.showSoftKeyboard(requireActivity(),apQuantityView)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        apQuantityView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().substring(0) == "0"){
                    BaseActivity.showAlert(requireActivity(),"Quantity value should start from greater then 0!")
                    apQuantityView.setText(s.toString().substring(1))
                }
                else{
                    appSettings.putString("AP_PRODUCT_QUANTITY", s.toString())
                }

            }

        })
        apQuantityView.setOnEditorActionListener(object:TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT){
                    BaseActivity.hideSoftKeyboard(requireActivity(),apQuantityView)
                    val intent = Intent("move-next")
                    LocalBroadcastManager.getInstance(requireActivity())
                        .sendBroadcast(intent)
                }
                return false
            }

        })

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
        val closeDialogBtn = layout.findViewById<AppCompatImageView>(R.id.lwfv_dialog_close_btn)

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(layout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()
        closeDialogBtn.setOnClickListener {
            alert.dismiss()
        }
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
                    var isStringFound = false
                    val listArray = list.split(",")
                    for (i in 0 until listArray.size){
                        if (!listArray[i].trim().isDigitsOnly()){
                            isStringFound = true
                            break
                        }
                        else{
                            isStringFound = false
                        }
                    }

                    if (isStringFound){
                        BaseActivity.showAlert(requireActivity(),requireActivity().getString(R.string.quantity_list_string_error))
                    }
                    else {
                        //selectedListTextView.text = listValue.value
                        appSettings.putInt("AP_QUANTITY_LIST_ID", listId!!)
                        appSettings.putString("AP_QUANTITY_LIST_NAME", listValue.value)
                        apQuantityActiveListNameView.text = "Active Lis: ${listValue.value}"
                        //appSettings.putString("AP_PRODUCT_QUANTITY",list.split(",")[0])
                        val listOptions: String = tableGenerator.getListValues(listId!!)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()) {
                            appSettings.putString("AP_PRODUCT_QUANTITY", listValues[0])
                        }
                        val apQuantitySpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                        apQuantityListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    appSettings.putString(
                                        "AP_PRODUCT_QUANTITY",
                                        parent!!.selectedItem.toString()
                                    )
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }
                        alert.dismiss()
                    }
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

    fun updateTestData(text:String){
        apQuantityView.setText(text)
        apQuantityViewWrapper.visibility = View.VISIBLE
        apQuantityListBtn.visibility = View.GONE
        apQuantityActiveListNameView.visibility = View.GONE
        apQuantityDefaultInputWrapper.visibility = View.GONE
        apQuantityDefaultValueMessage.visibility = View.GONE
        apQuantityListSpinner.visibility = View.GONE
    }

}