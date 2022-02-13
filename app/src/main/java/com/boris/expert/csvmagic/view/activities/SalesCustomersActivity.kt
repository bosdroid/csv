package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.regex.Pattern

class SalesCustomersActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var appSettings: AppSettings
    private lateinit var viewModel: SalesCustomersViewModel
    private lateinit var insalesLoginWrapperLayout: CardView
    private lateinit var insalesDataWrapperLayout:LinearLayout
    private lateinit var insalesShopNameBox: TextInputEditText
    private lateinit var insalesEmailBox: TextInputEditText
    private lateinit var insalesPasswordBox: TextInputEditText
    private lateinit var insalesLoginBtn: MaterialButton


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

        insalesLoginWrapperLayout = findViewById(R.id.insales_login_wrapper_layout)
        insalesDataWrapperLayout = findViewById(R.id.insales_data_wrapper_layout)
        insalesShopNameBox = findViewById(R.id.insales_login_shop_name_box)
        insalesEmailBox = findViewById(R.id.insales_login_email_box)
        insalesPasswordBox = findViewById(R.id.insales_login_password_box)
        insalesLoginBtn = findViewById(R.id.insales_login_btn)
        insalesLoginBtn.setOnClickListener(this)
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

    private fun inSalesLogin(shopName: String, email: String, password: String) {

        startLoading(context)
        viewModel.callSalesAccount(context, shopName, email, password)
        viewModel.getSalesAccountResponse().observe(this, Observer { response ->
            dismiss()
            if (response != null) {
                if (response.get("status").asString == "200"){
                    appSettings.putString("INSALES_STATUS","logged")
                    appSettings.putString("INSALES_SHOP_NAME",shopName)
                    appSettings.putString("INSALES_EMAIL",email)
                    appSettings.putString("INSALES_PASSWORD",password)

                    insalesLoginWrapperLayout.visibility = View.GONE
                    insalesDataWrapperLayout.visibility = View.VISIBLE
                    showAlert(context,response.toString())
                }
                else{
                    showAlert(context,response.get("message").asString)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        val insalesStatus = appSettings.getString("INSALES_STATUS")

        if (insalesStatus!!.isNotEmpty() && insalesStatus == "logged"){
            insalesLoginWrapperLayout.visibility = View.GONE
            insalesDataWrapperLayout.visibility = View.VISIBLE
        }
        else{
            insalesDataWrapperLayout.visibility = View.GONE
            insalesLoginWrapperLayout.visibility = View.VISIBLE
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.insales_login_btn -> {
                if (validation()) {
                    val shopName = insalesShopNameBox.text.toString().trim()
                    val email = insalesEmailBox.text.toString().trim()
                    val password = insalesPasswordBox.text.toString().trim()
                    inSalesLogin(shopName, email, password)
                }
            }
            else -> {

            }
        }
    }

    private fun validation(): Boolean {
        if (insalesShopNameBox.text.toString().trim().isEmpty()){
            showAlert(context,getString(R.string.empty_text_error))
            return false
        }else if (insalesEmailBox.text.toString().trim().isEmpty()){
            showAlert(context,getString(R.string.empty_text_error))
            return false
        }
        else if (!Pattern.compile(Constants.emailPattern).matcher(insalesEmailBox.text.toString().trim())
                .matches()){
            showAlert(context,getString(R.string.email_valid_error))
            return false
        }
        else if (insalesPasswordBox.text.toString().trim().isEmpty()){
            showAlert(context,getString(R.string.empty_text_error))
            return false
        }
        return true
    }
}