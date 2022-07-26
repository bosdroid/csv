package com.boris.expert.csvmagic.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.InternetImageAdapter
import com.boris.expert.csvmagic.adapters.RainForestApiAdapter
import com.boris.expert.csvmagic.adapters.RainForestProductImageAdapter
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.interfaces.TranslationCallback
import com.boris.expert.csvmagic.model.RainForestApiObject
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.fragments.FullImageFragment
import com.boris.expert.csvmagic.view.fragments.InsalesFragment
import com.boris.expert.csvmagic.viewmodel.SharedViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.cloud.translate.Detection
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.database.FirebaseDatabase
import com.skydoves.balloon.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apmem.tools.layouts.FlowLayout
import org.json.JSONObject
import java.lang.StringBuilder
import java.text.BreakIterator
import java.util.*


class RainForestApiActivity : BaseActivity(), RainForestApiAdapter.OnItemClickListener,
    View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var rainForestRecyclerView: RecyclerView
    private lateinit var adapter: RainForestApiAdapter
    private var rainForestList = mutableListOf<RainForestApiObject>()
    private lateinit var searchBox: TextInputEditText
    private lateinit var searchImageBtn: ImageButton
    private lateinit var appSettings: AppSettings
    private lateinit var voiceSearchIcon: AppCompatImageView
    private lateinit var barcodeSearchIcon: AppCompatImageView
    private var pDetailsPrice = 0F
    private var pListPrice = 0F
    private var characters = 0
    private var translatorPrice = 0F
    private var unitCharacterPrice = 0F
    private var userCurrentCredits = ""
    private var howMuchChargeCredits = 0F
    private var voiceLanguageCode = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rain_forest_api)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        toolbar = findViewById(R.id.toolbar)
        rainForestRecyclerView = findViewById(R.id.rainforest_result_recyclerview)
        searchBox = findViewById(R.id.rainforest_products_search_box)
        searchImageBtn = findViewById(R.id.rainforest_products_search_btn)
        searchImageBtn.setOnClickListener(this)
        voiceSearchIcon = findViewById(R.id.rain_forest_voice_search_icon)
        barcodeSearchIcon = findViewById(R.id.rain_forest_barcode_search_icon)

        rainForestRecyclerView.layoutManager = GridLayoutManager(context, 2)
        rainForestRecyclerView.hasFixedSize()
        adapter = RainForestApiAdapter(context, rainForestList as ArrayList<RainForestApiObject>)
        rainForestRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)

        pDetailsPrice = appSettings.getString("P_DETAILS_PRICE")!!.toFloat()
        pListPrice = appSettings.getString("P_LIST_PRICE")!!.toFloat()
        characters = appSettings.getInt("TRANSLATOR_CHARACTERS_LIMIT")
        translatorPrice = appSettings.getString("TRANSLATOR_CHARACTERS_PRICE")!!.toFloat()
        unitCharacterPrice = translatorPrice / characters
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

        searchBox.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = searchBox.text.toString().trim()
                    if (query.isNotEmpty()) {
                        hideSoftKeyboard(context, searchBox)
                        searchBox.clearFocus()
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                        startLoading(context)
                        CoroutineScope(Dispatchers.IO).launch {
//                        System.setProperty("GOOGLE_API_KEY",context.resources.getString(R.string.translation_api_key))
//                        val translate = TranslateOptions.getDefaultInstance().service
//                        val detection: Detection = translate.detect(query)
                            val detectedLanguage = "en"//detection.language
                            if (detectedLanguage == "en") {
                                CoroutineScope(Dispatchers.Main).launch {
                                    getProducts(query)
                                }
                            } else {
                                val totalCreditPrice = unitCharacterPrice * query.length
                                val total = totalCreditPrice + pListPrice
                                howMuchChargeCredits = total
                                CoroutineScope(Dispatchers.IO).launch {
                                    GcpTranslator.translateFromRusToEng(
                                        context,
                                        query,
                                        object : TranslationCallback {
                                            override fun onTextTranslation(translatedText: String) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    if (translatedText.isNotEmpty()) {
                                                        //showAlert(context,translatedText)
                                                        getProducts(translatedText)
                                                    } else {
                                                        dismiss()
                                                        showAlert(
                                                            context,
                                                            getString(R.string.something_wrong_with_translator_error)
                                                        )
                                                    }
                                                }

                                            }

                                        })
                                }
                            }
                        }

                    } else {
                        showAlert(context, getString(R.string.empty_text_error))
                    }
                    return true
                }
                return false
            }

        })

        voiceSearchIcon.setOnClickListener {
            voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
            val voiceLayout =
                LayoutInflater.from(context).inflate(R.layout.voice_language_setting_layout, null)
            val voiceLanguageSpinner =
                voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
            val voiceLanguageSaveBtn =
                voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

            if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                voiceLanguageSpinner.setSelection(0, false)
            } else {
                voiceLanguageSpinner.setSelection(1, false)
            }

            voiceLanguageSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        voiceLanguageCode =
                            if (parent!!.selectedItem.toString().toLowerCase(Locale.ENGLISH)
                                    .contains("english")
                            ) {
                                "en"
                            } else {
                                "ru"
                            }
                        appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            val builder = MaterialAlertDialogBuilder(context)
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

        barcodeSearchIcon.setOnClickListener {
            val intent = Intent(context, BarcodeReaderActivity::class.java)
            barcodeImageResultLauncher.launch(intent)
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private var barcodeImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.hasExtra("SCANNED_BARCODE_VALUE")) {
                    val barcodeId =
                        result.data!!.getStringExtra("SCANNED_BARCODE_VALUE") as String
                    if (barcodeId.isNotEmpty()) {
                        searchBox.setText(barcodeId)
                        searchBox.clearFocus()
                        hideSoftKeyboard(context, searchBox)
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                        startSearch(barcodeId)
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
                            results!![0]
                        }

                searchBox.setText(spokenText)
                searchBox.clearFocus()
                hideSoftKeyboard(context, searchBox)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                startSearch(spokenText)
            }
        }

    override fun onItemClick(position: Int) {
        val item = rainForestList[position]
//        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= pDetailsPrice) {

        getProductDescription(item.asin)
//        }
//        else{
//            MaterialAlertDialogBuilder(context)
//                .setMessage(getString(R.string.low_credites_error_message))
//                .setCancelable(false)
//                .setNegativeButton(getString(R.string.no_text)){dialog,which->
//                    dialog.dismiss()
//                }
//                .setPositiveButton(getString(R.string.buy_credits)){dialog,which ->
//                    dialog.dismiss()
//                    startActivity(Intent(context,UserScreenActivity::class.java))
//                }
//                .create().show()
//        }
    }

    override fun onClick(v: View?) {
        val view = v!!
        when (view.id) {
            R.id.rainforest_products_search_btn -> {
//                userCurrentCredits = "0"
                val query = searchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideSoftKeyboard(context, searchBox)
                    searchBox.clearFocus()
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                    startSearch(query)
                } else {
                    showAlert(context, getString(R.string.empty_text_error))
                }

//                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= total) {
//                }
//                else{
//                    MaterialAlertDialogBuilder(context)
//                        .setMessage(getString(R.string.low_credites_error_message))
//                        .setCancelable(false)
//                        .setNegativeButton(getString(R.string.no_text)){dialog,which->
//                            dialog.dismiss()
//                        }
//                        .setPositiveButton(getString(R.string.buy_credits)){dialog,which ->
//                            dialog.dismiss()
//                            startActivity(Intent(context,UserScreenActivity::class.java))
//                        }
//                        .create().show()
//                }
            }
            else -> {
//                if (view.tag == "title") {
//                    val position = view.id
//                    val textView = view as MaterialTextView
//                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_positive_color))
//                    view.setTextColor(ContextCompat.getColor(context, R.color.white))
//                    val balloon = Balloon.Builder(context)
//                        .setLayout(R.layout.ballon_layout_design)
//                        .setArrowSize(10)
//                        .setArrowOrientation(ArrowOrientation.TOP)
//                        .setArrowPosition(0.5f)
//                        .setWidthRatio(0.55f)
//                        .setCornerRadius(4f)
//                        .setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
//                        .setBalloonAnimation(BalloonAnimation.ELASTIC)
//                        .setLifecycleOwner(this@RainForestApiActivity)
//                        .build()
//                    val editTextBox = balloon.getContentView().findViewById<TextInputEditText>(R.id.balloon_edit_text)
//                    editTextBox.setText(textView.text.toString().trim())
//                    val closeBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_close_btn)
//                    val applyBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_apply_btn)
//                    balloon.showAlignTop(textView,)
//                    editTextBox.requestFocus()
//                    Constants.openKeyboar(context)
//                    closeBtn.setOnClickListener {
//                        Constants.hideKeyboar(context)
//                        balloon.dismiss()
//                        view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                        view.setTextColor(ContextCompat.getColor(context, R.color.black))
//                    }
//                    applyBtn.setOnClickListener {
//                        Constants.hideKeyboar(context)
//                        balloon.dismiss()
//                        //val tempText = textView.replace(mWord,editTextBox.text.toString().trim())
//                        textView.text = editTextBox.text.toString().trim()
//                        view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                        view.setTextColor(ContextCompat.getColor(context, R.color.black))
//
//                    }
//                }
            }
        }
    }


    private fun startSearch(query: String) {
        startLoading(context)
        CoroutineScope(Dispatchers.IO).launch {
//                        System.setProperty("GOOGLE_API_KEY",context.resources.getString(R.string.translation_api_key))
//                        val translate = TranslateOptions.getDefaultInstance().service
//                        val detection: Detection = translate.detect(query)
            val detectedLanguage = "en"//detection.language
            if (detectedLanguage == "en") {
                CoroutineScope(Dispatchers.Main).launch {
                    getProducts(query)
                }
            } else {
                val totalCreditPrice = unitCharacterPrice * query.length
                val total = totalCreditPrice + pListPrice
                howMuchChargeCredits = total
                CoroutineScope(Dispatchers.IO).launch {
                    GcpTranslator.translateFromRusToEng(
                        context,
                        query,
                        object : TranslationCallback {
                            override fun onTextTranslation(translatedText: String) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (translatedText.isNotEmpty()) {
                                        //showAlert(context,translatedText)
                                        getProducts(translatedText)
                                    } else {
                                        dismiss()
                                        showAlert(
                                            context,
                                            getString(R.string.something_wrong_with_translator_error)
                                        )
                                    }
                                }

                            }

                        })
                }
            }
        }
    }

    private fun getProducts(query: String) {

        val url =
            "https://api.rainforestapi.com/request?api_key=2ADA91B95479431FAFCDEDFA36717046&type=search&amazon_domain=amazon.com&search_term=$query"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {

                val response = JSONObject(it)
                if (response.has("search_results")) {
                    chargeCreditsPrice()
                    val searchResults = response.getJSONArray("search_results")
                    if (searchResults.length() > 0) {
                        rainForestList.clear()
                        var totalCharacters = 0
                        for (i in 0 until searchResults.length()) {
                            val item = searchResults.getJSONObject(i)
                            rainForestList.add(
                                RainForestApiObject(
                                    item.getString("asin"),
                                    item.getString("image"),
                                    item.getString("title")
                                )
                            )
                            totalCharacters += item.getString("title").length
                        }
                        val totalCreditPrice = unitCharacterPrice * totalCharacters
                        howMuchChargeCredits = totalCreditPrice
                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                        userCurrentCredits = "0"
//                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= totalCreditPrice) {
                        try {

                            for (i in 0 until rainForestList.size) {
                                val text = rainForestList[i].title
                                GcpTranslator.translateFromEngToRus(
                                    context,
                                    text,
                                    object : TranslationCallback {
                                        override fun onTextTranslation(translatedText: String) {
                                            rainForestList[i].title = translatedText
                                            adapter.notifyItemChanged(i)
                                        }

                                    })
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        dismiss()
//                        if (rainForestList.size > 0) {
//                            adapter.notifyItemRangeChanged(0,rainForestList.size)
//                        }
//                        CoroutineScope(Dispatchers.Main).launch {
//                            dismiss()
//                            if (rainForestList.size > 0) {
//                                adapter.notifyDataSetChanged()
//                            }
//                        }
                        chargeCreditsPrice()
//                        CoroutineScope(Dispatchers.IO).launch {
//                            try {
//                                for (i in 0 until rainForestList.size) {
//                                        val text = rainForestList[i].title
//                                        GcpTranslator.translateFromEngToRus(
//                                            context,
//                                            text,
//                                            object : TranslationCallback {
//                                                override fun onTextTranslation(translatedText: String) {
//                                                    rainForestList[i].title = translatedText
//                                                }
//
//                                            })
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//
//                            CoroutineScope(Dispatchers.Main).launch {
//                                dismiss()
//                                if (rainForestList.size > 0) {
//                                    adapter.notifyDataSetChanged()
//                                }
//                            }
//                            chargeCreditsPrice()
//                        }
//                        }
//                        else{
//                            if (rainForestList.size > 0) {
//                                adapter.notifyItemRangeChanged(0, rainForestList.size)
//                            }
//                            MaterialAlertDialogBuilder(context)
//                                .setMessage(getString(R.string.low_credites_error_message))
//                                .setCancelable(false)
//                                .setNegativeButton(getString(R.string.no_text)){dialog,which->
//                                    dialog.dismiss()
//                                }
//                                .setPositiveButton(getString(R.string.buy_credits)){dialog,which ->
//                                    dialog.dismiss()
//                                    startActivity(Intent(context,UserScreenActivity::class.java))
//                                }
//                                .create().show()
//                        }
                    } else {
                        dismiss()
                    }
                }
            },
            {
                Log.d("TEST199", it.localizedMessage!!)
            })

        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }

        VolleySingleton(context).addToRequestQueue(stringRequest)
    }

    private fun chargeCreditsPrice() {
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        val remaining = userCurrentCredits.toFloat() - howMuchChargeCredits
        userCurrentCredits = remaining.toString()
        hashMap["credits"] = userCurrentCredits
        firebaseDatabase.child(Constants.firebaseUserCredits)
            .child(Constants.firebaseUserId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                howMuchChargeCredits = 0F
                getUserCredits(
                    context
                )
            }
            .addOnFailureListener {

            }
    }

    private fun getProductDescription(asin: String) {
        startLoading(context)
        val url =
            "https://api.rainforestapi.com/request?api_key=2ADA91B95479431FAFCDEDFA36717046&type=product&amazon_domain=amazon.com&asin=$asin"
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            {
                dismiss()
                val descriptionBuilder = StringBuilder()
                val response = JSONObject(it)
                if (response.has("product")) {
                    val productResults = response.getJSONObject("product")
                    val title = productResults.getString("title")
                    if (productResults.has("specifications_flat")) {
                        descriptionBuilder.append(productResults.getString("specifications_flat"))
                        descriptionBuilder.append("\n\n")
                    }

                    if (productResults.has("feature_bullets_flat")) {
                        descriptionBuilder.append(productResults.getString("feature_bullets_flat"))
                        descriptionBuilder.append("\n\n")
                    }

                    if (productResults.has("feature_bullets")) {
                        val featureBullets = productResults.getJSONArray("feature_bullets")
                        for (i in 0 until featureBullets.length()) {
                            descriptionBuilder.append(featureBullets[i])
                            descriptionBuilder.append("\n")
                        }
                    }

                    if (productResults.has("description")) {
                        descriptionBuilder.append("\n\n")
                        descriptionBuilder.append(productResults.getString("description"))
                    }


                    val description = if (descriptionBuilder.toString().isEmpty()) {
                        ""
                    } else {
                        descriptionBuilder.toString()
                    }

                    val imageList = if (productResults.has("images_flat")) {
                        productResults.getString("images_flat")
                    } else {
                        ""
                    }

                    CustomDialog(
                        title,
                        description,
                        imageList,
                        userCurrentCredits,
                        unitCharacterPrice,
                        howMuchChargeCredits
                    ).show(supportFragmentManager, "dialog")

//                        val layoutBuilder = MaterialAlertDialogBuilder(context)
//                        val dialogLayout = LayoutInflater.from(context)
//                                .inflate(R.layout.rain_forest_title_description_layout, null)
//                        val dialogCloseBtn =
//                                dialogLayout.findViewById<AppCompatImageView>(R.id.dialog_close_btn)
//                        val titleTextView =
//                                dialogLayout.findViewById<MaterialTextView>(R.id.title_text_view)
//                        val descriptionTextView =
//                                dialogLayout.findViewById<MaterialTextView>(R.id.description_text_view)
//                        val titleAddBtn =
//                                dialogLayout.findViewById<MaterialTextView>(R.id.add_title_button)
//                        val descriptionAddBtn =
//                                dialogLayout.findViewById<MaterialTextView>(R.id.add_description_button)
//                        val doneBtn = dialogLayout.findViewById<MaterialButton>(R.id.rfa_item_done_btn)
//                        val dynamicTitleTextViewWrapper = dialogLayout.findViewById<FlowLayout>(R.id.dynamic_textview_wrapper)
//                        val dynamicDescriptionTextViewWrapper = dialogLayout.findViewById<FlowLayout>(R.id.dynamic_description_textview_wrapper)
//
//                        titleAddBtn.isEnabled = true
//                        descriptionAddBtn.isEnabled = true
//
//                        layoutBuilder.setView(dialogLayout)
//                        layoutBuilder.setCancelable(false)
//                        val alert = layoutBuilder.create()
//                        alert.show()
//                        dialogCloseBtn.setOnClickListener {
//                            alert.dismiss()
//                        }
//
//                        doneBtn.setOnClickListener {
//                            alert.dismiss()
//                            val finalTitleText = if (titleAddBtn.isEnabled) {
//                                ""
//                            } else {
//                                val stringBuilder = StringBuilder()
//                                for (i in 0 until titleTextViewList.size){
//                                    val titleItem = titleTextViewList[i]
//                                    stringBuilder.append(titleItem.text.toString())
//                                    stringBuilder.append(" ")
//                                }
//
//                                stringBuilder.toString().trim()
//                            }
//                            val finalDescriptionText = if (descriptionAddBtn.isEnabled) {
//                                ""
//                            } else {
//                                val stringBuilder = StringBuilder()
//                                for (i in 0 until descriptionTextViewList.size){
//                                    val titleItem = descriptionTextViewList[i]
//                                    stringBuilder.append(titleItem.text.toString())
//                                    stringBuilder.append(" ")
//                                }
//
//                                stringBuilder.toString().trim()
//                                //descriptionTextView.text.toString()
//                            }
//                            setResult(RESULT_OK, Intent().apply {
//                                putExtra("TITLE", finalTitleText)
//                                putExtra("DESCRIPTION", finalDescriptionText)
//                            })
//                            finish()
//                        }
//                        val totalCharacters = title.length + description.length
//                        val totalCreditPrice = unitCharacterPrice * totalCharacters
//                        howMuchChargeCredits = totalCreditPrice
//                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
////                    userCurrentCredits = "0"
////                    if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= totalCreditPrice) {
//
//                        GcpTranslator.translateFromEngToRus(
//                                context,
//                                title,
//                                object : TranslationCallback {
//                                    override fun onTextTranslation(translatedText: String) {
//                                        if (translatedText.isNotEmpty()) {
//                                            titleTextView.text = translatedText
//
//                                            val textList = translatedText.split(" ")
//
//                                            titleTextViewList.clear()
//                                            for (i in 0 until textList.size) {
//                                                val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
//                                                        FlowLayout.LayoutParams.WRAP_CONTENT)
//                                                params.setMargins(5, 5, 5, 5)
//                                                val textView = MaterialTextView(context)
//                                                textView.layoutParams = params
//                                                textView.text = textList[i]
//                                                textView.tag = "title"
//                                                textView.id = i
//                                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
//                                                textView.setTextColor(ContextCompat.getColor(context,R.color.black))
//                                                titleTextViewList.add(textView)
//                                                textView.setOnClickListener(this@RainForestApiActivity)
//                                                dynamicTitleTextViewWrapper.addView(textView)
//                                            }
//
//
//                                        } else {
//                                            titleTextView.text = ""
//                                        }
//                                    }
//
//                                })
//
////                        initSelectebleWord(titleTextView.text.toString(), titleTextView)
////                        titleTextView.setOnTouchListener(LinkMovementMethodOverride())
//
//
//                        if (description.isNotEmpty()) {
//                            GcpTranslator.translateFromEngToRus(
//                                    context,
//                                    description,
//                                    object : TranslationCallback {
//                                        override fun onTextTranslation(translatedText: String) {
//                                            if (translatedText.isNotEmpty()) {
//                                                descriptionTextView.text = translatedText
//
//                                                val textList = translatedText.split(" ")
//
//                                                descriptionTextViewList.clear()
//                                                for (i in 0 until textList.size) {
//                                                    val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
//                                                            FlowLayout.LayoutParams.WRAP_CONTENT)
//                                                    params.setMargins(5, 5, 5, 5)
//                                                    val textView = MaterialTextView(context)
//                                                    textView.layoutParams = params
//                                                    textView.text = textList[i]
//                                                    textView.tag = "title"
//                                                    textView.id = i
//                                                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
//                                                    textView.setTextColor(ContextCompat.getColor(context,R.color.black))
//                                                    descriptionTextViewList.add(textView)
//                                                    textView.setOnClickListener(this@RainForestApiActivity)
//                                                    dynamicDescriptionTextViewWrapper.addView(textView)
//                                                }
//
//                                            } else {
//                                                descriptionTextView.text = ""
//                                            }
//                                        }
//
//                                    })
//                        }else{
//                            val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
//                                FlowLayout.LayoutParams.WRAP_CONTENT)
//                            params.setMargins(5, 5, 5, 5)
//                            val textView = MaterialTextView(context)
//                            textView.layoutParams = params
//                            textView.text = "Nothing to show"
//                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
//                            textView.setTextColor(ContextCompat.getColor(context,R.color.red))
//                            dynamicDescriptionTextViewWrapper.addView(textView)
//                        }
////                        initSelectebleWord(descriptionTextView.text.toString(), descriptionTextView)
////                        descriptionTextView.setOnTouchListener(LinkMovementMethodOverride())
//                        chargeCreditsPrice()
////                    }
////                    else{
////                        titleTextView.text = title
////                        descriptionTextView.text = description
////                        MaterialAlertDialogBuilder(context)
////                                .setMessage(getString(R.string.low_credites_error_message))
////                                .setCancelable(false)
////                                .setNegativeButton(getString(R.string.no_text)){dialog,which->
////                                    dialog.dismiss()
////                                }
////                                .setPositiveButton(getString(R.string.buy_credits)){dialog,which ->
////                                    dialog.dismiss()
////                                    startActivity(Intent(context,UserScreenActivity::class.java))
////                                }
////                                .create().show()
////                    }
//
//                        titleAddBtn.setOnClickListener {
//                            val builder = MaterialAlertDialogBuilder(context)
//                            builder.setMessage(getString(R.string.apply_warning_message))
//                            builder.setCancelable(false)
//                            builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
//                                dialog.dismiss()
//                            }
//                            builder.setPositiveButton(getString(R.string.apply_text)) { dialog, which ->
//                                dialog.dismiss()
//                                titleAddBtn.text = getString(R.string.added_text)
//                                titleAddBtn.isEnabled = false
//                                doneBtn.visibility = View.VISIBLE
//                            }
//
//                            val alert1 = builder.create()
//                            alert1.show()
//
//                        }
//
//                        descriptionAddBtn.setOnClickListener {
//                            if (description.isNotEmpty()) {
//                                val builder = MaterialAlertDialogBuilder(context)
//                                builder.setMessage(getString(R.string.apply_warning_message))
//                                builder.setCancelable(false)
//                                builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
//                                    dialog.dismiss()
//                                }
//                                builder.setPositiveButton(getString(R.string.apply_text)) { dialog, which ->
//                                    dialog.dismiss()
//                                    descriptionAddBtn.text = getString(R.string.added_text)
//                                    descriptionAddBtn.isEnabled = false
//                                    doneBtn.visibility = View.VISIBLE
//                                }
//
//                                val alert1 = builder.create()
//                                alert1.show()
//                            }
//                        }

                }
            },
            {
                Log.d("TEST199", it.localizedMessage!!)
            })

        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }

        VolleySingleton(context).addToRequestQueue(stringRequest)
    }

    private fun initSelectebleWord(entireContent: String, textView: MaterialTextView) {
        //First we trim the text and remove the spaces at start and end.
        val definition = entireContent.trim()
        //And then  set the textview movement method to prevent frezing
        //And we set the text as SPANNABLE text.
        //val definitionView = findViewById<View>(R.id.lblContent) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(definition, TextView.BufferType.SPANNABLE)
        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        //After we get the spans of the text with iterator and we initialized the iterator
        val spans = textView.text as Spannable
        val iterator: BreakIterator = BreakIterator.getWordInstance(Locale.US)
        iterator.setText(definition)
        var start: Int = iterator.first()

        //Here we get all possible words by iterators
        var end: Int = iterator.next()
        while (end != BreakIterator.DONE) {
            val possibleWord = definition.substring(start, end)
            if (Character.isLetterOrDigit(possibleWord[0])) {
                val clickSpan = getClickableSpan(possibleWord, definition, spans)
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            start = end
            end = iterator.next()
        }
    }

    var i = 0
    private fun getClickableSpan(word: String, text: String, spans: Spannable): ClickableSpan {
        return object : ClickableSpan() {

            var mWord: String = ""
            var ds: TextPaint? = null

            init {
                mWord = word
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                this.ds = ds
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(context, R.color.black)


            }

            override fun onClick(widget: View) {

                val textView = widget as MaterialTextView
                if (widget.isPressed) {
                    ds!!.bgColor = ContextCompat.getColor(context, R.color.primary_positive_color)
                } else {
                    ds!!.bgColor = ContextCompat.getColor(context, R.color.white)
                }
//                i +=1
//                val tempText = text.replace(mWord,"$i")
//                textView.text = tempText
//                initSelectebleWord(textView.text.toString(),textView)
//                textView.setOnTouchListener(LinkMovementMethodOverride())

                val balloon = Balloon.Builder(context)
                    .setLayout(R.layout.ballon_layout_design)
                    .setArrowSize(10)
                    .setArrowOrientation(ArrowOrientation.TOP)
                    .setArrowPosition(0.1f)
                    .setWidthRatio(0.55f)
                    .setCornerRadius(4f)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
                    .setBalloonAnimation(BalloonAnimation.ELASTIC)
                    .setLifecycleOwner(this@RainForestApiActivity)
                    .build()
                val editTextBox =
                    balloon.getContentView().findViewById<TextInputEditText>(R.id.balloon_edit_text)
                editTextBox.setText(mWord)
                val closeBtn =
                    balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_close_btn)
                val applyBtn =
                    balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_apply_btn)
                balloon.showAlignTop(widget)
                editTextBox.requestFocus()
                Constants.openKeyboar(context)
                closeBtn.setOnClickListener {
                    Constants.hideKeyboar(context)
                    balloon.dismiss()
                }
                applyBtn.setOnClickListener {
                    Constants.hideKeyboar(context)
                    balloon.dismiss()
                    val tempText = text.replace(mWord, editTextBox.text.toString().trim())
                    textView.text = tempText
                    initSelectebleWord(textView.text.toString(), textView)
                    textView.setOnTouchListener(LinkMovementMethodOverride())
                }

            }

        }
    }

    open class CustomDialog(
        private val title: String,
        private val description: String,
        private val imageList: String,
        private var userCurrentCredits: String,
        private val unitCharacterPrice: Float,
        private var howMuchChargeCredits: Float
    ) : DialogFragment(), View.OnClickListener {

        private var titleTextViewList = mutableListOf<TextView>()
        private var descriptionTextViewList = mutableListOf<TextView>()
        private var rainForestApiInstance: RainForestApiActivity? = null
        private lateinit var sharedViewModel: SharedViewModel
        private var finalTitleText = ""
        private var finalDescriptionText = ""
        private var finalSelectedImageList = mutableListOf<String>()
        private lateinit var rainForestProductImagesRecyclerView: RecyclerView
        var rainForestProductImagesList = mutableListOf<String>()
        private lateinit var rainForestProductImageAdapter: RainForestProductImageAdapter
        private lateinit var fullDescriptionBtn: AppCompatImageView

        override fun onAttach(context: Context) {
            super.onAttach(context)
            sharedViewModel = ViewModelProviders.of(
                requireActivity() as RainForestApiActivity,
                ViewModelFactory(SharedViewModel()).createFor()
            )[SharedViewModel::class.java]
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialogStyle
            )
            rainForestApiInstance = RainForestApiActivity()
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v =
                inflater.inflate(R.layout.rain_forest_title_description_layout, container)

            initViews(v)

            return v
        }

        private fun initViews(view: View) {
            val dialogCloseBtn =
                view.findViewById<AppCompatImageView>(R.id.dialog_close_btn)
            val titleTextView =
                view.findViewById<MaterialTextView>(R.id.title_text_view)
            val descriptionTextView =
                view.findViewById<MaterialTextView>(R.id.description_text_view)
            val titleAddBtn =
                view.findViewById<MaterialTextView>(R.id.add_title_button)
            val descriptionAddBtn =
                view.findViewById<MaterialTextView>(R.id.add_description_button)
            val backBtn =
                view.findViewById<MaterialButton>(R.id.rfa_item_back_btn)
            val doneBtn = view.findViewById<MaterialButton>(R.id.rfa_item_done_btn)
            val dynamicTitleTextViewWrapper =
                view.findViewById<FlowLayout>(R.id.dynamic_textview_wrapper)
            val dynamicDescriptionTextViewWrapper =
                view.findViewById<FlowLayout>(R.id.dynamic_description_textview_wrapper)
            rainForestProductImagesRecyclerView =
                view.findViewById(R.id.rainforest_product_images_recyclerview)
            fullDescriptionBtn = view.findViewById(R.id.full_description_button)

            fullDescriptionBtn.setOnClickListener {
                FullDescriptionDialogFragment(
                    description
                ).show(childFragmentManager, "dialog")
            }

            if (imageList.isNotEmpty()) {
                rainForestProductImagesList.addAll(imageList.split(","))
            }

            val layoutManager = LinearLayoutManager(requireActivity())
            layoutManager.orientation = RecyclerView.HORIZONTAL
            rainForestProductImagesRecyclerView.layoutManager = layoutManager
            rainForestProductImagesRecyclerView.hasFixedSize()
            rainForestProductImageAdapter = RainForestProductImageAdapter(
                requireActivity(),
                rainForestProductImagesList as ArrayList<String>
            )
            rainForestProductImagesRecyclerView.adapter = rainForestProductImageAdapter
            rainForestProductImageAdapter.setOnItemClickListener(object :
                RainForestProductImageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedImage = rainForestProductImagesList[position]
                    FullImageFragment(selectedImage).show(
                        childFragmentManager,
                        "full-image-dialog"
                    )
                }

                override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                    //btn.text = getString(R.string.please_wait)

                    val selectedImage = rainForestProductImagesList[position]
                    //Toast.makeText(requireActivity(),selectedImage,Toast.LENGTH_SHORT).show()
                    if (btn.text.toString()
                            .toLowerCase(Locale.ENGLISH) == "attach"
                    ) {
                        finalSelectedImageList.add(selectedImage)

                        btn.text =
                            requireActivity().resources.getString(R.string.attached_text)
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.dark_gray
                            )
                        )
                    } else {
                        btn.text =
                            requireActivity().resources.getString(R.string.attach_text)
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primary_positive_color
                            )
                        )
                        finalSelectedImageList.remove(selectedImage)
                    }
                    doneBtn.visibility = View.VISIBLE

                }

            })

            titleAddBtn.isEnabled = true
            descriptionAddBtn.isEnabled = true

            backBtn.setOnClickListener {
                dismiss()
            }

            dialogCloseBtn.setOnClickListener {
                dismiss()
            }

            doneBtn.setOnClickListener {
                finalTitleText = if (titleAddBtn.isEnabled) {
                    ""
                } else {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until titleTextViewList.size) {
                        val titleItem = titleTextViewList[i]
                        stringBuilder.append(titleItem.text.toString())
                        stringBuilder.append(" ")
                    }

                    stringBuilder.toString().trim()
                }
                finalDescriptionText = if (descriptionAddBtn.isEnabled) {
                    ""
                } else {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until descriptionTextViewList.size) {
                        val titleItem = descriptionTextViewList[i]
                        stringBuilder.append(titleItem.text.toString())
                        stringBuilder.append(" ")
                    }

                    stringBuilder.toString().trim()
                    //descriptionTextView.text.toString()
                }
                if (Constants.selectedRainForestProductImages.isEmpty()) {
                    Constants.selectedRainForestProductImages =
                        if (finalSelectedImageList.isEmpty()) {
                            ""
                        } else {
                            finalSelectedImageList.joinToString(",")
                        }
                } else {
                    Constants.selectedRainForestProductImages =
                        "${Constants.selectedRainForestProductImages},${
                            finalSelectedImageList.joinToString(",")
                        }"

                }
                requireActivity().setResult(RESULT_OK, Intent().apply {
                    putExtra("TITLE", finalTitleText)
                    putExtra("DESCRIPTION", finalDescriptionText)
                })
                requireActivity().finish()
                dismiss()
            }
            val totalCharacters = title.length + description.length
            val totalCreditPrice = unitCharacterPrice * totalCharacters
            howMuchChargeCredits = totalCreditPrice
            //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                    userCurrentCredits = "0"
