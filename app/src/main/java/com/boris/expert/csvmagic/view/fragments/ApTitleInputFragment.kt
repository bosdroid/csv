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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.FieldListsAdapter
import com.boris.expert.csvmagic.model.ListItem
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.activities.*
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
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


class ApTitleInputFragment : Fragment() {

    private lateinit var apTitleVoiceRecView: LinearLayout
    private lateinit var apTitleImageRecView: LinearLayout
    private lateinit var apTitleCameraRecView: LinearLayout
    private lateinit var apTitleDefaultValueMessage: MaterialTextView
    private lateinit var apTitleDefaultInputWrapper: TextInputLayout
    private lateinit var apTitleDefaultInputBox: TextInputEditText
    private lateinit var apTitleListBtn: MaterialButton
    private lateinit var apTitleSpinner: AppCompatSpinner
    private lateinit var apTitleViewWrapper: TextInputLayout
    private lateinit var apTitleActiveListNameView: MaterialTextView
    private lateinit var apTitleListSpinner: AppCompatSpinner
    private lateinit var appSettings: AppSettings
    private lateinit var apTitleView: TextInputEditText
    private lateinit var tableGenerator: TableGenerator
    private var listId: Int? = null
    private lateinit var adapter: FieldListsAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private var userCurrentCredits = ""
    private var voiceLanguageCode = "en"
//    private lateinit var getTitleBtn:MaterialTextView
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
        val v = inflater.inflate(R.layout.fragment_ap_title_input, container, false)

        initViews(v)

        return v
    }

    override fun onResume() {
        super.onResume()
        apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
    }

    private fun initViews(view: View) {

        apTitleView = view.findViewById(R.id.ap_title)
        apTitleViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_title_wrapper)
