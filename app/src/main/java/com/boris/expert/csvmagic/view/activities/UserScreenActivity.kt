package com.boris.expert.csvmagic.view.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class UserScreenActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var appSettings: AppSettings
    private lateinit var auth: FirebaseAuth
    var userCurrentCreditsValue: Int = 0
    private lateinit var usCurrentCreditView:MaterialTextView
    private lateinit var usStorageSpaceView:MaterialTextView
    private lateinit var usDurationView:MaterialTextView
    private lateinit var usExpiredAtView:MaterialTextView

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
        usExpiredAtView = findViewById(R.id.us_expired_at_view)
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

    private fun getUserCredit(){
        if(auth.currentUser != null) {

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

    private fun getUserSubscriptionDetails(){
        if(auth.currentUser != null) {
            startLoading(context)
            val userId = auth.currentUser!!.uid

            var total:Int = 0
            var duration:Int = 0
            var memory :Float = 0F
            var expiredAt:Long = 0
            firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
                .child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dismiss()
                        if (snapshot.hasChildren() && snapshot.hasChild("duration")) {
                            duration = snapshot.child("duration").getValue(Int::class.java)!!
                            val createdAt = snapshot.child("createdAt").getValue(Long::class.java)!!
                            val expiredAtx = snapshot.child("expiredAt").getValue(Long::class.java)!!
                            usDurationView.text = "${calculateDays(createdAt,expiredAtx)} Days"
                        } else {
                            usDurationView.text = "$duration Days"
                        }

                        if (snapshot.hasChildren() && snapshot.hasChild("memory")) {
                            total = snapshot.child("total_memory").getValue(Int::class.java)!!
                            memory = snapshot.child("memory").getValue(String::class.java)!!.toFloat()
                            usStorageSpaceView.text = "${String.format("%.1f",memory)} MB of $total MB"
                        } else {
                            usStorageSpaceView.text = "$memory MB"
                        }

                        if (snapshot.hasChildren() && snapshot.hasChild("expiredAt")) {
                            expiredAt = snapshot.child("expiredAt").getValue(Long::class.java)!!
                            usExpiredAtView.text = getDateTimeFromTimeStamp(expiredAt)
                        } else {
                            usExpiredAtView.text = "N/A"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                       dismiss()
                    }

                })

        }
    }

    private fun calculateDays(createdAt: Long, expiredAtx: Long): Int {
        val diff:Long = expiredAtx - createdAt
        val totalDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        val diff1 = System.currentTimeMillis() - createdAt
        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()
        return totalDays - goneDays
    }


}