package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.FeaturesAdapter
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.viewmodel.PurchaseFeatureActivityViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

        MaterialAlertDialogBuilder(context)
            .setMessage("Are you sure you want to purchase this feature?")
            .setCancelable(false)
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                dialog.dismiss()
                purchaseFeature(feature)
            }.create().show()

    }

    private var listener:ValueEventListener?=null
    private fun purchaseFeature(feature: Feature) {
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
        startLoading(context)
        if (userCurrentCredits.isNotEmpty()) {
            if (userCurrentCredits.toInt() > 0 && userCurrentCredits.toInt() >= 1 && userCurrentCredits.toInt() >= feature.credit_price) {

                if (auth.currentUser != null) {
                    val userId = auth.currentUser!!.uid
                    val reference = firebaseDatabase.child(Constants.firebaseUserFeatureDetails).child(userId)

                    listener = reference.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                reference.removeEventListener(listener!!)
                                var previouskey = ""
                                var foundFeature: Feature? = null
                                if (dataSnapshot.exists()) {
                                    for (postSnapshot in dataSnapshot.children) {
                                        previouskey = postSnapshot.key as String
                                        val item =
                                            postSnapshot.getValue(Feature::class.java) as Feature
                                        if (item.name == feature.name) {
                                            foundFeature = item
                                            break
                                        }
                                    }

                                    if (foundFeature != null) {
                                        reference.removeEventListener(listener!!)
                                        Log.d("TEST199DURATION","step")
                                        feature.createdAt = System.currentTimeMillis()
                                        feature.duration += 30
                                        feature.expiredAt = addDaysCalenderDate(feature.duration).timeInMillis

                                        firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
                                            .child(userId)
                                            .child(previouskey)
                                            .setValue(feature)
                                            .addOnSuccessListener {
                                                dismiss()
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

                                            }
                                            .addOnFailureListener {
                                                dismiss()
                                            }

                                    } else {
                                        reference.removeEventListener(listener!!)
                                        feature.createdAt = System.currentTimeMillis()
                                        feature.expiredAt = addDaysCalenderDate(30).timeInMillis

                                        firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
                                            .child(userId)
                                            .push()
                                            .setValue(feature)
                                            .addOnSuccessListener {
                                                dismiss()
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

                                            }
                                            .addOnFailureListener {
                                                dismiss()
                                            }
                                    }

                                }
                                else{
                                    reference.removeEventListener(listener!!)
                                    feature.createdAt = System.currentTimeMillis()
                                    feature.expiredAt = addDaysCalenderDate(30).timeInMillis

                                    firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
                                        .child(userId)
                                        .push()
                                        .setValue(feature)
                                        .addOnSuccessListener {
                                            dismiss()
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

                                        }
                                        .addOnFailureListener {
                                            dismiss()
                                        }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        })
                    reference.addValueEventListener(listener!!)
                }
            } else {
                dismiss()
                showAlert(context, "You can't purchase this feature due to zero or less credits!")
            }
        }

    }


}