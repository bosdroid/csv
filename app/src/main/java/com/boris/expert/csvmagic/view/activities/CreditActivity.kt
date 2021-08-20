package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.boris.expert.csvmagic.R

class CreditActivity : BaseActivity(), PurchasesUpdatedListener, View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var billingClient: BillingClient? = null
    private lateinit var minimumPackageBtn: AppCompatButton
    private lateinit var regularPackageBtn: AppCompatButton
    private lateinit var premiumPackageBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()
        toolbar = findViewById(R.id.toolbar)
        minimumPackageBtn = findViewById(R.id.minimum_package_btn)
        minimumPackageBtn.setOnClickListener(this)
        regularPackageBtn = findViewById(R.id.regular_package_btn)
        regularPackageBtn.setOnClickListener(this)
        premiumPackageBtn = findViewById(R.id.premium_package_btn)
        premiumPackageBtn.setOnClickListener(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.credits)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
//if item newly purchased

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
//            handlePurchases(purchases)
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(INAPP)
            val alreadyPurchases: List<Purchase>? = queryAlreadyPurchasesResult.purchasesList
            if (alreadyPurchases != null) {
//                handlePurchases(alreadyPurchases)
            }
        }
        //if purchase cancelled
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show()
        }
        // Handle any other error msgs
        else {
            Toast.makeText(
                applicationContext,
                "Error " + billingResult.debugMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.minimum_package_btn -> {

            }
            R.id.regular_package_btn -> {

            }
            R.id.premium_package_btn -> {

            }
            else -> {

            }
        }
    }

    private fun purchase() {
        if (billingClient!!.isReady) {
            initiatePurchase()
        }
        //else reconnect service
        else {
            billingClient =
                BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Error " + billingResult.debugMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    private fun initiatePurchase() {


    }


}