package com.boris.expert.csvmagic.model

import java.io.Serializable

data class Category(
    val title: String,
    val id: Int
) : Serializable {
    constructor() : this("", 0)

    override fun toString(): String {
        return title
    }
}