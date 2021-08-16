package com.boris.expert.csvmagic

import android.app.Application
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants

class App : Application() {

    private lateinit var appSettings: AppSettings
    override fun onCreate() {
        super.onCreate()
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