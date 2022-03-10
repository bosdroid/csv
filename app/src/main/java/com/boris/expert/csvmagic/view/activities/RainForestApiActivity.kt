package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.RainForestApiAdapter
import com.boris.expert.csvmagic.interfaces.TranslationCallback
import com.boris.expert.csvmagic.model.RainForestApiObject
import com.boris.expert.csvmagic.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
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
    private var pDetailsPrice = 0F
    private var pListPrice = 0F
    private var characters = 0
    private var translatorPrice = 0F
    private var unitCharacterPrice = 0F
    private var userCurrentCredits = ""
    private var howMuchChargeCredits = 0F
    private var titleTextViewList = mutableListOf<TextView>()
    private var descriptionTextViewList = mutableListOf<TextView>()


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
                val totalCreditPrice = unitCharacterPrice * query.length
                val total = totalCreditPrice + pListPrice
                howMuchChargeCredits = total
//                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= total) {
                if (query.isNotEmpty()) {
                    Constants.hideKeyboar(context)

                    GcpTranslator.translateFromRusToEng(
                            context,
                            query,
                            object : TranslationCallback {
                                override fun onTextTranslation(translatedText: String) {
                                    if (translatedText.isNotEmpty()) {
                                        //showAlert(context,translatedText)
                                        getProducts(translatedText)
                                    } else {
                                        showAlert(
                                                context,
                                                "Something wrong with translator, please try later!"
                                        )
                                    }
                                }

                            })
                } else {
                    showAlert(context, getString(R.string.empty_text_error))
                }
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
                    val position = view.id
                    val textView = view as MaterialTextView
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_positive_color))
                    view.setTextColor(ContextCompat.getColor(context, R.color.white))
                    val balloon = Balloon.Builder(context)
                        .setLayout(R.layout.ballon_layout_design)
                        .setArrowSize(10)
                        .setArrowOrientation(ArrowOrientation.TOP)
                        .setArrowPosition(0.5f)
                        .setWidthRatio(0.55f)
                        .setCornerRadius(4f)
                        .setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
                        .setBalloonAnimation(BalloonAnimation.ELASTIC)
                        .setLifecycleOwner(this@RainForestApiActivity)
                        .build()
                    val editTextBox = balloon.getContentView().findViewById<TextInputEditText>(R.id.balloon_edit_text)
                    editTextBox.setText(textView.text.toString().trim())
                    val closeBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_close_btn)
                    val applyBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_apply_btn)
                    balloon.showAlignTop(textView,)
                    editTextBox.requestFocus()
                    Constants.openKeyboar(context)
                    closeBtn.setOnClickListener {
                        Constants.hideKeyboar(context)
                        balloon.dismiss()
                        view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                        view.setTextColor(ContextCompat.getColor(context, R.color.black))
                    }
                    applyBtn.setOnClickListener {
                        Constants.hideKeyboar(context)
                        balloon.dismiss()
                        //val tempText = textView.replace(mWord,editTextBox.text.toString().trim())
                        textView.text = editTextBox.text.toString().trim()
                        view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                        view.setTextColor(ContextCompat.getColor(context, R.color.black))

                    }
