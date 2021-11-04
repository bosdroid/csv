package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.FeaturesAdapter
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.model.Package
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.viewmodel.PurchaseFeatureActivityViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.round
import kotlin.math.roundToInt

class PurchaseFeatureActivity : BaseActivity(), FeaturesAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: PurchaseFeatureActivityViewModel
    private var featureList = mutableListOf<Feature>()
    private lateinit var adapter: FeaturesAdapter
    private lateinit var featuresRecyclerView: RecyclerView
    private var userCurrentCredits = ""
    private lateinit var appSettings: AppSettings
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_feature)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(PurchaseFeatureActivityViewModel()).createFor()
        )[PurchaseFeatureActivityViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        featuresRecyclerView = findViewById(R.id.features_recyclerview)
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.purchase_feature)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onResume() {
        super.onResume()
        getFeaturesList()
    }

    private fun getFeaturesList() {
        featuresRecyclerView.layoutManager = LinearLayoutManager(context)
        featuresRecyclerView.hasFixedSize()
        adapter = FeaturesAdapter(context, featureList as ArrayList<Feature>)
        featuresRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)

        startLoading(context)
        viewModel.callFeaturesList(context)
        viewModel.getFeaturesList().observe(this, { list ->
            if (list != null) {
                dismiss()
                if (list.isNotEmpty()) {
                    featureList.clear()
                }
                featureList.addAll(list)
                adapter.notifyItemRangeChanged(0, featureList.size)
            } else {
                dismiss()
            }
        })
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

    override fun onItemPurchaseBtnClick(position: Int) {
        val feature = featureList[position]

        startLoading(context)
        viewModel.callUserPackageDetail(context, Constants.firebaseUserId)
        viewModel.getUserPackageDetail().observe(this, Observer { response ->
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
                            val packageDetail:JSONObject? = response.getJSONObject("package")
                            userCurrentCredits =
                                appSettings.getString(Constants.userCreditsValue) as String
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


//        var packageType = ""
//        var pkgName = ""
//        var duration = 0
//        var memory = 0F
//
//        packageType = if (feature.name.contains("pro")) {
//            "pro"
//        } else {
//            "simple"
//        }
//
//        if (feature.name.contains("storage")) {
//            pkgName = "storage"
//            memory = feature.memory
//
//            if (goneDays > 0) {
//                val unitPrice = feature.credit_price / feature.duration
//                val priceAlreadyDaysGone = unitPrice * goneDays
//
//            }
//
//
//        } else {
//            pkgName = "time"
//            duration = feature.duration
//
//
//        }


//        val params = HashMap<String, Any>()
//        params["package"] = pkgName
//        params["user_id"] = Constants.firebaseUserId
//        params["duration"] = duration
//        params["package_type"] = packageType
//        params["size"] = memory

//        val hashMap = HashMap<String, String>()
//        val remaining = userCurrentCredits.toInt() - feature.credit_price
//        hashMap["credits"] = remaining.toString()
//        firebaseDatabase.child(Constants.firebaseUserCredits)
//            .child(Constants.firebaseUserId)
//            .setValue(hashMap)
//            .addOnSuccessListener {
//
//            }
//            .addOnFailureListener {
//
//            }

    }

    private var listener: ValueEventListener? = null
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
//                            var foundMemory: Float = 0F
//                            var foundStorage = 0
//                            var isFoundValue = false
//                            val params = HashMap<String, Any>()
//                            if (dataSnapshot.exists()) {
//                                if (feature.name.contains("storage")) {
//                                    if (dataSnapshot.hasChild("memory")) {
//                                        isFoundValue = true
//                                        foundMemory = dataSnapshot.child("memory")
//                                            .getValue(String::class.java)!!.toFloat()
//                                        totalMemory = dataSnapshot.child("total_memory")
//                                            .getValue(Int::class.java)!!
//                                        if (dataSnapshot.hasChild("duration")) {
//                                            foundStorage = dataSnapshot.child("duration")
//                                                .getValue(Int::class.java)!!
//                                        }
//                                    } else {
//                                        isFoundValue = false
//                                    }
//
//                                } else {
//                                    if (dataSnapshot.hasChild("duration")) {
//                                        isFoundValue = true
//                                        foundStorage = dataSnapshot.child("duration")
//                                            .getValue(Int::class.java)!!
//                                    } else {
//                                        isFoundValue = false
//                                    }
//                                }
//
//                                if (isFoundValue) {
////                                        reference.removeEventListener(listener!!)
//                                    if (feature.name.contains("storage")) {
//                                        val tMemory = foundMemory + feature.memory
//                                        val total = totalMemory + feature.memory
//                                        feature.memory = tMemory
//                                        params["memory"] = tMemory.toString()
//                                        params["total_memory"] = total
//                                        feature.createdAt = System.currentTimeMillis()
//                                        feature.duration += foundStorage
//                                        feature.expiredAt =
//                                            addDaysCalenderDate(feature.duration).timeInMillis
//                                        params["createdAt"] = feature.createdAt
//                                        params["duration"] = feature.duration
//                                        params["expiredAt"] = feature.expiredAt
//                                    } else {
//                                        feature.createdAt = System.currentTimeMillis()
//                                        feature.duration += foundStorage
//                                        feature.expiredAt =
//                                            addDaysCalenderDate(feature.duration).timeInMillis
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
//
//                                } else {
////                                        reference.removeEventListener(listener!!)
//                                    if (feature.name.contains("storage")) {
//                                        val tMemory = feature.memory
//                                        feature.memory = tMemory
//                                        params["memory"] = tMemory.toString()
//                                        params["total_memory"] = tMemory
//                                        params["createdAt"] = System.currentTimeMillis()
//                                        params["duration"] = feature.duration
//                                        params["expiredAt"] =
//                                            addDaysCalenderDate(feature.duration).timeInMillis
//                                    } else {
//                                        feature.createdAt = System.currentTimeMillis()
//                                        feature.expiredAt =
//                                            addDaysCalenderDate(feature.duration).timeInMillis
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
//                            } else {
////                                    reference.removeEventListener(listener!!)
//                                if (feature.name.contains("storage")) {
//                                    val tMemory = feature.memory
//                                    feature.memory = tMemory
//                                    params["memory"] = tMemory.toString()
//                                    params["total_memory"] = tMemory
//                                    params["createdAt"] = System.currentTimeMillis()
//                                    params["duration"] = feature.duration
//                                    params["expiredAt"] =
//                                        addDaysCalenderDate(feature.duration).timeInMillis
//                                } else {
//                                    feature.createdAt = System.currentTimeMillis()
//                                    feature.expiredAt =
//                                        addDaysCalenderDate(feature.duration).timeInMillis
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