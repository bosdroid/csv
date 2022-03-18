package com.boris.expert.csvmagic.model

import java.io.Serializable

data class KeywordObject (
    val keyword:String,
    val quantity:Int
):Serializable{
constructor():this("",0)
}