package com.boris.expert.csvmagic.model

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(
    @SerializedName("feedbacks")
    val feedbacks: ArrayList<Feedback>
)