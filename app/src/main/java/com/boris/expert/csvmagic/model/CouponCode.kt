package com.boris.expert.csvmagic.model

import java.io.Serializable

data class CouponCode(
    val name: String,
    val code: String,
    val credits:Int,
    val expired: String
) : Serializable {
     var isUsed:Int = 0 // 1 is used
    var user_id:String = ""

    constructor() : this("", "", 0,"")

}