package com.boris.expert.csvmagic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    var title = MutableLiveData<String>()
    var description = MutableLiveData<String>()


    fun setTitleValue(titleValue:String){
        title.value = titleValue
    }

    fun setDescription(descValue:String){
        description.value = descValue
    }

    fun getTitleValue():LiveData<String>{
        return title
    }

    fun getDescriptionValue():LiveData<String>{
        return description
    }

}