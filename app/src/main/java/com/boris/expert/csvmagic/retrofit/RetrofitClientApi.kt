package com.boris.expert.csvmagic.retrofit

import com.boris.expert.csvmagic.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientApi {

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun<T> createService(bindService : Class<T>):T{
        return retrofit.create(bindService)
    }

}