//                }
            }
        }
    }

    private fun getProducts(query: String) {
        startLoading(context)
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

                            CoroutineScope(Dispatchers.IO).launch {

                                for (i in 0 until rainForestList.size) {
                                    val text = rainForestList[i].title
                                    GcpTranslator.translateFromEngToRus(
                                            context,
                                            text,
                                            object : TranslationCallback {
                                                override fun onTextTranslation(translatedText: String) {
                                                    rainForestList[i].title = translatedText
                                                }

                                            })
                                }
                                CoroutineScope(Dispatchers.Main).launch {
                                    dismiss()
                                    if (rainForestList.size > 0) {
                                        adapter.notifyItemRangeChanged(0, rainForestList.size)
                                    }
                                }
                                chargeCreditsPrice()
                            }
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
                    val response = JSONObject(it)
                    if (response.has("product")) {
                        val productResults = response.getJSONObject("product")
                        val description = if (productResults.isNull("description")) {
                            ""
                        } else {
                            productResults.getString("description")
                        }
                        val title = productResults.getString("title")

                        val layoutBuilder = MaterialAlertDialogBuilder(context)
                        val dialogLayout = LayoutInflater.from(context)
                                .inflate(R.layout.rain_forest_title_description_layout, null)
                        val dialogCloseBtn =
                                dialogLayout.findViewById<AppCompatImageView>(R.id.dialog_close_btn)
                        val titleTextView =
                                dialogLayout.findViewById<MaterialTextView>(R.id.title_text_view)
                        val descriptionTextView =
                                dialogLayout.findViewById<MaterialTextView>(R.id.description_text_view)
                        val titleAddBtn =
                                dialogLayout.findViewById<MaterialTextView>(R.id.add_title_button)
                        val descriptionAddBtn =
                                dialogLayout.findViewById<MaterialTextView>(R.id.add_description_button)
                        val doneBtn = dialogLayout.findViewById<MaterialButton>(R.id.rfa_item_done_btn)
                        val dynamicTitleTextViewWrapper = dialogLayout.findViewById<FlowLayout>(R.id.dynamic_textview_wrapper)
                        val dynamicDescriptionTextViewWrapper = dialogLayout.findViewById<FlowLayout>(R.id.dynamic_description_textview_wrapper)

                        titleAddBtn.isEnabled = true
                        descriptionAddBtn.isEnabled = true

                        layoutBuilder.setView(dialogLayout)
                        layoutBuilder.setCancelable(false)
                        val alert = layoutBuilder.create()
                        alert.show()
                        dialogCloseBtn.setOnClickListener {
                            alert.dismiss()
                        }

                        doneBtn.setOnClickListener {
                            alert.dismiss()
                            val finalTitleText = if (titleAddBtn.isEnabled) {
                                ""
                            } else {
                                val stringBuilder = StringBuilder()
                                for (i in 0 until titleTextViewList.size){
                                    val titleItem = titleTextViewList[i]
                                    stringBuilder.append(titleItem.text.toString())
                                    stringBuilder.append(" ")
                                }

                                stringBuilder.toString().trim()
                            }
                            val finalDescriptionText = if (descriptionAddBtn.isEnabled) {
                                ""
                            } else {
                                val stringBuilder = StringBuilder()
                                for (i in 0 until descriptionTextViewList.size){
                                    val titleItem = descriptionTextViewList[i]
                                    stringBuilder.append(titleItem.text.toString())
                                    stringBuilder.append(" ")
                                }

                                stringBuilder.toString().trim()
                                //descriptionTextView.text.toString()
                            }
                            setResult(RESULT_OK, Intent().apply {
                                putExtra("TITLE", finalTitleText)
                                putExtra("DESCRIPTION", finalDescriptionText)
                            })
                            finish()
                        }
                        val totalCharacters = title.length + description.length
                        val totalCreditPrice = unitCharacterPrice * totalCharacters
                        howMuchChargeCredits = totalCreditPrice
                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                    userCurrentCredits = "0"
//                    if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= totalCreditPrice) {

                        GcpTranslator.translateFromEngToRus(
                                context,
                                title,
                                object : TranslationCallback {
                                    override fun onTextTranslation(translatedText: String) {
                                        if (translatedText.isNotEmpty()) {
                                            titleTextView.text = translatedText

                                            val textList = translatedText.split(" ")


                                            for (i in 0 until textList.size) {
                                                val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
                                                        FlowLayout.LayoutParams.WRAP_CONTENT)
                                                params.setMargins(5, 5, 5, 5)
                                                val textView = MaterialTextView(context)
                                                textView.layoutParams = params
                                                textView.text = textList[i]
                                                textView.tag = "title"
                                                textView.id = i
                                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                                                textView.setTextColor(ContextCompat.getColor(context,R.color.black))
                                                titleTextViewList.add(textView)
                                                textView.setOnClickListener(this@RainForestApiActivity)
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
                                    context,
                                    description,
                                    object : TranslationCallback {
                                        override fun onTextTranslation(translatedText: String) {
                                            if (translatedText.isNotEmpty()) {
                                                descriptionTextView.text = translatedText

                                                val textList = translatedText.split(" ")


                                                for (i in 0 until textList.size) {
                                                    val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
                                                            FlowLayout.LayoutParams.WRAP_CONTENT)
                                                    params.setMargins(5, 5, 5, 5)
                                                    val textView = MaterialTextView(context)
                                                    textView.layoutParams = params
                                                    textView.text = textList[i]
                                                    textView.tag = "title"
                                                    textView.id = i
                                                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                                                    textView.setTextColor(ContextCompat.getColor(context,R.color.black))
                                                    descriptionTextViewList.add(textView)
                                                    textView.setOnClickListener(this@RainForestApiActivity)
                                                    dynamicDescriptionTextViewWrapper.addView(textView)
                                                }

                                            } else {
                                                descriptionTextView.text = ""
                                            }
                                        }

                                    })
                        }
//                        initSelectebleWord(descriptionTextView.text.toString(), descriptionTextView)
//                        descriptionTextView.setOnTouchListener(LinkMovementMethodOverride())
                        chargeCreditsPrice()
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
                            val builder = MaterialAlertDialogBuilder(context)
                            builder.setMessage(getString(R.string.apply_warning_message))
                            builder.setCancelable(false)
                            builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                                dialog.dismiss()
                            }
                            builder.setPositiveButton(getString(R.string.apply_text)) { dialog, which ->
                                dialog.dismiss()
                                titleAddBtn.text = getString(R.string.added_text)
                                titleAddBtn.isEnabled = false
                            }

                            val alert1 = builder.create()
                            alert1.show()

                        }

                        descriptionAddBtn.setOnClickListener {
                            if (description.isNotEmpty()) {
                                val builder = MaterialAlertDialogBuilder(context)
                                builder.setMessage(getString(R.string.apply_warning_message))
                                builder.setCancelable(false)
                                builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                                    dialog.dismiss()
                                }
                                builder.setPositiveButton(getString(R.string.apply_text)) { dialog, which ->
                                    dialog.dismiss()
                                    descriptionAddBtn.text = getString(R.string.added_text)
                                    descriptionAddBtn.isEnabled = false
                                }

                                val alert1 = builder.create()
                                alert1.show()
                            }
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
        return object: ClickableSpan() {

            var mWord:String = ""
            var ds:TextPaint?=null
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
                 if (widget.isPressed){
                     ds!!.bgColor = ContextCompat.getColor(context, R.color.primary_positive_color)
                 }
                else
                 {
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
                val editTextBox = balloon.getContentView().findViewById<TextInputEditText>(R.id.balloon_edit_text)
                editTextBox.setText(mWord)
                val closeBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_close_btn)
                val applyBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_apply_btn)
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

}