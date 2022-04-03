package com.boris.expert.csvmagic.utils

import android.content.Context
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.interfaces.TranslationCallback
import com.google.cloud.translate.Detection
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions


object GcpTranslator {

    fun translateFromEngToRus(context: Context, text: String, listener: TranslationCallback){
        listener.onTextTranslation(text)
        System.setProperty("GOOGLE_API_KEY",context.resources.getString(R.string.translation_api_key))
        val translate = TranslateOptions.getDefaultInstance().service
        val detection: Detection = translate.detect(text)
        val detectedLanguage = detection.language
        if (detectedLanguage == "ru"){
            listener.onTextTranslation(text)
        }
        else{
            val translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage(detectedLanguage),
                Translate.TranslateOption.targetLanguage("ru"))
            listener.onTextTranslation(translation.translatedText)
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