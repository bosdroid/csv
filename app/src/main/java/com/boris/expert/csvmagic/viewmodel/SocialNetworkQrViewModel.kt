package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.model.SNPayload
import com.boris.expert.csvmagic.retrofit.ApiRepository
import com.google.gson.JsonObject

class SocialNetworkQrViewModel : ViewModel() {

    private var snQrCodeResponse = MutableLiveData<JsonObject>()

    fun createSnQrCode(context: Context, body: SNPayload){
        snQrCodeResponse = ApiRepository.getInstance(context).createSnTemplate(body)
    }

    fun getSnQrCode(): MutableLiveData<JsonObject> {
        return snQrCodeResponse
    }

}