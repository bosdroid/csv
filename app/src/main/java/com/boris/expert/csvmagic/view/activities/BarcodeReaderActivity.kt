package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.RuntimePermissionHelper
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BarcodeReaderActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_reader)

        initViews()
        setUpToolbar()

    }

    override fun onResume() {
        super.onResume()
        startScanner()
    }

    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        scannerView = findViewById(R.id.barcode_scanner_view)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.barcode_reader_text)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun startScanner() {
        if (RuntimePermissionHelper.checkCameraPermission(
                context,
                Constants.CAMERA_PERMISSION
            )
        ) {

            if (codeScanner == null) {
                codeScanner = CodeScanner(context, scannerView)
            }
//            if (!isFileSelected){
//                codeScanner!!.startPreview()
//            }
            // Parameters (default values)

            codeScanner!!.apply {
                camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
                formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
                // ex. listOf(BarcodeFormat.QR_CODE)
                autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
                scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
                isAutoFocusEnabled = true // Whether to enable auto focus or not
                isFlashEnabled = false // Whether to enable flash or not

                // Callbacks
                decodeCallback = DecodeCallback {

                    runOnUiThread {

                        if (it.text.isNotEmpty() && it.text.matches(Regex("[0-9]+"))) {
                            setResult(RESULT_OK, Intent().apply {
                                putExtra("SCANNED_BARCODE_VALUE", it.text)
                            })
                            finish()
                        } else {
                            showAlert(
                                context,
                                "${getString(R.string.barcode_data_format_error_text)}\nBarcode Data: ${it.text}"
                            )
                        }
                    }
                }
                errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                    if (RuntimePermissionHelper.checkCameraPermission(
                            context,
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                        // initMlScanner()
                    }
                    runOnUiThread {
//                        Toast.makeText(
//                            requireContext(), "Camera initialization error: ${it.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }

                scannerView.setOnClickListener {
                    startPreview()
                }
                startPreview()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanner()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                        RuntimePermissionHelper.checkStoragePermission(
                            context,
                            Constants.CAMERA_PERMISSION
                        )
                    } else {
                        MaterialAlertDialogBuilder(context)
                            .setMessage(resources.getString(R.string.camera_permission_failed_text))
                            .setCancelable(false)
                            .setPositiveButton(resources.getString(R.string.ok_text)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .create().show()
                    }
                }
            }
            else -> {

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}