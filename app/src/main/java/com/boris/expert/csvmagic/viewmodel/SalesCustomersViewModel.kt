package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.retrofit.ApiRepository
import com.google.gson.JsonObject

class SalesCustomersViewModel : ViewModel() {

    var accountResponse = MutableLiveData<JsonObject?>()


    fun callSalesAccount(context: Context,shopName:String,email:String,password:String){
        accountResponse = ApiRepository.getInstance(context).salesAccount(shopName,email,password)
    }

    fun getSalesAccountResponse():LiveData<JsonObject?>{
        return accountResponse
    }

}