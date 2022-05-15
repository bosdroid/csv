package com.boris.expert.csvmagic.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.io.IOException
import net.expandable.ExpandableTextView

object TextRecogniser {
    private val TAG: String = TextRecogniser::class.java.name
    fun runTextRecognition(context: Context, container: TextInputEditText, imageUri: Uri) {
        var inputImage: InputImage? = null
        try {
            inputImage = InputImage.fromFilePath(context, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        val image = InputImage.fromBitmap(bitMap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        if (inputImage != null) {
            recognizer.process(inputImage)
                    .addOnSuccessListener { texts ->
                        processTextRecognitionResult(context, container, texts)
                    }
                    .addOnFailureListener { e -> // Task failed with an exception
                        e.printStackTrace()
                        Log.d(TAG, "runTextRecognition: ")
                    }
        } else {
            Toast.makeText(context, "Please select appropriate image.", Toast.LENGTH_LONG).show()
        }
    }

    fun runTextRecognition(context: Context, container: ExpandableTextView, imageUri: Uri,listener:ResponseListener) {
        var inputImage: InputImage? = null
        try {
            inputImage = InputImage.fromFilePath(context, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        val image = InputImage.fromBitmap(bitMap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        if (inputImage != null) {
            recognizer.process(inputImage)
                .addOnSuccessListener { texts ->
                    processTextRecognitionResult(context, container, texts,listener)
                }
                .addOnFailureListener { e -> // Task failed with an exception
                    e.printStackTrace()
                    Log.d(TAG, "runTextRecognition: ")
                }
        } else {
            Toast.makeText(context, "Please select appropriate image.", Toast.LENGTH_LONG).show()
        }
    }

    private fun processTextRecognitionResult(context: Context, container: TextInputEditText, texts: Text) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(context, "No text found", Toast.LENGTH_LONG).show()
            return
        }
        val stringBuilder = StringBuilder()
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val str = lines[j].text
                stringBuilder.append(str)
                if (j != lines.size-1){
                    stringBuilder.append("\n")
                }
            }
        }
//        container.setText(stringBuilder)
//        container.setSelection(container.text.toString().length)
        val stringBuilder1 = StringBuilder()
        stringBuilder1.append(container.text.toString().trim())
        stringBuilder1.append(" $stringBuilder")
        container.setText(stringBuilder1.toString())
    }

    private fun processTextRecognitionResult(context: Context, container: ExpandableTextView, texts: Text,listener: ResponseListener) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(context, "No text found", Toast.LENGTH_LONG).show()
            return
        }
        val stringBuilder = StringBuilder()
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val str = lines[j].text
                stringBuilder.append(str)
                if (j != lines.size-1){
                    stringBuilder.append("\n")
                }
            }
        }
        container.isExpanded = true
        val stringBuilder1 = StringBuilder()
        stringBuilder1.append(container.text.toString())
        stringBuilder1.append(" $stringBuilder")
        container.text = stringBuilder1
        listener.onSuccess(stringBuilder1.toString())
    }
}