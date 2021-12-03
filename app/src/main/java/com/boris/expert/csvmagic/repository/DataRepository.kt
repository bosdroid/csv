package com.boris.expert.csvmagic.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.boris.expert.csvmagic.interfaces.UploadImageCallback
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.model.Fonts
import com.boris.expert.csvmagic.model.HelpObject
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.VolleySingleton
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.File

class DataRepository {

    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    companion object {
        lateinit var context: Context

        private var dataRepository: DataRepository? = null

        fun getInstance(mContext: Context): DataRepository {
            context = mContext
            if (dataRepository == null) {
                dataRepository = DataRepository()
            }
            return dataRepository!!
        }
    }


    // THIS FUNCTION WILL FETCH THE FIREBASE BACKGROUND IMAGES
    fun getBackgroundImages(): MutableLiveData<List<String>> {

        val backgroundImages = MutableLiveData<List<String>>()
        val imageList = mutableListOf<String>()

        databaseReference.child(Constants.firebaseBackgroundImages)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            imageList.add(url!!)
                        }
                        backgroundImages.postValue(imageList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    backgroundImages.postValue(null)
                }
            })

        return backgroundImages
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE LOGO IMAGES
    fun getLogoImages():MutableLiveData<List<String>>{
        val logoImages = MutableLiveData<List<String>>()
        val logoList = mutableListOf<String>()

        databaseReference.child(Constants.firebaseLogoImages)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            logoList.add(url!!)
                        }
                        logoImages.postValue(logoList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    logoImages.postValue(null)
                }
            })

        return logoImages
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE FONT LIST
    fun getFontList():MutableLiveData<List<Fonts>>{
        val fontList = MutableLiveData<List<Fonts>>()
        val list = mutableListOf<Fonts>()

        databaseReference.child(Constants.firebaseFonts)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {

                            val font = Fonts(postSnapshot.child("image_url").getValue(String::class.java)!!,
                                postSnapshot.child("font_url").getValue(String::class.java)!!)
                            list.add(font)

                        }
                        fontList.postValue(list)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    fontList.postValue(null)
                }
            })

        return fontList
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE FEATURE LIST
    fun getFeatureList():MutableLiveData<List<Feature>>{
        val featureList = MutableLiveData<List<Feature>>()
        val list = mutableListOf<Feature>()
        databaseReference.child(Constants.firebaseFeatures)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        list.clear()
                        for (postSnapshot in dataSnapshot.children) {

                            val item = postSnapshot.getValue(Feature::class.java) as Feature
                            list.add(item)

                        }
                        featureList.postValue(list)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    featureList.postValue(null)
                }
            })
        return featureList
    }

    fun getUserPackage(userId:String):MutableLiveData<JSONObject?>{
        val userPackageDetail = MutableLiveData<JSONObject?>()


            val stringRequest  = object : StringRequest(
                Method.POST, "https://itmagicapp.com/api/get_user_packages.php",
                Response.Listener {
                    val response = JSONObject(it)
                    if (response.getInt("status") == 200) {
                        userPackageDetail.postValue(response)
                    }
                }, Response.ErrorListener {
                    Log.d("TEST199", it.localizedMessage!!)
                    userPackageDetail.postValue(null)
                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user_id"] = userId
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

        return userPackageDetail

    }

    fun getHelpVideosList(langVideoRef:String):MutableLiveData<List<HelpObject>>{
        val helpVideosList = MutableLiveData<List<HelpObject>>()
        val list = mutableListOf<HelpObject>()

        databaseReference.child(langVideoRef)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {

                            val video = HelpObject(postSnapshot.child("type").getValue(String::class.java)!!,
                                postSnapshot.child("link").getValue(String::class.java)!!)
                            list.add(video)

                        }
                        helpVideosList.postValue(list)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    helpVideosList.postValue(null)
                }
            })

        return helpVideosList
    }

}