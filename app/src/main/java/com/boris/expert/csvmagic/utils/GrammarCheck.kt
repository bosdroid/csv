package com.boris.expert.csvmagic.utils

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.boris.expert.csvmagic.interfaces.GrammarCallback
import com.google.android.material.textview.MaterialTextView
import net.expandable.ExpandableTextView
import org.json.JSONObject


object GrammarCheck {
    var grammarErrors = false
    fun check(context: Context, text: String, output: ExpandableTextView, type:Int, grammarStatusView:MaterialTextView, listener:GrammarCallback){
        //BaseActivity.startLoading(context)
        val FirstPart = "https://api.textgears.com/grammar?key=vJTW1KUmyAxAEzAy&text="
        //val Last_Part = "&language=ru-RU"
        val url = "$FirstPart${text.trim()}"//FirstPart + text + Last_Part
         if (type == 1){
             grammarErrors = false
         }
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject?) {
                    //BaseActivity.dismiss()
//                    try {
                        val error: MutableList<String> = ArrayList()
                        val mp: MutableMap<Int, String> = HashMap()
                        val respon = response!!.getJSONObject("response")
                        val errors = respon.getJSONArray("errors")

                        if (errors.length() == 0) {
                            output.setText(text)
                            listener.onSuccess(null,grammarErrors)
                            return
                        }
                        for (i in 0 until errors.length()) {
                            val SingleError = errors[i] as JSONObject
                            val s = SingleError.getString("bad")
                            val x = SingleError.getInt("offset")
                            mp[x] = s
                            error.add(s)

                        }
                        val builder = SpannableStringBuilder()
                        val list: MutableList<Int> = ArrayList()
                        val set: Set<Map.Entry<Int,String>> = mp.entries //Converting to Set so that we can traverse
                        val itr = set.iterator()
                        while (itr.hasNext()) {
                            //Converting to Map.Entry so that we can get key and value separately
                            val entry = itr.next() as Map.Entry<*, *>
                            val x = entry.key as Int
                            val s = entry.value as String
                            for (i in x downTo 0) {
                                if (text.substring(i, i + s.length) == s) {
                                    list.add(i)
                                    break
                                }
                            }
                        }

                        var i = 0
                        while (i < text.length) {
                            if (list.size > 0 && i == list[0]) {
                                grammarErrors = true
                                val s = error[0]
                                val span = SpannableString(s)
                                span.setSpan(ForegroundColorSpan(Color.RED), 0, s.length, 0)
                                builder.append(span)
                                list.removeAt(0)
                                error.removeAt(0)
                                i += s.length
                                continue
                            }

                            val temp = text.substring(i, i + 1)
                            val span = SpannableString(temp)
                            span.setSpan(ForegroundColorSpan(Color.RED), 0, 0, 0)
                            builder.append(span)
                            i++
                        }

                         output.setText(builder)
                        listener.onSuccess(builder,grammarErrors)

//                    } catch (e: Exception) {
//                        listener.onSuccess("")
//                    }
                }

            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    //BaseActivity.dismiss()
                    listener.onSuccess(null,false)
                }

            })

        VolleySingleton.getInstance(context)!!.addToRequestQueue(request)
    }

}