package com.boris.expert.csvmagic.view.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.boris.expert.csvmagic.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.lucem.anb.characterscanner.Scanner
import com.lucem.anb.characterscanner.ScannerListener

class OcrActivity : BaseActivity() {

    private lateinit var scanner: Scanner
    private lateinit var surfaceView: SurfaceView
    private lateinit var textCaptureButton:MaterialButton
    private var extractText = ""
    private var capturedBitmap:Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)

//        dispatchTakePictureIntent()

        surfaceView = findViewById(R.id.surface)
        textCaptureButton = findViewById(R.id.text_capture_button)
        scanner = Scanner(this, surfaceView, object : ScannerListener {
            override fun onDetected(detections: String) {
                extractText = detections
            }

            override fun onStateChanged(state: String, p1: Int) {
                Log.d("state", state);
            }

        })

        textCaptureButton.setOnClickListener {
             scanner.isScanning = false
            val intent = Intent()
            intent.putExtra("SCAN_TEXT", extractText)
            setResult(RESULT_OK, intent)
            finish()
        }


    }

    private fun dispatchTakePictureIntent(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(takePictureIntent)
    }

    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->


//                // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                capturedBitmap = data!!.extras!!.get("data") as Bitmap
                detectText()
//                val file = ImageManager.readWriteImage(context,bitmap)
//                cropImage(Uri.fromFile(file))
            }
        }

    private fun detectText(){
        if (capturedBitmap != null){
            startLoading(this)
            val image = FirebaseVisionImage.fromBitmap(capturedBitmap!!)
            val detector = FirebaseVision.getInstance().cloudTextRecognizer
            val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(listOf("en", "ru"))
                .build()
            detector.processImage(image)
                .addOnSuccessListener { result ->
                    dismiss()
                    // Task completed successfully
                    val resultText = result.text
                    val intent = Intent()
                    intent.putExtra("SCAN_TEXT", resultText)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    dismiss()
                    showAlert(this,e.localizedMessage)
                }

        }
    }
}