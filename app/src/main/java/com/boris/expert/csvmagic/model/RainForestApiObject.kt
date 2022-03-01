package com.boris.expert.csvmagic.model

import java.io.Serializable

data class RainForestApiObject(
    val asin:String,
    val image: String,
    var title: String,
) : Serializable {
    constructor() : this("","", "")
}