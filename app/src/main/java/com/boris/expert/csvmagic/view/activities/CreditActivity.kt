package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.FeaturesAdapter
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.model.PurchaseDetail
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.Security
import com.boris.expert.csvmagic.viewmodel.CreditActivityViewModel
import com.boris.expert.csvmagic.viewmodel.PurchaseFeatureActivityViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException


class CreditActivity : BaseActivity(), View.OnClickListener, PurchasesUpdatedListener,
    FeaturesAdapter.OnItemClickListener {

    private var purchaseDetail: PurchaseDetail? = null
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: CreditActivityViewModel

    private var billingClient: BillingClient? = null
    private lateinit var minimumPackageBtn: AppCompatButton
    private lateinit var regularPackageBtn: AppCompatButton
    private lateinit var premiumPackageBtn: AppCompatButton
    private lateinit var appSettings: AppSettings
    private var minimumProductId = ""
    private var featureList = mutableListOf<Feature>()
    private lateinit var featuresRecyclerView: RecyclerView
    private lateinit var totalCreditsView: MaterialTextView
    private lateinit var adapter: FeaturesAdapter
    private var creditsValue: Int = 0
    private var userCurrentCreditsValue: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: DatabaseReference
    private var userId: String = ""
    private var creditsUpdateList: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)

        initViews()
        setUpToolbar()
        getUserCredits()
    }

    private fun initViews() {
        context = this
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        appSettings = AppSettings(context)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(CreditActivityViewModel()).createFor()
        )[CreditActivityViewModel::class.java]
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()

        toolbar = findViewById(R.id.toolbar)
        featuresRecyclerView = findViewById(R.id.features_recyclerview)
        minimumPackageBtn = findViewById(R.id.minimum_package_btn)
        minimumPackageBtn.setOnClickListener(this)
        regularPackageBtn = findViewById(R.id.regular_package_btn)
        regularPackageBtn.setOnClickListener(this)
        premiumPackageBtn = findViewById(R.id.premium_package_btn)
        premiumPackageBtn.setOnClickListener(this)
        minimumProductId = getString(R.string.minimum_product_id)
        totalCreditsView = findViewById(R.id.total_credits_view)
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
            val purchase: Purchase = purchases[0]
            if (auth.currentUser != null) {
                val userId = auth.currentUser!!.uid
                purchaseDetail =
                    PurchaseDetail(
                        userId,
                        purchase.orderId,
                        purchase.packageName,
                        "single_credit",
                        purchase.purchaseTime,
                        purchase.purchaseToken
                    )
                verifyPurchase()
            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
            //handlePurchases(purchases)
        }
        //if item already purchased then check and reflect changes
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
        //if purchase cancelled
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show()
        }
        // Handle any other error msgs
        else {
//            Toast.makeText(
//                applicationContext,
//                "Error " + billingResult.debugMessage,
//                Toast.LENGTH_SHORT
//            ).show()
        }
    }

    private fun verifyPurchase() {
        startLoading(context)
        viewModel.callPurchase(
            context,
            purchaseDetail!!.packageName,
            "single_credit",
            purchaseDetail!!.purchaseToken
        )
        viewModel.getPurchaseResponse().observe(this, { response ->
            dismiss()
            if (response != null) {
                when (response.get("validPurchase").asString) {
                    "true" -> {
                        firebaseDatabase.child(Constants.firebasePurchaseHistory).push()
                            .setValue(purchaseDetail)
                        val hashMap = HashMap<String, String>()
                        creditsValue += userCurrentCreditsValue
                        hashMap["credits"] = creditsValue.toString()
                        firebaseDatabase.child(Constants.firebaseUserCredits)
                            .child(userId)
                            .setValue(hashMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    getString(R.string.user_credits_update_success_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                getUserCredits()
                                Log.d("TEST199", "$userCurrentCreditsValue")
                            }
                            .addOnFailureListener {

                            }
                    }
                    "false" -> {
                        showRetryDialogBox()
                    }
                    "error" -> {
                        showRetryDialogBox()
                    }
                }
            } else {
                showRetryDialogBox()
            }
        })

    }


    private fun showRetryDialogBox() {
        MaterialAlertDialogBuilder(context).setCancelable(false)
            .setMessage(getString(R.string.purchase_retry_error_message))
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.retry_text)) { dialog, which ->
                dialog.dismiss()
                verifyPurchase()
            }
            .create().show()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.minimum_package_btn -> {
                if (auth.currentUser != null) {
                    creditsValue = 1
                    userId = auth.currentUser!!.uid
                    purchase()

                } else {
                    showAlert(context, getString(R.string.user_session_expired))
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


    override fun onDestroy() {
        super.onDestroy()
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
        val skuList: MutableList<String> = ArrayList()
        skuList.add("single_credit")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(INAPP)

        billingClient!!.querySkuDetailsAsync(params.build())
        { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    billingClient!!.launchBillingFlow(this, flowParams)
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console

                    Toast.makeText(
                        applicationContext,
                        "Purchase Item not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased

            if (purchase.skus.equals("single_credit") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    // Invalid purchase
                    // show error to user

                    Toast.makeText(
                        applicationContext,
                        "Error : Invalid Purchase",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                // else purchase is valid
                //if item is purchased and not acknowledged


                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase)
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity

//                    if (!purchaseValueFromPref) {
//                        savePurchaseValueToPref(true)
//                        Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
//                        recreate()
//                    }
                }
            }
            //if purchase is pending
            else if (purchase.skus.equals("single_credit") && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Toast.makeText(
                    applicationContext,
                    "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT
                ).show()
            }
            //if purchase is refunded or unknown
            else if (purchase.skus.equals("single_credit") && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
//                savePurchaseValueToPref(false)
//                purchaseStatus!!.text = "Purchase Status : Not Purchased"
//                purchaseButton!!.visibility = View.VISIBLE
                Toast.makeText(applicationContext, "Purchase Status Unknown", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    var ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            //if purchase is acknowledged
            // Grant entitlement to the user. and restart activity
//            savePurchaseValueToPref(true)
            Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
//            recreate()
        }
    }

    private val purchaseValueFromPref: Boolean
        get() = appSettings.getBoolean("PURCHASE_KEY")

    private fun savePurchaseValueToPref(value: Boolean) {
        appSettings.putBoolean("PURCHASE_KEY", value)
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.

            Security.verifyPurchase(Constants.licenseKey, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    override fun onResume() {
        super.onResume()
//        getFeaturesList()

    }


    private fun getUserCredits() {
        if (auth.currentUser != null) {
            startLoading(context)
            userId = auth.currentUser!!.uid

            firebaseDatabase.child(Constants.firebaseUserCredits)
                .child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dismiss()
                        if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                            val previousCredits =
                                snapshot.child("credits").getValue(String::class.java)
                            userCurrentCreditsValue = if (previousCredits!!.isNotEmpty()) {
                                previousCredits.toInt()
                            } else {
                                0
                            }
                        }
                        totalCreditsView.text = "$userCurrentCreditsValue"
                        Log.d("TEST199", "$userCurrentCreditsValue")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        dismiss()
                    }

                })
            firebaseDatabase.child(Constants.firebaseUserCredits)
                .child(userId).addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        userCurrentCreditsValue =
                            if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                val previousCredits =
                                    snapshot.child("credits").getValue(String::class.java)
                                previousCredits!!.toInt()
                            } else {
                                0
                            }
                        Log.d("TEST199", "$userCurrentCreditsValue")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }
    }


//    private fun getFeaturesList() {
//        featuresRecyclerView.layoutManager = LinearLayoutManager(context)
//        featuresRecyclerView.hasFixedSize()
//        adapter = FeaturesAdapter(context, featureList as ArrayList<Feature>)
//        featuresRecyclerView.adapter = adapter
//        adapter.setOnItemClickListener(this)
//
//        startLoading(context)
//        viewModel.callFeaturesList(context)
//        viewModel.getFeaturesList().observe(this, { list ->
//            if (list != null) {
//                dismiss()
//                if (list.isNotEmpty()) {
//                    featureList.clear()
//                }
//                featureList.addAll(list)
//                adapter.notifyItemRangeChanged(0, featureList.size)
//            } else {
//                dismiss()
//            }
//        })
//    }

    override fun onItemPurchaseBtnClick(position: Int) {
        val feature = featureList[position]

    }

}