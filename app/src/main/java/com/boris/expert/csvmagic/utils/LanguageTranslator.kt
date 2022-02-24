package com.boris.expert.csvmagic.utils

import com.boris.expert.csvmagic.interfaces.TranslationCallback
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions


object LanguageTranslator {

    private fun getTranslatorFromEngToRus(): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        return Translation.getClient(options)
    }

    private fun getTranslatorFromRusToEng(): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.RUSSIAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        return Translation.getClient(options)
    }

    fun translateText(text: String, type: String, listener: TranslationCallback) {
        val firebaseTranslator = if (type == "en") {
            getTranslatorFromEngToRus()
        } else {
            getTranslatorFromRusToEng()
        }

        downloadModal(firebaseTranslator, text, listener)

    }

    private fun downloadModal(
        firebaseTranslator: Translator,
        text: String,
        listener: TranslationCallback
    ) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        firebaseTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener(OnSuccessListener<Void?> {

                firebaseTranslator.translate(text)
                    .addOnSuccessListener(OnSuccessListener<String?> { s ->
                        listener.onTextTranslation(s)
                    }).addOnFailureListener(
                        OnFailureListener {
                            BaseActivity.dismiss()
                            listener.onTextTranslation("")
                        })
            }).addOnFailureListener(OnFailureListener {
                BaseActivity.dismiss()
                listener.onTextTranslation("")
            })
    }

}