//                    if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= totalCreditPrice) {

            GcpTranslator.translateFromEngToRus(
                requireActivity(),
                title,
                object : TranslationCallback {
                    override fun onTextTranslation(translatedText: String) {
                        if (translatedText.isNotEmpty()) {
                            titleTextView.text = translatedText

                            val textList = translatedText.split(" ")

                            titleTextViewList.clear()
                            for (i in 0 until textList.size) {
                                val params = FlowLayout.LayoutParams(
                                    FlowLayout.LayoutParams.WRAP_CONTENT,
                                    FlowLayout.LayoutParams.WRAP_CONTENT
                                )
                                params.setMargins(5, 5, 5, 5)
                                val textView = MaterialTextView(requireActivity())
                                textView.layoutParams = params
                                textView.text = textList[i]
                                textView.tag = "title"
                                textView.id = i
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.black
                                    )
                                )
                                titleTextViewList.add(textView)
                                textView.setOnClickListener(this@CustomDialog)
                                dynamicTitleTextViewWrapper.addView(textView)
                            }


                        } else {
                            titleTextView.text = ""
                        }
                    }

                })

//                        initSelectebleWord(titleTextView.text.toString(), titleTextView)
//                        titleTextView.setOnTouchListener(LinkMovementMethodOverride())


            if (description.isNotEmpty()) {
                GcpTranslator.translateFromEngToRus(
                    requireActivity(),
                    description,
                    object : TranslationCallback {
                        override fun onTextTranslation(translatedText: String) {
                            if (translatedText.isNotEmpty()) {
                                descriptionTextView.text = translatedText

                                val textList = translatedText.split(" ")

                                descriptionTextViewList.clear()
                                for (i in 0 until textList.size) {
                                    val params = FlowLayout.LayoutParams(
                                        FlowLayout.LayoutParams.WRAP_CONTENT,
                                        FlowLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    params.setMargins(5, 5, 5, 5)
                                    val textView = MaterialTextView(requireActivity())
                                    textView.layoutParams = params
                                    textView.text = textList[i]
                                    textView.tag = "title"
                                    textView.id = i
                                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                                    textView.setTextColor(
                                        ContextCompat.getColor(
                                            requireActivity(),
                                            R.color.black
                                        )
                                    )
                                    descriptionTextViewList.add(textView)
                                    textView.setOnClickListener(this@CustomDialog)
                                    dynamicDescriptionTextViewWrapper.addView(textView)
                                }

                            } else {
                                descriptionTextView.text = ""
                            }
                        }

                    })
            } else {
                val params = FlowLayout.LayoutParams(
                    FlowLayout.LayoutParams.WRAP_CONTENT,
                    FlowLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(5, 5, 5, 5)
                val textView = MaterialTextView(requireActivity())
                textView.layoutParams = params
                textView.text = getString(R.string.nothing_show_text)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red))
                dynamicDescriptionTextViewWrapper.addView(textView)
            }
