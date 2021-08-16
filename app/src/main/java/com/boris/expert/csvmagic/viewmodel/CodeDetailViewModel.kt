package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.model.FeedbackResponse
import com.boris.expert.csvmagic.retrofit.ApiRepository
import com.google.gson.JsonObject

class CodeDetailViewModel : ViewModel() {

    var feedbackResponse = MutableLiveData<FeedbackResponse>()
    private var dynamicQrCodeResponse = MutableLiveData<JsonObject>()

    fun createDynamicQrCode(context: Context, body:HashMap<String,String>){
        dynamicQrCodeResponse = ApiRepository.getInstance(context).createDynamicQrCode(body)
    }

    fun getDynamicQrCode():MutableLiveData<JsonObject>{
        return dynamicQrCodeResponse
    }

    fun callFeedbacks(context: Context,id:String){
        feedbackResponse = ApiRepository.getInstance(context).getAllFeedbacks(id)
    }

    fun getAllFeedbacks():MutableLiveData<FeedbackResponse>{
        return feedbackResponse
    }

}