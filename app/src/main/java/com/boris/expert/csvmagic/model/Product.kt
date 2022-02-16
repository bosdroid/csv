package com.boris.expert.csvmagic.model

import java.io.Serializable

data class Product (
      val id:Int,
      val title:String,
      val productImages:ArrayList<ProductImages>?
        ):Serializable{
            constructor():this(0,"",null)
        }