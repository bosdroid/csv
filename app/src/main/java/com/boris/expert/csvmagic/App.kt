package com.boris.expert.csvmagic

import android.app.Application
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.google.firebase.database.FirebaseDatabase

class App : Application() {

    private lateinit var appSettings: AppSettings
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        appSettings = AppSettings(applicationContext)
        getUserDetail()

    }


    private fun getUserDetail(){
        if (appSettings.getBoolean(Constants.isLogin)){
            val user = appSettings.getUser(Constants.user)
            Constants.userData = user
        }
    }

}