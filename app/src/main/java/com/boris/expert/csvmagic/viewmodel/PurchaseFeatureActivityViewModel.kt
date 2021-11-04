package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.repository.DataRepository
import com.google.gson.JsonObject
import org.json.JSONObject

class PurchaseFeatureActivityViewModel : ViewModel() {

    private var featuresList = MutableLiveData<List<Feature>>()
    private var userPackageResponse = MutableLiveData<JSONObject?>()

    fun callFeaturesList(context: Context){
        featuresList = DataRepository.getInstance(context).getFeatureList()
    }

    fun getFeaturesList(): LiveData<List<Feature>> {
        return featuresList
    }

    fun callUserPackageDetail(context: Context,userId:String){
        userPackageResponse = DataRepository.getInstance(context).getUserPackage(userId)
    }

    fun getUserPackageDetail():LiveData<JSONObject?>{
        return userPackageResponse
    }

}