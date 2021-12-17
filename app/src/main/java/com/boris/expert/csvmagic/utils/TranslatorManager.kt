package com.boris.expert.csvmagic.utils

import com.boris.expert.csvmagic.interfaces.TranslationCallback
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslatorManager {

    fun translate(text: String, listener: TranslationCallback) {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.RUSSIAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()

        val translator = Translation.getClient(options)

        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                listener.onTextTranslation(translatedText)
            }
            .addOnFailureListener { exception ->
                listener.onTextTranslation("")
            }
    }

}