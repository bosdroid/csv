package com.boris.expert.csvmagic.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boris.expert.csvmagic.retrofit.ApiRepository
import com.google.api.client.json.Json
import com.google.gson.JsonObject
import org.json.JSONObject

class SalesCustomersViewModel : ViewModel() {

    var accountResponse = MutableLiveData<JsonObject?>()
    var productsResponse = MutableLiveData<JsonObject?>()
    var updateImageResponse = MutableLiveData<JsonObject?>()
    var removeImageResponse = MutableLiveData<JsonObject?>()
    var updateProductResponse = MutableLiveData<JsonObject?>()
    var categoriesResponse = MutableLiveData<JsonObject?>()


    fun callSalesAccount(context: Context,shopName:String,email:String,password:String){
        accountResponse = ApiRepository.getInstance(context).salesAccount(shopName,email,password)
    }

    fun getSalesAccountResponse():LiveData<JsonObject?>{
        return accountResponse
    }

    fun callProducts(context: Context,shopName:String,email:String,password:String,page:Int){
        productsResponse = ApiRepository.getInstance(context).salesProducts(shopName,email,password,page)
    }

    fun getSalesProductsResponse():LiveData<JsonObject?>{
        return productsResponse
    }

    fun callUpdateProductImage(context: Context,shopName:String,email:String,password:String,image:String,pId:Int,position:Int,imageId:Int,fileName:String){
        updateImageResponse = ApiRepository.getInstance(context).updateProductImage(shopName,email,password,image,pId,position,imageId,fileName)
    }

    fun callUpdateProductImage(context: Context,url:String,body: JSONObject){
        updateImageResponse = ApiRepository.getInstance(context).updateProductImage(url,body)
    }

    fun getUpdateProductImageResponse():LiveData<JsonObject?>{
        return updateImageResponse
    }

    fun callAddProductImage(context: Context,shopName:String,email:String,password:String,image:String,pId:Int,fileName:String,src:String){
        updateImageResponse = ApiRepository.getInstance(context).addProductImage(shopName,email,password,image,pId,fileName,src)
    }


    fun getAddProductImageResponse():LiveData<JsonObject?>{
        return updateImageResponse
    }

    fun callRemoveProductImage(context: Context,shopName:String,email:String,password:String,pId:Int,imageId:Int){
        removeImageResponse = ApiRepository.getInstance(context).removeProductImage(shopName,email,password,pId,imageId)
    }

    fun getRemoveProductImageResponse():LiveData<JsonObject?>{
        return removeImageResponse
    }

    fun callUpdateProductDetail(context: Context,shopName:String,email:String,password:String,pId:Int,title:String,shortDesc:String,fullDesc:String){
        updateProductResponse = ApiRepository.getInstance(context).updateProductDetail(shopName,email,password,pId,title,shortDesc,fullDesc)
    }

    fun getUpdateProductDetailResponse():LiveData<JsonObject?>{
        return updateProductResponse
    }

    fun callCategories(context: Context,shopName:String,email:String,password:String){
        categoriesResponse = ApiRepository.getInstance(context).categories(shopName,email,password)
    }

    fun getCategoriesResponse():LiveData<JsonObject?>{
        return categoriesResponse
    }


}