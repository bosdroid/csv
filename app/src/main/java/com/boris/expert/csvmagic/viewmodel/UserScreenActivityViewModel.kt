package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.retrofit.ApiRepository
import com.google.gson.JsonObject
import org.json.JSONObject

class UserScreenActivityViewModel : ViewModel() {

    private var purchaseResponse = MutableLiveData<JsonObject>()
    private var userPackageDetail = MutableLiveData<JSONObject?>()

    fun callPurchase(context: Context, packageName: String, productId: String, token: String) {
        purchaseResponse =
            ApiRepository.getInstance(context).purchase(packageName, productId, token)
    }

    fun getPurchaseResponse(): LiveData<JsonObject> {
        return purchaseResponse
    }

    fun callUserPackageDetail(context: Context, user_id: String) {
        userPackageDetail = ApiRepository.getInstance(context).getUserPackageDetail(user_id)
    }

    fun getUserPackageResponse(): LiveData<JSONObject?> {
        return userPackageDetail
    }

}