//                        initSelectebleWord(descriptionTextView.text.toString(), descriptionTextView)
//                        descriptionTextView.setOnTouchListener(LinkMovementMethodOverride())
            val firebaseDatabase = FirebaseDatabase.getInstance().reference
            val hashMap = HashMap<String, Any>()
            val remaining = userCurrentCredits.toFloat() - howMuchChargeCredits
            userCurrentCredits = remaining.toString()
            hashMap["credits"] = userCurrentCredits
            firebaseDatabase.child(Constants.firebaseUserCredits)
                .child(Constants.firebaseUserId)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    howMuchChargeCredits = 0F
                    getUserCredits(
                        requireActivity()
                    )
                }
                .addOnFailureListener {

                }
//                    }
//                    else{
//                        titleTextView.text = title
//                        descriptionTextView.text = description
//                        MaterialAlertDialogBuilder(context)
//                                .setMessage(getString(R.string.low_credites_error_message))
//                                .setCancelable(false)
//                                .setNegativeButton(getString(R.string.no_text)){dialog,which->
//                                    dialog.dismiss()
//                                }
//                                .setPositiveButton(getString(R.string.buy_credits)){dialog,which ->
//                                    dialog.dismiss()
//                                    startActivity(Intent(context,UserScreenActivity::class.java))
//                                }
//                                .create().show()
//                    }

            titleAddBtn.setOnClickListener {
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setMessage(getString(R.string.apply_warning_message))
                builder.setCancelable(false)
                builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                    dialog.dismiss()
                }
                builder.setPositiveButton(getString(R.string.apply_text)) { dialog, which ->
//                    finalTitleText = if (titleAddBtn.isEnabled) {
//                        ""
//                    } else {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until titleTextViewList.size) {
                        val titleItem = titleTextViewList[i]
                        stringBuilder.append(titleItem.text.toString())
                        stringBuilder.append(" ")
                    }

                    finalTitleText = stringBuilder.toString().trim()
                    sharedViewModel.setTitleValue(finalTitleText)
