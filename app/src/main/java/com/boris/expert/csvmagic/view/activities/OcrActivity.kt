package com.boris.expert.csvmagic.view.activities

import android.content.Intent
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import com.boris.expert.csvmagic.R
import com.google.android.material.button.MaterialButton
import com.lucem.anb.characterscanner.Scanner
import com.lucem.anb.characterscanner.ScannerListener

class OcrActivity : BaseActivity() {

    private lateinit var scanner: Scanner
    private lateinit var surfaceView: SurfaceView
    private lateinit var textCaptureButton:MaterialButton
    private var extractText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)


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
}