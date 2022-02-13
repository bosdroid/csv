package com.boris.expert.csvmagic.retrofit

import com.boris.expert.csvmagic.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientApi {


    private val client = OkHttpClient.Builder().build()


    private val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun <T> createService(bindService: Class<T>):T{
        return retrofit.create(bindService)
    }
}