package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.model.HelpObject
import com.boris.expert.csvmagic.repository.DataRepository

class HelpActivityViewModel : ViewModel() {

    private var helpVideosList = MutableLiveData<List<HelpObject>>()

    fun callHelpVideosList(context: Context,langVideoRef:String){
        helpVideosList = DataRepository.getInstance(context).getHelpVideosList(langVideoRef)
    }


    fun getHelpVideosList(): LiveData<List<HelpObject>> {
        return helpVideosList
    }

}