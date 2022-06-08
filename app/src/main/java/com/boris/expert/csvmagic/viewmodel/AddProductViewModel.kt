package com.boris.expert.csvmagic.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddProductViewModel : ViewModel() {


    var apTitleValue = MutableLiveData<String>()

    fun updateApTitleValue(updateValue:String){
            apTitleValue.value = updateValue
        Log.d("TEST199TITLE",apTitleValue.value.toString())
    }

    fun getApTitleValue():LiveData<String>{
        return apTitleValue
    }

}