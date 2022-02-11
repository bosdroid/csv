package com.boris.expert.csvmagic.retrofit

import com.boris.expert.csvmagic.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientApi {


    companion object{
        private var retrofit:Retrofit?=null

        fun getInstance():Retrofit{
            val client:OkHttpClient = OkHttpClient.Builder().addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request: Request = chain.request()
                    var string: String = request.url().toString()
                    string = string.replace("%40", "@")
                    val newRequest: Request = Request.Builder()
                        .url(string)
                        .build()
                    return chain.proceed(newRequest)
                }

            }).build()



            if (retrofit == null){
                retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            }
            return retrofit!!
        }

        fun <T> createService(bindService: Class<T>):T{
            return retrofit!!.create(bindService)
        }
    }

//    private val client = OkHttpClient.Builder().build()
//
//
//    private val retrofit = Retrofit.Builder()
//            .baseUrl(Constants.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()


}