package com.boris.expert.csvmagic.retrofit

import com.boris.expert.csvmagic.model.FeedbackResponse
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/user/add")
    fun createDynamicQrCode(@Body body: JsonObject): Call<JsonObject>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/webpage/create/template1")
    fun createCouponQrCode(@Body body: JsonObject): Call<JsonObject>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING FEEDBACK QR CODE
    @POST("service/webpage/create/feedbacktemplate")
    fun createFeedbackQrCode(@Body body: JsonObject): Call<JsonObject>

    @POST("service/user/google/add")
    fun signUp(@Body body: JsonObject): Call<JsonObject>

    @GET("service/user/google/{email}")
    fun signIn(@Path("email") email: String): Call<JsonObject>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING SOCIAL NETWORK QR CODE
    @POST("service/webpage/create/sntemplate")
    fun createSnTemplate(@Body body: JsonObject): Call<JsonObject>

    @GET("service/feedback/{id}")
    fun getAllFeedbacks(@Path("id") id: String): Call<FeedbackResponse>

    @GET("service/purchase/verify/{packageName}/{productId}/{token}")
    fun purchase(
            @Path("packageName") packageName: String,
            @Path("productId") productId: String,
            @Path("token") token: String
    ): Call<JsonObject>


    @FormUrlEncoded
    @POST("insales/login.php")
    fun salesLoginAccount(@Field("email") email: String,@Field("password") password:String,@Field("shop_name") shopName:String): Call<JsonObject>

    @FormUrlEncoded
    @POST("insales/products.php")
    fun salesProducts(@Field("email") email: String,@Field("password") password:String,@Field("shop_name") shopName:String,@Field("page") page:Int): Call<JsonObject>

    @FormUrlEncoded
    @POST("insales/update_image.php")
    fun updateProductImage(@Field("email") email: String, @Field("password") password:String, @Field("shop_name") shopName:String, @Field("image") image:String, @Field("p_id") pId:Int, @Field("image_position") position:Int, @Field("image_id") imageId:Int, @Field("file_name") fileName:String): Call<JsonObject>

    @FormUrlEncoded
    @POST("insales/add_image.php")
    fun addProductImage(@Field("email") email: String,@Field("password") password:String,@Field("shop_name") shopName:String,@Field("image") image:String,@Field("p_id") pId:Int,@Field("file_name") fileName:String,@Field("src") src:String): Call<JsonObject>


    @POST("")
    fun updateProductImage(@Url url:String,@Body body: JSONObject): Call<JsonObject>

    @FormUrlEncoded
    @POST("insales/remove_image.php")
    fun removeProductImage(@Field("email") email: String, @Field("password") password:String, @Field("shop_name") shopName:String, @Field("p_id") pId:Int, @Field("image_id") imageId:Int): Call<JsonObject>

    @FormUrlEncoded
    @POST("insales/update_product.php")
    fun updateProductDetail(@Field("email") email: String, @Field("password") password:String, @Field("shop_name") shopName:String, @Field("p_id") pId:Int, @Field("title") title:String, @Field("short_desc") shortDesc:String, @Field("full_desc") fullDesc:String): Call<JsonObject>

    @FormUrlEncoded
    @POST("insales/categories.php")
    fun categories(@Field("email") email: String,@Field("password") password:String,@Field("shop_name") shopName:String): Call<JsonObject>


}