package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.retrofit.ApiRepository
import com.google.gson.JsonObject

class FeedbackQrViewModel : ViewModel() {

    private var feedbackQrCodeResponse = MutableLiveData<JsonObject>()

    fun createFeedbackQrCode(context: Context, body:HashMap<String,String>){
        feedbackQrCodeResponse = ApiRepository.getInstance(context).createFeedbackQrCode(body)
    }

    fun getFeedbackQrCode(): MutableLiveData<JsonObject> {
        return feedbackQrCodeResponse
    }

}