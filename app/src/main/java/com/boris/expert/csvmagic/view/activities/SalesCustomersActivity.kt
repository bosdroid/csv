package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.VolleySingleton
import com.boris.expert.csvmagic.viewmodel.CodeDetailViewModel
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SalesCustomersActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var appSettings: AppSettings
    private lateinit var viewModel:SalesCustomersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_customers)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(SalesCustomersViewModel()).createFor()
        )[SalesCustomersViewModel::class.java]
        appSettings = AppSettings(context)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.insales_customers)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun inSalesLogin(){

        startLoading(context)
        viewModel.callSalesAccount(context,"myshop-bsq158","asatarpk@gmail.com","Sattar_786")
        viewModel.getSalesAccountResponse().observe(this, Observer { response->
            dismiss()
            if (response != null){
                Log.d("TEST199",response.toString())
            }
        })
//        AndroidNetworking.get("https://${URLEncoder.encode("asatarpk@gmail.com", "UTF-8")}:Sattar_786@myshop-bsq158.myinsales.ru/admin/account.json")
//                .setPriority(Priority.LOW)
//                .build().getAsJSONObject(object : JSONObjectRequestListener{
//                    override fun onResponse(response: JSONObject?) {
//                        dismiss()
//                        Log.d("TEST1999",response.toString())
//                    }
//
//                    override fun onError(anError: ANError?) {
//                        dismiss()
//                        Log.d("TEST1999",anError!!.localizedMessage!!)
//                    }
//
//                })

//        val url = "https://asatarpk@gmail.com:Sattar_786@myshop-bsq158.myinsales.ru/admin/account.json"
//        startLoading(context)
//        val stringRequest = object : StringRequest(
//            Method.GET, url,
//            Response.Listener {
//                dismiss()
//                val response = JSONObject(it)
//                Log.d("TEST199SALES", response.toString())
//            }, Response.ErrorListener {
//                dismiss()
//                //Log.d("TEST199", it.localizedMessage!!)
//            }){
//            override fun getHeaders(): MutableMap<String, String> {
//                val headers = HashMap<String, String>()
////                headers.put(
////                    "Authorization",
////                    "Basic ZGlnaXRhbC1nb29kczpjNTFjOTA3MDdhMTNjZTNmZmYyMTNhZmJiNWNkMTI3MA=="
////                )
//                headers.put(
//                    "Content-Type",
//                    "application/json; charset=utf-8"
//                )
//                return headers
//            }
//        }
//
//        stringRequest.retryPolicy = object : RetryPolicy {
//            override fun getCurrentTimeout(): Int {
//                return 50000
//            }
//
//            override fun getCurrentRetryCount(): Int {
//                return 50000
//            }
//
//            @Throws(VolleyError::class)
//            override fun retry(error: VolleyError) {
//            }
//        }
//
//        VolleySingleton(context).addToRequestQueue(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        inSalesLogin()
    }

    fun md5(s: String): String {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest
                .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}