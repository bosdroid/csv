package com.boris.expert.csvmagic.model

import java.io.Serializable

data class Package(
    val created_datetime: String,
    val duration: Int,
    val end_date: String,
    val id: Int,
    val size: Int,
    val start_date: String,
    val user_id: String
):Serializable{
    constructor():this("",0,"",0,0,"","")
}