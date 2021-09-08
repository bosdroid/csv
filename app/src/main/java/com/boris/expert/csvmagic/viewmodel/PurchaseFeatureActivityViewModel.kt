package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.model.Feature
import com.boris.expert.csvmagic.repository.DataRepository

class PurchaseFeatureActivityViewModel : ViewModel() {

    private var featuresList = MutableLiveData<List<Feature>>()

    fun callFeaturesList(context: Context){
        featuresList = DataRepository.getInstance(context).getFeatureList()
    }

    fun getFeaturesList(): LiveData<List<Feature>> {
        return featuresList
    }

}