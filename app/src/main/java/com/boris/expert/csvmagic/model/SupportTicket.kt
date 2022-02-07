package com.boris.expert.csvmagic.model

import java.io.Serializable

data class SupportTicket(
    val id:String,
    val appName:String,
    val userName:String,
    val title:String,
    val message:String,
    val timeStamp:Long,
    val status:String,
    val lastReply:Long,
    val lastReplyBy:String
):Serializable{

    constructor():this("","","","","",0,"",0,"")

}