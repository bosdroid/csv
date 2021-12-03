package com.boris.expert.csvmagic.model

import java.io.Serializable

data class HelpObject(
    val type: String,
    val link: String
) : Serializable {
    constructor() : this("", "")
}