package com.boris.expert.csvmagic.model

import java.io.Serializable

data class Product (
      val id:Int,
      val categoryId:Int,
      val title:String,
      val shortDesc:String,
      val fullDesc:String,
      val productImages:ArrayList<ProductImages>?
        ):Serializable{
            constructor():this(0,0,"","","",null)
        }