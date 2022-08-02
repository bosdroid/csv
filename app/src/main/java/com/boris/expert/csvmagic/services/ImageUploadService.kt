package com.boris.expert.csvmagic.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.retrofit.ApiServices
import com.boris.expert.csvmagic.retrofit.RetrofitClientApi
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.ImageManager
import com.boris.expert.csvmagic.view.activities.MainActivity
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageUploadService(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private var selectedImageBase64String: String = ""
    private var selectedInternetImage: String = ""
    private var email = ""
    private var password = ""
    private var shopName = ""
    private lateinit var appSettings: AppSettings
    var builder: NotificationCompat.Builder? = null
    var notification: Notification? = null
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    var apiInterface: ApiServices = RetrofitClientApi.createService(ApiServices::class.java)


    override fun doWork(): Result {
        appSettings = AppSettings(applicationContext)
        showNotification()
        Constants.imageLoadingStatus = 1

        val productId = inputData.getInt("pId", 0)
        val imageList = inputData.getString("imageList")
        val type = inputData.getString("type")
        val flag = inputData.getBoolean("flag",false)
        val imagesArray = mutableListOf<String>()
        if (imageList!!.contains(",")) {
            imagesArray.addAll(imageList.split(","))
        } else {
            imagesArray.add(imageList)
        }
        shopName = appSettings.getString("INSALES_SHOP_NAME") as String
        email = appSettings.getString("INSALES_EMAIL") as String
        password = appSettings.getString("INSALES_PASSWORD") as String

        Constants.productId = productId
//        Handler(Looper.myLooper()!!).postDelayed({

        CoroutineScope(Dispatchers.IO).launch {
                 delay(8000)
            try {

                if (imagesArray.isNotEmpty()) {
//                    updateNotificationProgress(100)
                    uploadImages(
                        productId,
                        imagesArray,
                        appSettings,
                        object : ResponseListener {
                            override fun onSuccess(result: String) {
                                if (result.contains("success")) {
                                    notificationManager.cancel(101)
                                    Constants.multiImagesSelectedListSize = 0
                                    Constants.imageLoadingStatus = 0
                                    Constants.productId = 0
                                    Toast.makeText(applicationContext,"Product images attached successfully!",Toast.LENGTH_SHORT).show()
//                                    if (flag) {
                                        val intent =
                                                Intent("update-images")
                                        intent.putExtra("PID", productId)
                                        LocalBroadcastManager.getInstance(
                                                applicationContext
                                        ).sendBroadcast(intent)
//                                    }
                                }
                                else{
                                    notificationManager.cancel(101)
                                }
                            }

                        })
                } else {
                    notificationManager.cancel(101)
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                notificationManager.cancel(101)
            }
        }

//        },3000)

        return Result.success()
    }

    var index = 0
    private fun uploadImages(
        productId: Int,
        listImages: List<String>,
        appSettings: AppSettings,
        responseListener: ResponseListener
    ) {

        var imageType = ""
        val imageFile = listImages[index]
        Log.d("TEST199",imageFile)
        if (imageFile.contains("http")) {
            imageType = "src"
            selectedInternetImage = imageFile
        } else {
            imageType = "attachment"
            selectedImageBase64String = ImageManager.convertImageToBase64(
                applicationContext,
                imageFile
            )
        }

        apiInterface.addProductImage(
            email,
            password,
            shopName,
            selectedImageBase64String,
            productId,
            "${System.currentTimeMillis()}.jpg",
            if (imageType == "attachment") {
                ""
            } else {
                selectedInternetImage
            }
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>?) {
                if (response != null) {
                    selectedImageBase64String = ""
                    selectedInternetImage = ""

                    if (index == listImages.size - 1) {
                        index = 0
                        responseListener.onSuccess("success")
                    } else {
                        index++
                        uploadImages(productId, listImages, appSettings, responseListener)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
               Log.d("TEST199",t.message.toString())
                responseListener.onSuccess("fail")
            }

        })
    }

//    private fun updateNotificationProgress(progress: Int) {
//        builder!!.contentView.setProgressBar(R.id.progressBar, 100, progress, true)
//        notificationManager.notify(101, notification)
//    }

    private fun showNotification() {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val intent = PendingIntent.getActivity(
            context, 0,
            notificationIntent, PendingIntent.FLAG_MUTABLE or 0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel = NotificationChannel(
                Constants.channelId,
                Constants.channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(defaultChannel)
        }

        builder = NotificationCompat.Builder(context, Constants.channelId)
            .setSmallIcon(R.drawable.ic_cloud_upload)
//            .setContentTitle("SimplePdfToImages")
            .setContentText("Insales Product images upload in progress....")
            .setOngoing(true)
            .setContentIntent(intent)
            .setProgress(100, 100, true)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_cloud_upload
                )
            )

        notification = builder!!.build()
        notificationManager.notify(101, notification)
    }
}