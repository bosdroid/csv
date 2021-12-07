package com.boris.expert.csvmagic.model

import java.io.Serializable

data class HelpObject(
    val type: String,
    val link: String,
    val thumbnail:String
) : Serializable {
    constructor() : this("", "","")
}