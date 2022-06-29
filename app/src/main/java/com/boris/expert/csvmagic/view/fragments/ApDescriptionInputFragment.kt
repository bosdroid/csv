package com.boris.expert.csvmagic.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.FieldListsAdapter
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.model.ListItem
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.activities.*
import com.boris.expert.csvmagic.viewmodel.SharedViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class ApDescriptionInputFragment : Fragment() {

    private lateinit var apDescriptionVoiceRecView: LinearLayout
    private lateinit var apDescriptionImageRecView: LinearLayout
    private lateinit var apDescriptionCameraRecView: LinearLayout
    private lateinit var apDescriptionDefaultValueMessage: MaterialTextView
    private lateinit var apDescriptionDefaultInputWrapper: TextInputLayout
    private lateinit var apDescriptionListBtn: MaterialButton
    private lateinit var apDescriptionViewWrapper: TextInputLayout
    private lateinit var apDescriptionActiveListNameView: MaterialTextView
    private lateinit var apDescriptionListSpinner:AppCompatSpinner
    private lateinit var appSettings: AppSettings
    private lateinit var apDescriptionView: TextInputEditText
    private lateinit var tableGenerator: TableGenerator
    private var listId: Int? = null
    private lateinit var adapter: FieldListsAdapter
    private lateinit var getDescriptionBtn:MaterialTextView
    private var userCurrentCredits = ""
    private lateinit var sharedViewModel: SharedViewModel
    private var voiceLanguageCode = "en"
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
        tableGenerator = TableGenerator(requireActivity())
        sharedViewModel = ViewModelProviders.of(
                requireActivity() as MainActivity,
                ViewModelFactory(SharedViewModel()).createFor()
        )[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_ap_description_input, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View){

        apDescriptionView  = view.findViewById(R.id.ap_description)
        apDescriptionViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_description_wrapper)
        getDescriptionBtn  = view.findViewById(R.id.get_description_text_view)
        val apDescriptionSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_description_options_spinner)
        apDescriptionListBtn = view.findViewById<MaterialButton>(R.id.ap_description_list_with_fields_btn)
        val apDescriptionDefaultInputBox = view.findViewById<TextInputEditText>(R.id.ap_description_non_changeable_default_text_input)
        apDescriptionDefaultInputWrapper = view.findViewById<TextInputLayout>(R.id.ap_description_non_changeable_default_text_input_wrapper)
        apDescriptionDefaultValueMessage =
            view.findViewById<MaterialTextView>(R.id.ap_description_default_value_message)
        apDescriptionListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_description_list_spinner)
        apDescriptionActiveListNameView = view.findViewById<MaterialTextView>(R.id.ap_description_active_list_name)
        apDescriptionCameraRecView = view.findViewById<LinearLayout>(R.id.ap_description_camera_layout)
        apDescriptionImageRecView = view.findViewById<LinearLayout>(R.id.ap_description_images_layout)
        apDescriptionVoiceRecView = view.findViewById<LinearLayout>(R.id.ap_description_voice_layout)

        getDescriptionBtn.setOnClickListener {
            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

            if (userCurrentCredits.toFloat() >= 1.0) {

                launchActivity.launch(
                    Intent(
                        requireActivity(),
                        RainForestApiActivity::class.java
                    )
                )
            } else {
                MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(getString(R.string.low_credites_error_message2))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                        dialog.dismiss()
                        startActivity(Intent(context, UserScreenActivity::class.java))
                    }
                    .create().show()
            }
        }

        apDescriptionCameraRecView.setOnClickListener {

            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(), Constants.CAMERA_PERMISSION
                )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apDescriptionCameraRecView)
                pickImageFromCamera()
            }
        }
        apDescriptionImageRecView.setOnClickListener {
            Constants.hint = "ap_description"
            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(),
                    Constants.READ_STORAGE_PERMISSION
                )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apDescriptionImageRecView)
                pickImageFromGallery()
            }
        }

        sharedViewModel.getDescriptionValue().observe(viewLifecycleOwner, Observer { updateDescription->
            apDescriptionView.setText(updateDescription)
        })

        apDescriptionVoiceRecView.setOnClickListener {
            voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
            val voiceLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.voice_language_setting_layout, null)
            val voiceLanguageSpinner = voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
            val voiceLanguageSaveBtn = voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

            if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                voiceLanguageSpinner.setSelection(0,false)
            } else {
                voiceLanguageSpinner.setSelection(1,false)
            }

            voiceLanguageSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                        ) {
                            voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(Locale.ENGLISH).contains("english")){"en"}else{"ru"}
                            appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setView(voiceLayout)
            val alert = builder.create();
            alert.show()
            voiceLanguageSaveBtn.setOnClickListener {
                alert.dismiss()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode)

                }
                voiceResultLauncher.launch(intent)
            }
        }

        apDescriptionView.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
              appSettings.putString("AP_PRODUCT_DESCRIPTION",s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        apDescriptionView.setOnEditorActionListener(object:TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT){
                    BaseActivity.hideSoftKeyboard(requireActivity(),apDescriptionView)
                    val intent = Intent("move-next")
                    LocalBroadcastManager.getInstance(requireActivity())
                        .sendBroadcast(intent)
                }
                return false
            }

        })

        val apDescriptionSpinnerSelectedPosition = appSettings.getInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION")
        val apDescriptionDefaultValue = appSettings.getString("AP_DESCRIPTION_DEFAULT_VALUE")
        val apDescriptionListId = appSettings.getInt("AP_DESCRIPTION_LIST_ID")
        val apDescriptionActiveListName = appSettings.getString("AP_DESCRIPTION_LIST_NAME")
        if (apDescriptionActiveListName!!.isEmpty()){
            apDescriptionActiveListNameView.text = "Active List: None"
        }
        else{
            apDescriptionActiveListNameView.text = "Active List: $apDescriptionActiveListName"
        }
        apDescriptionSpinner.setSelection(apDescriptionSpinnerSelectedPosition)
        apDescriptionListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_description")
        }
        when (apDescriptionSpinnerSelectedPosition) {
            1 -> {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.VISIBLE
                apDescriptionDefaultInputWrapper.visibility = View.VISIBLE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionDefaultInputBox.setText(apDescriptionDefaultValue)
                apDescriptionView.setText(apDescriptionDefaultValue)
//                BaseActivity.showSoftKeyboard(requireActivity(),apDescriptionDefaultInputBox)
            }
            2 -> {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionListBtn.visibility = View.VISIBLE
                apDescriptionActiveListNameView.visibility = View.VISIBLE
                apDescriptionViewWrapper.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apDescriptionListId)
                val listValues = listOptions.split(",")
                if (listValues.isNotEmpty()){
                    appSettings.putString("AP_PRODUCT_DESCRIPTION",listValues[0])
                }
                val apDescriptionSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                apDescriptionListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        appSettings.putString("AP_PRODUCT_DESCRIPTION",parent!!.selectedItem.toString())
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }

            }
            3 -> {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionVoiceRecView.visibility = View.VISIBLE
            }
            4 -> {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.GONE
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionCameraRecView.visibility = View.VISIBLE
            }
            5 -> {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.GONE
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionImageRecView.visibility = View.VISIBLE
            }
            else -> {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
//                BaseActivity.showSoftKeyboard(requireActivity(),apDescriptionView)
            }
        }

        apDescriptionDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                apDescriptionView.setText(s.toString())
                appSettings.putString("AP_DESCRIPTION_DEFAULT_VALUE", s.toString())
                appSettings.putString("AP_PRODUCT_DESCRIPTION",s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        apDescriptionDefaultInputBox.setOnEditorActionListener(object :TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    BaseActivity.hideSoftKeyboard(requireActivity(),apDescriptionDefaultInputBox)
                    val intent = Intent("move-next")
                    LocalBroadcastManager.getInstance(requireActivity())
                        .sendBroadcast(intent)
                }
                return false
            }

        })

        apDescriptionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appSettings.putInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION", position)
                when (position) {
                    1 -> {
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.VISIBLE
                        apDescriptionDefaultValueMessage.visibility = View.VISIBLE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionDefaultInputBox.setText(apDescriptionDefaultValue)
                        apDescriptionView.setText(apDescriptionDefaultValue)
//                        BaseActivity.showSoftKeyboard(requireActivity(),apDescriptionDefaultInputBox)
                    }
                    2 -> {
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionDefaultValueMessage.visibility = View.GONE
                        apDescriptionListBtn.visibility = View.VISIBLE
                        apDescriptionActiveListNameView.visibility = View.VISIBLE
                        apDescriptionViewWrapper.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apDescriptionListId)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()){
                            appSettings.putString("AP_PRODUCT_DESCRIPTION",listValues[0])
                        }
                        val apDescriptionSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                        apDescriptionListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                appSettings.putString("AP_PRODUCT_DESCRIPTION",parent!!.selectedItem.toString())
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

                    }
                    3 -> {
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionDefaultValueMessage.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionVoiceRecView.visibility = View.VISIBLE
                    }
                    4 -> {
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionDefaultValueMessage.visibility = View.GONE
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionCameraRecView.visibility = View.VISIBLE
                    }
                    5 -> {
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionDefaultValueMessage.visibility = View.GONE
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionImageRecView.visibility = View.VISIBLE
                    }
                    else -> {
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionDefaultValueMessage.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
//                        BaseActivity.showSoftKeyboard(requireActivity(),apDescriptionView)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }


    override fun onResume() {
        super.onResume()
        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
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
                    //selectedListTextView.text = listValue.value
                        appSettings.putInt("AP_DESCRIPTION_LIST_ID", listId!!)
                    appSettings.putString("AP_DESCRIPTION_LIST_NAME",listValue.value)
                    apDescriptionActiveListNameView.text = "Active List: ${listValue.value}"
                    //appSettings.putString("AP_PRODUCT_DESCRIPTION",list.split(",")[0])
                    val listOptions: String = tableGenerator.getListValues(listId!!)
                    val listValues = listOptions.split(",")
                    if (listValues.isNotEmpty()){
                        appSettings.putString("AP_PRODUCT_DESCRIPTION",listValues[0])
                    }
                    val apDescriptionSpinnerAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        listValues
                    )
                    apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                    apDescriptionListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            appSettings.putString("AP_PRODUCT_DESCRIPTION",parent!!.selectedItem.toString())
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

    var launchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                if (data != null && data.hasExtra("DESCRIPTION")) {
                    val description = data.getStringExtra("DESCRIPTION") as String
                    if (description.isNotEmpty()) {

                        val currentPItemDescription = apDescriptionView.text.toString().trim()
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemDescription)
                        stringBuilder.append(description)
                        apDescriptionView.setText(stringBuilder.toString())
                        appSettings.putString("AP_PRODUCT_DESCRIPTION",apDescriptionView.text.toString().trim())

                    }
                }
            }
        }

    private var voiceResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText: String =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results!!.get(0)
                        }
                val currentPItemTitle = apDescriptionView.text.toString().trim()
                val stringBuilder = java.lang.StringBuilder()
                stringBuilder.append(currentPItemTitle)
                stringBuilder.append(spokenText)
                apDescriptionView.setText(stringBuilder.toString())
                appSettings.putString("AP_PRODUCT_DESCRIPTION",apDescriptionView.text.toString().trim())
            }
        }

    fun pickImageFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher1.launch(
            Intent.createChooser(
                pickPhoto, getString(R.string.choose_image_gallery)
            )
        )
    }

    private var resultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val cropPicUri = CropImage.getPickImageResultUri(requireActivity(), data)
                cropImage(cropPicUri)
            }
        }

    private fun cropImage(imageUri: Uri) {

        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(requireActivity())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (Constants.hint == "ap_description" && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imgUri = result.uri
            try {
                TextRecogniser.runTextRecognition(requireActivity(), apDescriptionView, imgUri)
                Handler(Looper.myLooper()!!).postDelayed({
                    appSettings.putString("AP_PRODUCT_DESCRIPTION",apDescriptionView.text.toString().trim())
                },2000)
                Constants.hint = "default"
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        //super.onActivityResult(requestCode, resultCode, data)
    }

    fun pickImageFromCamera() {
        //        startActivity(Intent(context,OcrActivity::class.java))
        val takePictureIntent = Intent(context, OcrActivity::class.java)
        cameraResultLauncher1.launch(takePictureIntent)
    }

    private var cameraResultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val text = result.data!!.getStringExtra("SCAN_TEXT")
                val currentPItemTitle = apDescriptionView.text.toString().trim()
                val stringBuilder = java.lang.StringBuilder()
                stringBuilder.append(currentPItemTitle)
                stringBuilder.append(text)
                apDescriptionView.setText(stringBuilder.toString())
                appSettings.putString("AP_PRODUCT_DESCRIPTION",apDescriptionView.text.toString().trim())
            }
        }

    fun updateTestData(text:String){

        if (appSettings.getString("AP_PRODUCT_DESCRIPTION")!!.isNotEmpty()){
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setCancelable(false)
            builder.setNegativeButton(requireActivity().resources.getString(R.string.cancel_text)){dialog,which->
                dialog.dismiss()
            }
            builder.setPositiveButton(requireActivity().resources.getString(R.string.erase)){dialog,which->
                apDescriptionView.setText(text)
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionDefaultValueMessage.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                dialog.dismiss()
            }
            builder.setMessage("Description already have data, Are you sure you want to erase data?")
            val alert = builder.create()
            alert.show()
        }


    }

}