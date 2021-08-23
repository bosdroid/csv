package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants


class CreditActivity : BaseActivity(), View.OnClickListener, BillingProcessor.IBillingHandler {

    private var minimumProductDetail: TransactionDetails? = null
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar

    //    private var billingClient: BillingClient? = null
    private lateinit var minimumPackageBtn: AppCompatButton
    private lateinit var regularPackageBtn: AppCompatButton
    private lateinit var premiumPackageBtn: AppCompatButton
    private lateinit var appSettings: AppSettings
    private lateinit var bp: BillingProcessor
    private var minimumProductId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
//        billingClient = BillingClient.newBuilder(this)
//            .enablePendingPurchases().setListener(this).build()
        bp = BillingProcessor.newBillingProcessor(this, Constants.licenseKey, this)
        bp.initialize()
        toolbar = findViewById(R.id.toolbar)
        minimumPackageBtn = findViewById(R.id.minimum_package_btn)
        minimumPackageBtn.setOnClickListener(this)
        regularPackageBtn = findViewById(R.id.regular_package_btn)
        regularPackageBtn.setOnClickListener(this)
        premiumPackageBtn = findViewById(R.id.premium_package_btn)
        premiumPackageBtn.setOnClickListener(this)
        minimumProductId = getString(R.string.minimum_product_id)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

//    override fun onPurchasesUpdated(
//        billingResult: BillingResult,
//        purchases: MutableList<Purchase>?
//    ) {
////if item newly purchased
//
//        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
//            handlePurchases(purchases)
//        }
//        //if item already purchased then check and reflect changes
//        else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//            billingClient!!.queryPurchasesAsync(INAPP) { billingResults, list ->
//                handlePurchases(list)
//            }
////            val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(INAPP)
////            val alreadyPurchases: List<Purchase>? = queryAlreadyPurchasesResult.purchasesList
////            if (alreadyPurchases != null) {
////                handlePurchases(alreadyPurchases)
////            }
//        }
//        //if purchase cancelled
//        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show()
//        }
//        // Handle any other error msgs
//        else {
//            Toast.makeText(
//                applicationContext,
//                "Error " + billingResult.debugMessage,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.minimum_package_btn -> {

                if (bp.isOneTimePurchaseSupported) {
                    bp.purchase(this, minimumProductId)
                } else {
                    Toast.makeText(context, "Billing Purchased not Supported!", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            R.id.regular_package_btn -> {

            }
            R.id.premium_package_btn -> {

            }
            else -> {

            }
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Log.d("TEST199", "onProductPurchased: ")
    }

    override fun onPurchaseHistoryRestored() {
        Log.d("TEST199", "onPurchaseHistoryRestored: ")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.d("TEST199", "onBillingError: ")
    }

    override fun onBillingInitialized() {
        Log.d("TEST199", "onBillingInitialized: ")
        minimumProductDetail = bp.getPurchaseTransactionDetails(minimumProductId)

        if (bp.isPurchased(minimumProductId)){
            Toast.makeText(context, "Product already purchased!", Toast.LENGTH_SHORT)
                .show()
        }
        else{
            Toast.makeText(context, "You can purchase a Product!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        bp.release()
        super.onDestroy()
    }

//    private fun purchase() {
//        if (billingClient!!.isReady) {
//            initiatePurchase()
//        }
//        //else reconnect service
//        else {
//            billingClient =
//                BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
//            billingClient!!.startConnection(object : BillingClientStateListener {
//                override fun onBillingSetupFinished(billingResult: BillingResult) {
//                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                        initiatePurchase()
//                    } else {
//                        Toast.makeText(
//                            applicationContext,
//                            "Error " + billingResult.debugMessage,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onBillingServiceDisconnected() {}
//            })
//        }
//    }
//
//    private fun initiatePurchase() {
//        val skuList: MutableList<String> = ArrayList()
//        skuList.add("credit")
//        val params = SkuDetailsParams.newBuilder()
//        params.setSkusList(skuList).setType(INAPP)
//
//        billingClient!!.querySkuDetailsAsync(params.build())
//        { billingResult, skuDetailsList ->
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                if (skuDetailsList != null && skuDetailsList.size > 0) {
//                    val flowParams = BillingFlowParams.newBuilder()
//                        .setSkuDetails(skuDetailsList[0])
//                        .build()
//                    billingClient!!.launchBillingFlow(this, flowParams)
//                } else {
//                    //try to add item/product id "purchase" inside managed product in google play console
//
//                    Toast.makeText(
//                        applicationContext,
//                        "Purchase Item not Found",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            } else {
//                Toast.makeText(
//                    applicationContext,
//                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//    }
//
//    private fun handlePurchases(purchases: List<Purchase>) {
//        for (purchase in purchases) {
//            //if item is purchased
//
//            if (purchase.skus.equals("credit") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
//                    // Invalid purchase
//                    // show error to user
//
//                    Toast.makeText(
//                        applicationContext,
//                        "Error : Invalid Purchase",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return
//                }
//                // else purchase is valid
//                //if item is purchased and not acknowledged
//
//
//                if (!purchase.isAcknowledged) {
//                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
//                        .setPurchaseToken(purchase.purchaseToken)
//                        .build()
//                    billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase)
//                }
//                //else item is purchased and also acknowledged
//                else {
//                    // Grant entitlement to the user on item purchase
//                    // restart activity
//
//                    if (!purchaseValueFromPref) {
//                        savePurchaseValueToPref(true)
//                        Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
//                        recreate()
//                    }
//                }
//            }
//            //if purchase is pending
//            else if (purchase.skus.equals("credit") && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
//                Toast.makeText(
//                    applicationContext,
//                    "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT
//                ).show()
//            }
//            //if purchase is refunded or unknown
//            else if (purchase.skus.equals("credit") && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
//                savePurchaseValueToPref(false)
////                purchaseStatus!!.text = "Purchase Status : Not Purchased"
////                purchaseButton!!.visibility = View.VISIBLE
//                Toast.makeText(applicationContext, "Purchase Status Unknown", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//    var ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
//        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//            //if purchase is acknowledged
//            // Grant entitlement to the user. and restart activity
//
//            savePurchaseValueToPref(true)
//            Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
//            recreate()
//        }
//    }
//
//    private val purchaseValueFromPref: Boolean
//        get() = appSettings.getBoolean("PURCHASE_KEY")
//
//    private fun savePurchaseValueToPref(value: Boolean) {
//        appSettings.putBoolean("PURCHASE_KEY", value)
//    }
//
//    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
//        return try {
//            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
//
//            val base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlbIjQZds4JytxrzDIzVQ9EY0FzpTsuLPX7OO/c6SF9kS6TN4REhsgcaXO6BbyBKoVUL5SznysLATryvjpliLtI///8I9ohz1A5AaxAoqzXZgpj0ECHuHk68R+nGs1dzBS9/pjNjh1Gj3rMf5eSNjBTIGqjPPZjtgMW7c+sr/BfHe+L1Mci3Ep0pv17roZPwczsHzPaK8yP308fd5n6KU3VJDmrj4xwcyqdPVQvcbC4bM7/JK523xNNsEtoF10grxj1Izeo6AYplSV5KjvrN/ByqTqGLP4x4KyfDoE0BA/6hyoARTPKoM9clDN1EhwUb/yItH6tAlOO2AcAp7GVWCHQIDAQAB"
//            Security.verifyPurchase(base64Key, signedData, signature)
//        } catch (e: IOException) {
//            false
//        }
//    }

}