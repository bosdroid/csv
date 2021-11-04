package com.boris.expert.csvmagic.interfaces

import com.android.volley.VolleyError
import org.json.JSONObject

interface APICallback {
    fun onSuccess(response:JSONObject)
    fun onError(error:VolleyError)
}