package com.boris.expert.csvmagic.model

data class Feature(
    val id: Int,
    val packageId: Int,
    val credit_price: Int,
    var duration: Int,
    var memory: Float,
    var createdAt:Long,
    var expiredAt:Long
){
    constructor():this(0,0,0,0,0F,0,0)
}