//        getTitleBtn  = view.findViewById(R.id.get_title_text_view)
        apTitleSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_title_options_spinner)
        apTitleListBtn = view.findViewById<MaterialButton>(R.id.ap_title_list_with_fields_btn)
        apTitleDefaultInputBox =
                view.findViewById<TextInputEditText>(R.id.ap_title_non_changeable_default_text_input)
        apTitleDefaultInputWrapper =
            view.findViewById<TextInputLayout>(R.id.ap_title_non_changeable_default_text_input_wrapper)
        apTitleDefaultValueMessage =
                view.findViewById<MaterialTextView>(R.id.ap_title_default_value_message)
        apTitleListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_title_list_spinner)
        apTitleActiveListNameView = view.findViewById<MaterialTextView>(R.id.ap_title_active_list_name)

        apTitleCameraRecView = view.findViewById<LinearLayout>(R.id.ap_title_camera_layout)
        apTitleImageRecView = view.findViewById<LinearLayout>(R.id.ap_title_images_layout)
        apTitleVoiceRecView = view.findViewById<LinearLayout>(R.id.ap_title_voice_layout)

        val apTitleSpinnerSelectedPosition =
                appSettings.getInt("AP_TITLE_SPINNER_SELECTED_POSITION")
        val apTitleDefaultValue = appSettings.getString("AP_TITLE_DEFAULT_VALUE")
        val apTitleListId = appSettings.getInt("AP_TITLE_LIST_ID")
        val apTitleActiveListName = appSettings.getString("AP_TITLE_LIST_NAME")
        if (apTitleActiveListName!!.isEmpty()) {
            apTitleActiveListNameView.text = "Active List: None"
        } else {
            apTitleActiveListNameView.text = "Active List: $apTitleActiveListName"
        }
        apTitleSpinner.setSelection(apTitleSpinnerSelectedPosition)

        apTitleView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                appSettings.putString("AP_PRODUCT_TITLE", s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        sharedViewModel.getTitleValue().observe(viewLifecycleOwner, Observer { updateTitle ->
            apTitleView.setText(updateTitle)
        })

//        getTitleBtn.setOnClickListener {
//            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//
//            if (userCurrentCredits.toFloat() >= 1.0) {
//
//                launchActivity.launch(
//                        Intent(
//                                requireActivity(),
//                                RainForestApiActivity::class.java
//                        )
//                )
//            } else {
//                MaterialAlertDialogBuilder(requireActivity())
//                        .setMessage(getString(R.string.low_credites_error_message2))
//                        .setCancelable(false)
//                        .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                            dialog.dismiss()
//                        }
//                        .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                            dialog.dismiss()
//                            startActivity(Intent(context, UserScreenActivity::class.java))
//                        }
//                        .create().show()
//            }
//        }

        apTitleCameraRecView.setOnClickListener {
            if (RuntimePermissionHelper.checkCameraPermission(
                            requireActivity(), Constants.CAMERA_PERMISSION
                    )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apTitleCameraRecView)
                pickImageFromCamera()
            }
        }
        apTitleImageRecView.setOnClickListener {
            Constants.hint = "ap_title"
            if (RuntimePermissionHelper.checkCameraPermission(
                            requireActivity(),
                            Constants.READ_STORAGE_PERMISSION
                    )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apTitleImageRecView)
                pickImageFromGallery()
            }
        }
        apTitleVoiceRecView.setOnClickListener {
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


        apTitleListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_title")
        }

        when (apTitleSpinnerSelectedPosition) {
            1 -> {
//                BaseActivity.showSoftKeyboard(requireActivity(),apTitleDefaultInputBox)
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.VISIBLE
                apTitleDefaultValueMessage.visibility = View.VISIBLE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleDefaultInputBox.setText(apTitleDefaultValue)
                apTitleView.setText(apTitleDefaultValue)

            }
            2 -> {
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleDefaultValueMessage.visibility = View.GONE
                apTitleListBtn.visibility = View.VISIBLE
                apTitleActiveListNameView.visibility = View.VISIBLE
                apTitleViewWrapper.visibility = View.GONE
                apTitleListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apTitleListId)
                val listValues = listOptions.split(",")
                if (listValues.isNotEmpty()) {
                    appSettings.putString("AP_PRODUCT_TITLE", listValues[0])
                }
                val apTitleSpinnerAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        listValues
                )
                apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apTitleListSpinner.adapter = apTitleSpinnerAdapter

                apTitleListSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                            ) {
                                appSettings.putString("AP_PRODUCT_TITLE", parent!!.selectedItem.toString())

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

            }
            3 -> {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleDefaultValueMessage.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleVoiceRecView.visibility = View.VISIBLE
            }
            4 -> {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleDefaultValueMessage.visibility = View.GONE
                apTitleVoiceRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleCameraRecView.visibility = View.VISIBLE
            }
            5 -> {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleDefaultValueMessage.visibility = View.GONE
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleImageRecView.visibility = View.VISIBLE
            }
            else -> {
//                BaseActivity.showSoftKeyboard(requireActivity(),apTitleView)
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleDefaultValueMessage.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE

            }
        }

        apTitleDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                apTitleView.setText(s.toString())
                appSettings.putString("AP_TITLE_DEFAULT_VALUE", s.toString())
                appSettings.putString("AP_PRODUCT_TITLE", s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        apTitleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                appSettings.putInt("AP_TITLE_SPINNER_SELECTED_POSITION", position)
                when (position) {
                    1 -> {
//                        BaseActivity.showSoftKeyboard(requireActivity(),apTitleDefaultInputBox)
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.VISIBLE
                        apTitleDefaultValueMessage.visibility = View.VISIBLE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleDefaultInputBox.setText(apTitleDefaultValue)
                        apTitleView.setText(apTitleDefaultValue)

                    }
                    2 -> {
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleDefaultValueMessage.visibility = View.GONE
                        apTitleListBtn.visibility = View.VISIBLE
                        apTitleActiveListNameView.visibility = View.VISIBLE
                        apTitleViewWrapper.visibility = View.GONE
                        apTitleListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apTitleListId)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()) {
                            appSettings.putString("AP_PRODUCT_TITLE", listValues[0])
                        }
                        val apTitleSpinnerAdapter = ArrayAdapter(
                                requireActivity(),
                                android.R.layout.simple_spinner_item,
                                listValues
                        )
                        apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apTitleListSpinner.adapter = apTitleSpinnerAdapter

                        apTitleListSpinner.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                            parent: AdapterView<*>?,
                                            view: View?,
                                            position: Int,
                                            id: Long
                                    ) {
                                        appSettings.putString("AP_PRODUCT_TITLE", parent!!.selectedItem.toString())
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {

                                    }

                                }

                    }
                    3 -> {
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleDefaultValueMessage.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleVoiceRecView.visibility = View.VISIBLE
                    }
                    4 -> {
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleDefaultValueMessage.visibility = View.GONE
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleCameraRecView.visibility = View.VISIBLE
                    }
                    5 -> {
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleDefaultValueMessage.visibility = View.GONE
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleImageRecView.visibility = View.VISIBLE
                    }
                    else -> {
//                        BaseActivity.showSoftKeyboard(requireActivity(),apTitleView)
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleDefaultValueMessage.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE

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
                    appSettings.putInt("AP_TITLE_LIST_ID", listId!!)
                    appSettings.putString("AP_TITLE_LIST_NAME", listValue.value)
                    apTitleActiveListNameView.text = "Active List: ${listValue.value}"
                    val listOptions: String = tableGenerator.getListValues(listId!!)
                    val listValues = listOptions.split(",")
                    if (listValues.isNotEmpty()) {
                        appSettings.putString("AP_PRODUCT_TITLE", listValues[0])
                    }
                    val apTitleSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                    )
                    apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    apTitleListSpinner.adapter = apTitleSpinnerAdapter

                    apTitleListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                ) {
                                    appSettings.putString("AP_PRODUCT_TITLE", parent!!.selectedItem.toString())
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }
                    //appSettings.putString("AP_PRODUCT_TITLE",list.split(",")[0])
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
        val listValueInputBox =
                listValueLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
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

                    if (data != null && data.hasExtra("TITLE")) {
                        val title = data.getStringExtra("TITLE") as String
                        if (title.isNotEmpty()) {

                            val currentPItemDescription = apTitleView.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemDescription)
                            stringBuilder.append(title)
                            apTitleView.setText(stringBuilder.toString())
                            appSettings.putString("AP_PRODUCT_TITLE",apTitleView.text.toString().trim())

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
                    val currentPItemTitle = apTitleView.text.toString().trim()
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append(currentPItemTitle)
                    stringBuilder.append(spokenText)
                    apTitleView.setText(stringBuilder.toString())
                    appSettings.putString("AP_PRODUCT_TITLE", apTitleView.text.toString().trim())

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

        if (Constants.hint == "ap_title" && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imgUri = result.uri
            try {
                TextRecogniser.runTextRecognition(requireActivity(), apTitleView, imgUri)
                Handler(Looper.myLooper()!!).postDelayed({
                    appSettings.putString("AP_PRODUCT_TITLE", apTitleView.text.toString().trim())
                }, 2000)
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
                    val currentPItemTitle = apTitleView.text.toString().trim()
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append(currentPItemTitle)
                    stringBuilder.append(text)
                    apTitleView.setText(stringBuilder.toString())
                    appSettings.putString("AP_PRODUCT_TITLE", apTitleView.text.toString().trim())
                }
            }

    fun updateTestData(text:String){
        if (appSettings.getString("AP_PRODUCT_TITLE")!!.isNotEmpty()){
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setCancelable(false)
            builder.setNegativeButton(requireActivity().resources.getString(R.string.cancel_text)){dialog,which->
                dialog.dismiss()
            }
            builder.setPositiveButton(requireActivity().resources.getString(R.string.erase)){dialog,which->
                apTitleView.setText(text)
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleDefaultValueMessage.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                dialog.dismiss()
            }
            builder.setMessage("Title already have data, Are you sure you want to erase data?")
            val alert = builder.create()
            alert.show()
        }

    }
}