package com.boris.expert.csvmagic.model

data class Feature(
    val id: Int,
    val name: String,
    val credit_price: Int,
    var duration: Int,
    var memory: Int,
    var createdAt:Long,
    var expiredAt:Long
){
    constructor():this(0,"",0,0,0,0,0)
}