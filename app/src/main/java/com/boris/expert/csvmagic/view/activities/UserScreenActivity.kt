package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.billingclient.api.*
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.model.PurchaseDetail
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.Security
import com.boris.expert.csvmagic.viewmodel.UserScreenActivityViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserScreenActivity : BaseActivity(), View.OnClickListener, PurchasesUpdatedListener {

    private var purchaseDetail: PurchaseDetail? = null
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var appSettings: AppSettings
    private lateinit var auth: FirebaseAuth
    var userCurrentCreditsValue: Int = 0
    private lateinit var usCurrentCreditView: MaterialTextView
    private lateinit var usStorageSpaceView: MaterialTextView
    private lateinit var usDurationView: MaterialTextView
    private lateinit var getCreditsBtn: MaterialButton
    private lateinit var increaseStorageBtn: MaterialButton
    private lateinit var extendUsageBtn:MaterialButton
    private var userCurrentCredits = ""

    //    private lateinit var usExpiredAtView:MaterialTextView
    private var billingClient: BillingClient? = null
    private var productId = ""
    private var creditsValue: Int = 0
    private var userId: String = ""
    private lateinit var viewModel: UserScreenActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_screen)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        appSettings = AppSettings(context)
        toolbar = findViewById(R.id.toolbar)
        usCurrentCreditView = findViewById(R.id.us_total_credits_view)
        usStorageSpaceView = findViewById(R.id.us_storage_space_view)
        usDurationView = findViewById(R.id.us_duration_view)
        getCreditsBtn = findViewById(R.id.get_credits_btn)
        getCreditsBtn.setOnClickListener(this)
        increaseStorageBtn = findViewById(R.id.increase_storage_btn)
        increaseStorageBtn.setOnClickListener(this)
        extendUsageBtn = findViewById(R.id.extend_usage_btn)
        extendUsageBtn.setOnClickListener(this)
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(UserScreenActivityViewModel()).createFor()
        )[UserScreenActivityViewModel::class.java]
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()
//        usExpiredAtView = findViewById(R.id.us_expired_at_view)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.user_screen)
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

    override fun onResume() {
        super.onResume()
        getUserCredit()
        getUserSubscriptionDetails()
    }

    private fun getUserCredit() {
        if (auth.currentUser != null) {

            val userId = auth.currentUser!!.uid
            Constants.firebaseUserId = userId
            firebaseDatabase.child(Constants.firebaseUserCredits)
                .child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                            val previousCredits =
                                snapshot.child("credits").getValue(String::class.java)
                            userCurrentCreditsValue = if (previousCredits!!.isNotEmpty()) {
                                previousCredits.toInt()
                            } else {
                                0
                            }
                        }
                        usCurrentCreditView.text = "$userCurrentCreditsValue"
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    private fun getUserSubscriptionDetails() {
        if (auth.currentUser != null) {

            val userId = auth.currentUser!!.uid

            startLoading(context)
            viewModel.callUserPackageDetail(context,userId)
            viewModel.getUserPackageResponse().observe(this, { response->
                dismiss()
                if (response != null){
                    if (response.has("package") && !response.isNull("package")) {

                        val packageDetail: JSONObject? = response.getJSONObject("package")
                        val startDate = packageDetail!!.getString("start_date")
                        val endDate = packageDetail.getString("end_date")
                        val expiredTimeMili = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(endDate)!!.time

                        val diff1 = System.currentTimeMillis() - SimpleDateFormat(
                            "dd-MM-yyyy HH:mm:ss",
                            Locale.ENGLISH
                        ).parse(startDate)!!.time

                        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()

                        val remainingDay = Constants.calculateDays(
                            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(startDate)!!.time,
                            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(endDate)!!.time
                        )

                        val availableSize = packageDetail.getString("size")
                        val tSize = packageDetail.getInt("total_size")
                        val availableDuration = packageDetail.getInt("duration")
                        val formatted = String.format("%.2f",availableSize.toDouble())
                        usStorageSpaceView.text = "$formatted MB of $tSize MB"
                        usDurationView.text = "$remainingDay days left / expires \non ${getDateFromTimeStamp(expiredTimeMili)}"
                    }

                }

            })


//            var total: Int = 0
//            var duration: Int = 0
//            var memory: Float = 0F
//            var expiredAt: Long = 0
//            firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
//                .child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        dismiss()
//                        if (snapshot.hasChildren() && snapshot.hasChild("duration")) {
//                            duration = snapshot.child("duration").getValue(Int::class.java)!!
//                            val createdAt = snapshot.child("createdAt").getValue(Long::class.java)!!
//                            val expiredAtx =
//                                snapshot.child("expiredAt").getValue(Long::class.java)!!
//                            usDurationView.text = "${
//                                calculateDays(
//                                    createdAt,
//                                    expiredAtx
//                                )
//                            } days left / expires \non ${getDateFromTimeStamp(expiredAtx)}"
//                        } else {
//                            usDurationView.text = "$duration Days"
//                        }
//
//                        if (snapshot.hasChildren() && snapshot.hasChild("memory")) {
//                            total = snapshot.child("total_memory").getValue(Int::class.java)!!
//                            memory =
//                                snapshot.child("memory").getValue(String::class.java)!!.toFloat()
//                            usStorageSpaceView.text =
//                                "${String.format("%.1f", memory)} MB of $total MB"
//                        } else {
//                            usStorageSpaceView.text = "$memory MB"
//                        }
//
//                        if (snapshot.hasChildren() && snapshot.hasChild("expiredAt")) {
//                            expiredAt = snapshot.child("expiredAt").getValue(Long::class.java)!!
////                            usExpiredAtView.text = getDateTimeFromTimeStamp(expiredAt)
//                        } else {
////                            usExpiredAtView.text = "N/A"
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        dismiss()
//                    }
//
//                })

        }
    }

    private fun calculateDays(createdAt: Long, expiredAtx: Long): Int {
        val diff: Long = expiredAtx - createdAt
        val totalDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        val diff1 = System.currentTimeMillis() - createdAt
        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()
        return totalDays - goneDays
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.get_credits_btn -> {
                getCredits()
            }
            R.id.increase_storage_btn->{
               increaseStorage()
            }
            R.id.extend_usage_btn->{
                extendUsage()
            }
            else -> {

            }
        }
    }

    private fun getCredits() {

        val choosePackageLayout =
            LayoutInflater.from(context).inflate(R.layout.get_credits_dialog_layout, null)

        val oneCreditBtn =
            choosePackageLayout.findViewById<AppCompatButton>(R.id.get_credits_minimum_package_btn)
        val sixCreditBtn =
            choosePackageLayout.findViewById<AppCompatButton>(R.id.get_credits_regular_package_btn)
        val tenCreditBtn =
            choosePackageLayout.findViewById<AppCompatButton>(R.id.get_credits_premium_package_btn)


        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(true)
        builder.setView(choosePackageLayout)
        val alert = builder.create()
        alert.show()

        oneCreditBtn.setOnClickListener {
            productId = "single_credit"
            if (auth.currentUser != null) {
                alert.dismiss()
                creditsValue = 1
                userId = auth.currentUser!!.uid
                purchase()

            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
        }

        sixCreditBtn.setOnClickListener {
            productId = "six_credits"
            if (auth.currentUser != null) {
                alert.dismiss()
                creditsValue = 6
                userId = auth.currentUser!!.uid
                purchase()

            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
        }

        tenCreditBtn.setOnClickListener {
            productId = "ten_credits"
            if (auth.currentUser != null) {
                alert.dismiss()
                creditsValue = 10
                userId = auth.currentUser!!.uid
                purchase()

            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
        }

    }

    private fun increaseStorage(){
        val increaseStorageLayout = LayoutInflater.from(context).inflate(R.layout.increase_storage_dialog_layout, null)
        val psPurchaseBtn = increaseStorageLayout.findViewById<AppCompatButton>(R.id.ps_purchase_package_btn)
        val pspPurchaseBtn = increaseStorageLayout.findViewById<AppCompatButton>(R.id.psp_purchase_package_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(true)
        builder.setView(increaseStorageLayout)
        val alert = builder.create()
        alert.show()
        psPurchaseBtn.setOnClickListener {
            val feature = Feature(0,"photo_storage","simple",1,30,250f,0,0)
            purchaseFeature(alert,feature)
//            MaterialAlertDialogBuilder(context)
//                .setMessage("Are you sure you want to purchase this feature?")
//                .setCancelable(false)
//                .setNegativeButton("No") { dialog, which ->
//                    dialog.dismiss()
//                }
//                .setPositiveButton("Yes") { dialog, which ->
//                    dialog.dismiss()
//                    alert.dismiss()
//                    purchaseFeature(feature)
//                }.create().show()
        }

        pspPurchaseBtn.setOnClickListener {
            val feature = Feature(0,"photo_storage_pro","pro",2,30,500f,0,0)
//            MaterialAlertDialogBuilder(context)
//                .setMessage("Are you sure you want to purchase this feature?")
//                .setCancelable(false)
//                .setNegativeButton("No") { dialog, which ->
//                    dialog.dismiss()
//                }
//                .setPositiveButton("Yes") { dialog, which ->
//                    dialog.dismiss()
//                    alert.dismiss()
//                    purchaseFeature(feature)
//                }.create().show()
            purchaseFeature(alert,feature)
        }
    }

    private fun purchaseFeature(alert:AlertDialog, feature: Feature){
        startLoading(context)
        viewModel.callUserPackageDetail(context, Constants.firebaseUserId)
        viewModel.getUserPackageResponse().observe(this, Observer { response ->
            dismiss()
            if (response != null) {

                MaterialAlertDialogBuilder(context)
                    .setMessage("Are you sure you want to purchase this feature?")
                    .setCancelable(false)
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { dialog, which ->
                        dialog.dismiss()
                        alert.dismiss()
                        if (response.has("package") && response.isNull("package")) {

                            if (feature.name.contains("time")) {
                                showAlert(
                                    context,
                                    "You can't purchase this feature because currently do not have any active subscription."
                                )
                            } else {
                                purchaseFeature(feature)
                            }
                        } else {
                            val packageDetail: JSONObject? = response.getJSONObject("package")
                            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                            if (userCurrentCredits.isNotEmpty()) {
                                if (userCurrentCredits.toInt() >= feature.credit_price) {
                                    upgradeSubscription(feature, packageDetail!!)
                                } else {
                                    showAlert(
                                        context,
                                        "You can't purchase this feature due to zero or less credits!"
                                    )
                                }


                            }
                        }

                    }.create().show()
            }
        })
    }

    private fun upgradeSubscription(feature: Feature, packageDetail: JSONObject) {
        val startDate = packageDetail.getString("start_date")
        val endDate = packageDetail.getString("end_date")
        val expiredTimeMili =
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(endDate)!!.time

        val diff1 = System.currentTimeMillis() - SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss",
            Locale.ENGLISH
        ).parse(startDate)!!.time

        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()

        val remainingDay = Constants.calculateDays(
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(startDate)!!.time,
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).parse(endDate)!!.time
        )

        val p_name = packageDetail.getString("package")
        val p_type = packageDetail.getString("package_type")
        Log.d("TEST1999", "$goneDays")

        val availableSize = packageDetail.getInt("size")
        val availableDuration = packageDetail.getInt("duration")

        val currentMiliSeconds = System.currentTimeMillis()
        if ((p_name == "storage" && p_type == "simple") && (feature.name.contains("storage") && feature.type == "simple") && expiredTimeMili >= currentMiliSeconds) {
            showAlert(context, "You already have this subscription and not expired yet!")
        } else if ((p_name == "storage" && p_type == "pro") && (feature.name.contains("storage") && feature.type == "pro") && expiredTimeMili >= currentMiliSeconds) {
            showAlert(context, "You already have this subscription and not expired yet!")
        } else if ((p_name == "storage" && p_type == "simple") && (feature.name.contains("storage") && feature.type == "pro"))
        {
            val simplePackageUnitPrice = 1.toDouble() / 30
            val totalDaysGonePrice = simplePackageUnitPrice * goneDays
            val roundUpValue = BigDecimal(totalDaysGonePrice).setScale(2, RoundingMode.HALF_EVEN)
            val priceCharge = feature.credit_price - roundUpValue.toFloat()

            val updatedTotalSize = feature.memory + availableSize

            startLoading(context)
            updateMemorySize(
                context,
                updatedTotalSize.toString(),
                Constants.firebaseUserId,
                1,
                object : APICallback {
                    override fun onSuccess(response: JSONObject) {
                        dismiss()
                        if (response.getInt("status") == 200) {

                            val hashMap = HashMap<String, String>()
                            val remaining = userCurrentCredits.toInt() - priceCharge
                            hashMap["credits"] = remaining.toString()
                            firebaseDatabase.child(Constants.firebaseUserCredits)
                                .child(Constants.firebaseUserId)
                                .setValue(hashMap)
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener {

                                }
                            getUserSubscriptionDetails()
                            showAlert(context, "Congratulation on upgrading the subscription!")
                        } else {
                            val message = response.getString("message")
                            showAlert(context, message)
                        }
                    }

                    override fun onError(error: VolleyError) {
                        dismiss()
                    }

                })
        } else if (feature.name.contains("time") && feature.type == "simple"){
            purchaseFeature(feature)
        }else if (feature.name.contains("time") && feature.type == "pro"){
            purchaseFeature(feature)
        }

    }

    private fun extendUsage(){
        val extendUsageLayout = LayoutInflater.from(context).inflate(R.layout.extend_usage_dialog_layout, null)
        val utPurchaseBtn = extendUsageLayout.findViewById<AppCompatButton>(R.id.ut_purchase_package_btn)
        val utpPurchaseBtn = extendUsageLayout.findViewById<AppCompatButton>(R.id.utp_purchase_package_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(true)
        builder.setView(extendUsageLayout)
        val alert = builder.create()
        alert.show()
        utPurchaseBtn.setOnClickListener {
            val feature = Feature(0,"usage_time","simple",1,30,0f,0,0)
//            MaterialAlertDialogBuilder(context)
//                .setMessage("Are you sure you want to purchase this feature?")
//                .setCancelable(false)
//                .setNegativeButton("No") { dialog, which ->
//                    dialog.dismiss()
//                }
//                .setPositiveButton("Yes") { dialog, which ->
//                    dialog.dismiss()
//                    alert.dismiss()
//                    purchaseFeature(feature)
//                }.create().show()
            purchaseFeature(alert,feature)
        }

        utpPurchaseBtn.setOnClickListener {
            val feature = Feature(0,"usage_time_pro","pro",2,60,0f,0,0)
//            MaterialAlertDialogBuilder(context)
//                .setMessage("Are you sure you want to purchase this feature?")
//                .setCancelable(false)
//                .setNegativeButton("No") { dialog, which ->
//                    dialog.dismiss()
//                }
//                .setPositiveButton("Yes") { dialog, which ->
//                    dialog.dismiss()
//                    alert.dismiss()
//                    purchaseFeature(feature)
//                }.create().show()
            purchaseFeature(alert,feature)
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
                        productId,
                        purchase.purchaseTime,
                        purchase.purchaseToken
                    )
                CoroutineScope(Dispatchers.IO).launch {
                    val token = purchase.purchaseToken
                    val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(token).build()
                    billingClient!!.consumeAsync(consumeParams, consumeListener)
                }

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

    var consumeListener =
        ConsumeResponseListener { billingResult, purchaseToken ->
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    getString(R.string.consumer_product_success_text),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    private fun verifyPurchase() {
        startLoading(context)
        viewModel.callPurchase(
            context,
            context.packageName,
            productId,
            purchaseDetail!!.purchaseToken
        )
        viewModel.getPurchaseResponse().observe(this, { response ->
            dismiss()
            if (response != null) {
                when (response.get("purchaseState").asInt) {
                    0 -> {
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
                                getUserCredit()
                                Log.d("TEST199", "$userCurrentCreditsValue")
                            }
                            .addOnFailureListener {

                            }
                    }
                    else -> {
                        showRetryDialogBox(response.get("error").asString)
                    }
                }
            } else {
                showRetryDialogBox(getString(R.string.something_wrong_error))
            }
        })

    }


    private fun showRetryDialogBox(message: String) {
        MaterialAlertDialogBuilder(context).setCancelable(false)
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.retry_text)) { dialog, which ->
                dialog.dismiss()
                verifyPurchase()
            }
            .create().show()
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
        skuList.add("six_credits")
        skuList.add("ten_credits")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

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

    var ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            //if purchase is acknowledged
            // Grant entitlement to the user. and restart activity
//            savePurchaseValueToPref(true)
//            Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
//            recreate()
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.

            Security.verifyPurchase(Constants.licenseKey, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    private var listener:ValueEventListener?=null
    private fun purchaseFeature(feature: Feature) {
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
        startLoading(context)
        if (userCurrentCredits.isNotEmpty()) {
            if (userCurrentCredits.toInt() >= feature.credit_price) {

                if (auth.currentUser != null) {
                    val userId = auth.currentUser!!.uid
//                    val reference = firebaseDatabase.child(Constants.firebaseUserFeatureDetails).child(userId)
//
//                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//                            //reference.removeEventListener(listener!!)
//                            var totalMemory = 0
//                            var foundMemory:Float = 0F
//                            var foundStorage = 0
//                            var isFoundValue = false
//                            val params = HashMap<String,Any>()
//                            if (dataSnapshot.exists()) {
//                                if (feature.name.contains("storage")){
//                                    if (dataSnapshot.hasChild("memory")){
//                                        isFoundValue = true
//                                        foundMemory = dataSnapshot.child("memory").getValue(String::class.java)!!.toFloat()
//                                        totalMemory = dataSnapshot.child("total_memory").getValue(Int::class.java)!!
//                                        if (dataSnapshot.hasChild("duration")){
//                                            foundStorage = dataSnapshot.child("duration").getValue(Int::class.java)!!
//                                        }
//                                    }
//                                    else{
//                                        isFoundValue = false
//                                    }
//
//                                }
//                                else{
//                                    if (dataSnapshot.hasChild("duration")){
//                                        isFoundValue = true
//                                        foundStorage = dataSnapshot.child("duration").getValue(Int::class.java)!!
//                                    }
//                                    else{
//                                        isFoundValue = false
//                                    }
//                                }
//
//                                if (isFoundValue) {
////                                        reference.removeEventListener(listener!!)
//                                    if (feature.name.contains("storage")){
//                                        val tMemory = foundMemory + feature.memory
//                                        val total = totalMemory + feature.memory
//                                        feature.memory = tMemory
//                                        params["memory"] = tMemory.toString()
//                                        params["total_memory"] = total
//                                        feature.createdAt = System.currentTimeMillis()
//                                        feature.duration += foundStorage
//                                        feature.expiredAt = addDaysCalenderDate(feature.duration).timeInMillis
//                                        params["createdAt"] = feature.createdAt
//                                        params["duration"] = feature.duration
//                                        params["expiredAt"] = feature.expiredAt
//                                    }
//                                    else{
//                                        feature.createdAt = System.currentTimeMillis()
//                                        feature.duration += foundStorage
//                                        feature.expiredAt = addDaysCalenderDate(feature.duration).timeInMillis
//                                        params["createdAt"] = feature.createdAt
//                                        params["duration"] = feature.duration
//                                        params["expiredAt"] = feature.expiredAt
//                                    }
//
//
//                                    firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
//                                        .child(userId)
//                                        .updateChildren(params)
//                                    dismiss()
//                                    val hashMap = HashMap<String, String>()
//                                    val remaining = userCurrentCredits.toInt() - feature.credit_price
//                                    hashMap["credits"] = remaining.toString()
//                                    firebaseDatabase.child(Constants.firebaseUserCredits)
//                                        .child(userId)
//                                        .setValue(hashMap)
//                                        .addOnSuccessListener {
//
//                                        }
//                                        .addOnFailureListener {
//
//                                        }
//
//                                } else {
////                                        reference.removeEventListener(listener!!)
//                                    if (feature.name.contains("storage")){
//                                        val tMemory = feature.memory
//                                        feature.memory = tMemory
//                                        params["memory"] = tMemory.toString()
//                                        params["total_memory"] = tMemory
//                                        params["createdAt"] = System.currentTimeMillis()
//                                        params["duration"] = feature.duration
//                                        params["expiredAt"] = addDaysCalenderDate(feature.duration).timeInMillis
//                                    }
//                                    else{
//                                        feature.createdAt = System.currentTimeMillis()
//                                        feature.expiredAt = addDaysCalenderDate(feature.duration).timeInMillis
//                                        params["createdAt"] = feature.createdAt
//                                        params["duration"] = feature.duration
//                                        params["expiredAt"] = feature.expiredAt
//
//                                    }
//
//
//                                    firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
//                                        .child(userId)
//                                        .updateChildren(params)
//                                    dismiss()
//                                    val hashMap = HashMap<String, String>()
//                                    val remaining =
//                                        userCurrentCredits.toInt() - feature.credit_price
//                                    hashMap["credits"] = remaining.toString()
//                                    firebaseDatabase.child(Constants.firebaseUserCredits)
//                                        .child(userId)
//                                        .setValue(hashMap)
//                                        .addOnSuccessListener {
//
//                                        }
//                                        .addOnFailureListener {
//
//                                        }
//                                }
//
//                            }
//                            else{
////                                    reference.removeEventListener(listener!!)
//                                if (feature.name.contains("storage")){
//                                    val tMemory = feature.memory
//                                    feature.memory = tMemory
//                                    params["memory"] = tMemory.toString()
//                                    params["total_memory"] = tMemory
//                                    params["createdAt"] = System.currentTimeMillis()
//                                    params["duration"] = feature.duration
//                                    params["expiredAt"] = addDaysCalenderDate(feature.duration).timeInMillis
//                                }
//                                else {
//                                    feature.createdAt = System.currentTimeMillis()
//                                    feature.expiredAt = addDaysCalenderDate(feature.duration).timeInMillis
//                                    params["createdAt"] = feature.createdAt
//                                    params["duration"] = feature.duration
//                                    params["expiredAt"] = feature.expiredAt
//                                }
//
//                                firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
//                                    .child(userId)
//                                    .updateChildren(params)
//                                dismiss()
//                                val hashMap = HashMap<String, String>()
//                                val remaining =
//                                    userCurrentCredits.toInt() - feature.credit_price
//                                hashMap["credits"] = remaining.toString()
//                                firebaseDatabase.child(Constants.firebaseUserCredits)
//                                    .child(userId)
//                                    .setValue(hashMap)
//                                    .addOnSuccessListener {
//
//                                    }
//                                    .addOnFailureListener {
//
//                                    }
//                            }
//
//                            getUserCredits(context)
//                        }
//
//                        override fun onCancelled(databaseError: DatabaseError) {
//                        }
//                    })

                    purchaseFeatures(context,feature,userId,object :APICallback{
                        override fun onSuccess(response: JSONObject) {
                            dismiss()
                            if (response.getInt("status") == 200) {

                                val hashMap = HashMap<String, String>()
                                val remaining =
                                    userCurrentCredits.toInt() - feature.credit_price
                                hashMap["credits"] = remaining.toString()
                                firebaseDatabase.child(Constants.firebaseUserCredits)
                                    .child(userId)
                                    .setValue(hashMap)
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener {

                                    }

                                showAlert(context, "Congratulation on purchasing the subscription!")
                            } else {
                                val message = response.getString("message")
                                showAlert(context, message)
                            }
                        }

                        override fun onError(error: VolleyError) {
                            dismiss()
                        }

                    })

//                    reference.addValueEventListener(listener!!)
                }
            } else {
                dismiss()
                showAlert(context, "You can't purchase this feature due to zero or less credits!")
            }
        }

    }

}