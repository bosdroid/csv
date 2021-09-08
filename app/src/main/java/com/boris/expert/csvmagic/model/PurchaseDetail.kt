package com.boris.expert.csvmagic.model

import java.io.Serializable
import java.util.*

data class PurchaseDetail(
    val userId: String,
    val packageName:String,
    val orderId: String,
    val productId: String,
    val purchaseTime: Long,
    val purchaseToken: String
):Serializable{
    constructor():this("","","","",0,"")
}