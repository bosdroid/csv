package com.boris.expert.csvmagic.model

import java.io.Serializable

data class ProductImages(
        val id: Int,
        val productId: Int,
        var imageUrl: String,
        val position:Int
) : Serializable {
    constructor() : this(0, 0, "",0)
}