//                    }
                    dialog.dismiss()
                    titleAddBtn.text = getString(R.string.added_text)
                    titleAddBtn.isEnabled = false
                    doneBtn.visibility = View.VISIBLE
                }

                val alert1 = builder.create()
                alert1.show()

            }

            descriptionAddBtn.setOnClickListener {
                if (description.isNotEmpty()) {
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setMessage(getString(R.string.apply_warning_message))
                    builder.setCancelable(false)
                    builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.setPositiveButton(getString(R.string.apply_text)) { dialog, which ->
//                        finalDescriptionText = if (descriptionAddBtn.isEnabled) {
//                            ""
//                        } else {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until descriptionTextViewList.size) {
                            val titleItem = descriptionTextViewList[i]
                            stringBuilder.append(titleItem.text.toString())
                            stringBuilder.append(" ")
                        }

                        finalDescriptionText = stringBuilder.toString().trim()
                        sharedViewModel.setDescription(finalDescriptionText)
                        //descriptionTextView.text.toString()
//                        }
                        dialog.dismiss()
                        descriptionAddBtn.text = getString(R.string.added_text)
                        descriptionAddBtn.isEnabled = false
                        doneBtn.visibility = View.VISIBLE
                    }

                    val alert1 = builder.create()
                    alert1.show()
                }
            }
        }

        override fun onClick(v: View?) {
            val view = v!!
            when (view.id) {
                else -> {
//                if (view.tag == "title") {
                    val position = view.id
                    val textView = view as MaterialTextView
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.primary_positive_color
                        )
                    )
                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                    val balloon = Balloon.Builder(requireActivity())
                        .setLayout(R.layout.ballon_layout_design)
                        .setArrowSize(10)
                        .setArrowOrientation(ArrowOrientation.TOP)
                        .setArrowPosition(0.5f)
                        .setWidthRatio(0.55f)
                        .setCornerRadius(4f)
                        .setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.light_gray
                            )
                        )
                        .setBalloonAnimation(BalloonAnimation.ELASTIC)
                        .setLifecycleOwner(this)
                        .build()
                    val editTextBox = balloon.getContentView()
                        .findViewById<TextInputEditText>(R.id.balloon_edit_text)
                    editTextBox.setText(textView.text.toString().trim())
                    val closeBtn = balloon.getContentView()
                        .findViewById<AppCompatButton>(R.id.balloon_close_btn)
                    val applyBtn = balloon.getContentView()
                        .findViewById<AppCompatButton>(R.id.balloon_apply_btn)
                    balloon.showAlignTop(textView)
                    editTextBox.requestFocus()
                    Constants.openKeyboar(requireActivity())
                    closeBtn.setOnClickListener {
                        Constants.hideKeyboar(requireActivity())
                        balloon.dismiss()
                        view.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.white
                            )
                        )
                        view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    }
                    applyBtn.setOnClickListener {
                        Constants.hideKeyboar(requireActivity())
                        balloon.dismiss()
                        //val tempText = textView.replace(mWord,editTextBox.text.toString().trim())
                        textView.text = editTextBox.text.toString().trim()
                        view.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.white
                            )
                        )
                        view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))

                    }
//                }
                }
            }
        }

    }


    open class FullDescriptionDialogFragment(private val description: String) : DialogFragment() {

        private lateinit var fullDescriptionView: MaterialTextView
        private lateinit var dialogCloseBtn: AppCompatImageView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialogStyle
            )

        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v =
                inflater.inflate(R.layout.rain_forest_full_description_layout, container)

            initViews(v)

            return v
        }

        private fun initViews(view: View) {
            fullDescriptionView = view.findViewById(R.id.full_description_text_view)
            dialogCloseBtn = view.findViewById(R.id.dialog_close_btn)


            fullDescriptionView.setText(description)
            dialogCloseBtn.setOnClickListener {
                dismiss()
            }
        }


    }

}