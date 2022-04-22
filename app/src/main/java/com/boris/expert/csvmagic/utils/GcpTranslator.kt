package com.boris.expert.csvmagic.utils

import android.content.Context
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.interfaces.TranslationCallback
import com.google.cloud.translate.Detection
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object GcpTranslator {

    fun translateFromEngToRus(context: Context, text: String, listener: TranslationCallback){
        listener.onTextTranslation(text)

        System.setProperty("GOOGLE_API_KEY",context.resources.getString(R.string.translation_api_key))
        CoroutineScope(Dispatchers.IO).launch {
            val translate = TranslateOptions.getDefaultInstance().service
            val detection: Detection = translate.detect(text)
            val detectedLanguage = detection.language
            if (detectedLanguage == "ru"){
                CoroutineScope(Dispatchers.Main).launch {
                    listener.onTextTranslation(text)
                }

            }
            else{
                val translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(detectedLanguage),
                    Translate.TranslateOption.targetLanguage("ru"))
                CoroutineScope(Dispatchers.Main).launch {
                    listener.onTextTranslation(translation.translatedText)
                }

            }
        }
    }

    fun translateFromRusToEng(context: Context, text: String, listener: TranslationCallback){
        listener.onTextTranslation(text)
        System.setProperty("GOOGLE_API_KEY",context.resources.getString(R.string.translation_api_key))
        val translate = TranslateOptions.getDefaultInstance().service
        val detection: Detection = translate.detect(text)
        val detectedLanguage = detection.language
        if (detectedLanguage == "en"){
            listener.onTextTranslation(text)
        }
        else{
            val translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage(detectedLanguage),
                Translate.TranslateOption.targetLanguage("en"))
            listener.onTextTranslation(translation.translatedText)
        }

    }

}