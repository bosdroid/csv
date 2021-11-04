package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.UploadImageCallback
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.repository.DataRepository
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.Constants.Companion.EMAIL_ADDRESS_PATTERN
import com.boris.expert.csvmagic.utils.DialogPrefs
import com.boris.expert.csvmagic.utils.VolleySingleton
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUserPackageDetail(this)
    }

    companion object {
        private var prDownloader: DownloadRequest? = null
        var alert: AlertDialog? = null

        // THIS FUNCTION WILL CHECK THE INTERNET CONNECTION AVAILABLE OR NOT
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = connectivityManager.getNetworkCapabilities(
                    connectivityManager.activeNetwork
                )
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
            return false
        }

        // THIS FUNCTION WILL RETURN THE DATE TIME STRING FROM TIMESTAMP
        fun getDateTimeFromTimeStamp(timeStamp: Long): String {
            val c = Date(timeStamp)
            val df = SimpleDateFormat("yyyy-MM-dd kk:mm a", Locale.getDefault())
            return df.format(c).toUpperCase(Locale.ENGLISH)
        }

        // THIS FUNCTION WILL SET THE FONT FAMILY
        fun setFontFamily(context: Context, view: MaterialTextView, path: String) {
            if (path.contains("http") || path.contains("https")) {
                val extension = path.substring(path.lastIndexOf("."), path.indexOf("?"))
                val fileName = "tempFont$extension"
                val filePath = context.externalCacheDir.toString() + "/fonts"
                val downloadFile = File(filePath, fileName)
                if (downloadFile.exists()) {
                    downloadFile.delete()
                }

                prDownloader = PRDownloader.download(path, filePath, fileName)
                    .build()
                    .setOnStartOrResumeListener {

                    }
                prDownloader!!.start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        val face = Typeface.createFromFile(downloadFile)
                        view.typeface = face
                    }

                    override fun onError(error: Error?) {
                        Log.d("TEST199", error.toString())
                    }
                })
            } else {
                MaterialAlertDialogBuilder(context)
                    .setMessage(context.resources.getString(R.string.font_file_error_text))
                    .setCancelable(false)
                    .setPositiveButton(context.resources.getString(R.string.ok_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .create().show()
            }

        }

        fun hideKeyboard(context: Context, activity: MainActivity) {
            val view: View? = activity.currentFocus
            if (view != null) {
                val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun openLink(context: Context, url: String) {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setMessage(context.getString(R.string.barcode_image_open_browser_message))
            builder.setCancelable(false)
            builder.setNegativeButton(context.getString(R.string.no_text)) { dialog, which ->
                dialog.dismiss()
            }
            builder.setPositiveButton(context.getString(R.string.yes_text)) { dialog, which ->
                dialog.dismiss()
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                context.startActivity(i)
            }
            val alert = builder.create()
            alert.show()
        }


        fun uploadImageOnServer(
            context: Context,
            image: String,
            user_id: String,
            listener: UploadImageCallback
        ) {
            val stringRequest = object : StringRequest(
                Method.POST, "https://itmagicapp.com/api/images_uploader.php",
                Response.Listener {
                    val response = JSONObject(it)
                    if (response.getInt("status") == 200) {
                        val url = response.getString("url")
                        listener.onSuccess(url)
                    }
                }, Response.ErrorListener {
                    Log.d("TEST199", it.localizedMessage!!)
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["image"] = image
                    params["user_id"] = user_id
                    return params
                }
            }

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

        fun updateMemorySize(
            context: Context,
            size: String,
            user_id: String,
            update_total:Int,
            listener: APICallback
        ) {
            val stringRequest = object : StringRequest(
                Method.POST, "https://itmagicapp.com/api/package_update_size.php",
                Response.Listener {
                    val response = JSONObject(it)
                    listener.onSuccess(response)
                }, Response.ErrorListener {
                    listener.onError(it)
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["size"] = size
                    params["user_id"] = user_id
                    params["update_total"] = "$update_total"
                    return params
                }
            }

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

        fun getUserPackageDetail(context: Context){
            val stringRequest  = object : StringRequest(
                Method.POST, "https://itmagicapp.com/api/get_user_packages.php",
                Response.Listener {
                    val response = JSONObject(it)
                    if (response.getInt("status") == 200) {
                        val packageDetail:JSONObject? = response.getJSONObject("package")
                        val availableSize = packageDetail!!.getString("size")
                        Constants.userServerAvailableStorageSize = availableSize
                    }
                }, Response.ErrorListener {
                    Log.d("TEST199", it.localizedMessage!!)

                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user_id"] = Constants.firebaseUserId
                    return params
                }
            }

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

        fun purchaseFeatures(context: Context,feature: Feature, user_id: String, listener: APICallback){
            val stringRequest = object : StringRequest(
                Method.POST, "https://itmagicapp.com/api/packages_manager.php",
                Response.Listener {
                    val response = JSONObject(it)
                    listener.onSuccess(response)
                }, Response.ErrorListener {
                    Log.d("TEST199", it.localizedMessage!!)
                    listener.onError(it)
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user_id"] = user_id
                    params["package"] = if (feature.name.contains("storage")){"storage"}else{"time"}
                    params["duration"] = feature.duration.toString()
                    params["package_type"] = feature.type
                    params["size"] = feature.memory.toString()
                    params["total_size"] = feature.memory.toString()
                    return params
                }
            }

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

        // THIS FUNCTION WILL ALERT THE DIFFERENT MESSAGES
        fun showAlert(context: Context, message: String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        }

        fun startLoading(context: Context) {
            if (alert == null){
                val builder = MaterialAlertDialogBuilder(context)
                val layout = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
                builder.setView(layout)
                builder.setCancelable(false)
                alert = builder.create()
                alert!!.show()
            }
        }

        fun dismiss() {
            if (alert != null) {
                alert!!.dismiss()
                alert = null
            }
        }

        fun addDaysCalenderDate(days: Int): Calendar {
            val sdf = SimpleDateFormat("yyyy-MM-dd kk:mm a", Locale.ENGLISH)
            val c = Calendar.getInstance()
            c.time = sdf.parse(getDateTimeFromTimeStamp(System.currentTimeMillis()))!!
            c.add(Calendar.DATE, days)
            return c
        }

        fun getDateFromTimeStamp(timeStamp: Long): String {
            val c: Date = Date(timeStamp)
            val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return df.format(c).toUpperCase(Locale.ENGLISH)
        }

        fun getFormattedDate(context: Context?, smsTimeInMilis: Long): String {
            val smsTime = Calendar.getInstance()
            smsTime.timeInMillis = smsTimeInMilis
            val now = Calendar.getInstance()
            val timeFormatString = "h:mm:ss"
            val dateTimeFormatString = "EEEE, MMMM d, h:mm:ss"

            return if (now[Calendar.DATE] == smsTime[Calendar.DATE]) {
                "Today " + DateFormat.format(timeFormatString, smsTime)
            } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1) {
                "Yesterday " + DateFormat.format(timeFormatString, smsTime)
            } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
                DateFormat.format(dateTimeFormatString, smsTime).toString()
            } else {
                DateFormat.format("MMMM dd yyyy, h:mm:ss", smsTime).toString()
            }
        }

        fun rateUs(context: AppCompatActivity) {
            val inflater = context.layoutInflater
            val view = inflater.inflate(R.layout.layout_dialog_rate_us, null)
            val builder = AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(view)

            val later = view.findViewById<AppCompatTextView>(R.id.laterTv)
            val ratingBar = view.findViewById<AppCompatRatingBar>(R.id.ratingBar)

            val alertDialog = builder.show()
            ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (rating <= 4.0) {
                    contactSupport(context)
                    alertDialog.dismiss()
                } else {
                    rateAppOnPlay(context)
                    alertDialog.dismiss()
                }
            }
            later.setOnClickListener {
                DialogPrefs.clearPreferences(context)
                alertDialog.dismiss()
            }
        }

        private fun rateAppOnPlay(context: AppCompatActivity) {
            val rateIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.packageName)
                )
            context.startActivity(rateIntent)
        }

        fun getUserCredits(context: Context) {
            val appSettings = AppSettings(context)
            var userCurrentCreditsValue: Int = 0
            val auth = FirebaseAuth.getInstance()
            val firebaseDatabase = FirebaseDatabase.getInstance().reference
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
                            appSettings.putString(
                                Constants.userCreditsValue,
                                "$userCurrentCreditsValue"
                            )
                            Log.d("TEST199", "$userCurrentCreditsValue")
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                firebaseDatabase.child(Constants.firebaseUserCredits)
                    .child(userId).addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            userCurrentCreditsValue =
                                if (snapshot.hasChildren() && snapshot.hasChild(
                                        "credits"
                                    )
                                ) {
                                    val previousCredits =
                                        snapshot.child("credits").getValue(String::class.java)
                                    previousCredits!!.toInt()
                                } else {
                                    0
                                }
                            appSettings.putString(
                                Constants.userCreditsValue,
                                "$userCurrentCreditsValue"
                            )
                            Log.d("TEST199", "$userCurrentCreditsValue")
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

            }
        }

        fun getCurrentSubscriptionDetail(context: Context) {
            val appSettings = AppSettings(context)
            val auth = FirebaseAuth.getInstance()
            val firebaseDatabase = FirebaseDatabase.getInstance().reference

            if (auth.currentUser != null) {
                val userId = auth.currentUser!!.uid
                Constants.firebaseUserId = userId
                var duration: Int = 0
                var memory: Float = 0F
                var expiredAt: Long = 0
                firebaseDatabase.child(Constants.firebaseUserFeatureDetails)
                    .child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            if (snapshot.hasChildren() && snapshot.hasChild("duration")) {
                                duration = snapshot.child("duration").getValue(Int::class.java)!!
                            }


                            if (snapshot.hasChildren() && snapshot.hasChild("memory")) {
                                memory = snapshot.child("memory").getValue(String::class.java)!!
                                    .toFloat()
                            }

                            if (snapshot.hasChildren() && snapshot.hasChild("expiredAt")) {
                                expiredAt = snapshot.child("expiredAt").getValue(Long::class.java)!!
                            }
                            appSettings.putInt(Constants.duration, duration)
                            appSettings.putString(Constants.memory, memory.toString())
                            appSettings.putLong(Constants.expiredAt, expiredAt)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }

        }

        fun contactSupport(context: AppCompatActivity) {
            val intent = Intent(Intent.ACTION_SENDTO)
            // only email apps should handle this
            intent.type = "message/rfc822"
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            try {
                context.startActivity(Intent.createChooser(intent, "Send Mail..."))
            } catch (e: Exception) {
//                Toast.makeText(context, "No app found to handle this intent", Toast.LENGTH_LONG)
//                    .show()
            }

        }

        fun setUpToolbar(context: AppCompatActivity, toolbar: Toolbar, title: String) {
            context.setSupportActionBar(toolbar)
            context.supportActionBar!!.title = title
            context.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
        }

        fun checkEmail(email: String): Boolean {
            return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
        }

        fun showSoftKeyboard(context: Context, view: View) {
            if (view.requestFocus()) {
                val imm: InputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun hideSoftKeyboard(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }


    }

}