package com.boris.expert.csvmagic.model

data class Feature(
    val id: Int,
    val name: String,
    val credit_price: Int,
    val duration: Int,
    val memory: Int
){
    constructor():this(0,"",0,0,0)
}