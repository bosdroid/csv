package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
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
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.LanguageTranslator
import com.boris.expert.csvmagic.utils.VolleySingleton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.util.ArrayList

class RainForestApiActivity : BaseActivity(), RainForestApiAdapter.OnItemClickListener,
    View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var rainForestRecyclerView: RecyclerView
    private lateinit var adapter: RainForestApiAdapter
    private var rainForestList = mutableListOf<RainForestApiObject>()
    private lateinit var searchBox: TextInputEditText
    private lateinit var searchImageBtn: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rain_forest_api)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
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
//        setResult(RESULT_OK,Intent().apply {
//            putExtra("DESCRIPTION","TEST description")
//        })
//        finish()
        getProductDescription(item.asin)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rainforest_products_search_btn -> {
                val query = searchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    Constants.hideKeyboar(context)

                    LanguageTranslator.translateText(query,"ru",object :TranslationCallback{
                        override fun onTextTranslation(translatedText: String) {
                             if (translatedText.isNotEmpty()){
                                 //showAlert(context,translatedText)
                                 getProducts(translatedText)
                             }
                            else{
                               showAlert(context,"Something wrong with translator, please try later!")
                            }

                        }

                    })
                } else {
                    showAlert(context, getString(R.string.empty_text_error))
                }
            }
            else -> {

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
                dismiss()
                val response = JSONObject(it)
                if (response.has("search_results")) {
                    val searchResults = response.getJSONArray("search_results")
                    if (searchResults.length() > 0) {
                        rainForestList.clear()
                        for (i in 0 until searchResults.length()) {
                            val item = searchResults.getJSONObject(i)
                            rainForestList.add(
                                RainForestApiObject(
                                    item.getString("asin"),
                                    item.getString("image"),
                                    item.getString("title")
                                )
                            )
                        }
                        if (rainForestList.size > 0){
                            adapter.notifyItemRangeChanged(0,rainForestList.size)
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
                    val description = productResults.getString("description")
                    startLoading(context)
                    LanguageTranslator.translateText(description,"en",object :TranslationCallback{
                        override fun onTextTranslation(translatedText: String) {
                            dismiss()
                            if (translatedText.isNotEmpty()){
                                val builder = MaterialAlertDialogBuilder(context)
                                builder.setMessage(translatedText)
                                builder.setCancelable(false)
                                builder.setNegativeButton(getString(R.string.cancel_text)){dialog,which->
                                    dialog.dismiss()
                                }
                                builder.setPositiveButton(getString(R.string.apply_text)){dialog,which->
                                    dialog.dismiss()
                                    setResult(RESULT_OK,Intent().apply {
                                        putExtra("DESCRIPTION",translatedText)
                                    })
                                    finish()
                                }

                                val alert = builder.create()
                                alert.show()
                            }
                            else{
                                showAlert(context,"Something wrong with translator, please try later!")
                            }

                        }
                    })
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
}