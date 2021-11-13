package com.boris.expert.csvmagic.model

import java.io.Serializable

data class CouponCode(
    val name: String,
    val code: String,
    val credits:Int,
    val expired: String
) : Serializable {


    constructor() : this("", "", 0,"")

}