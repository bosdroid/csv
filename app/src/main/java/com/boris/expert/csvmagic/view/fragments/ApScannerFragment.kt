package com.boris.expert.csvmagic.view.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.RuntimePermissionHelper
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ApScannerFragment : Fragment() {

    private lateinit var appSettings: AppSettings
    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_ap_scanner, container, false)
        initViews(v)
        return v
    }

    private fun initViews(view: View) {
        scannerView = view.findViewById(R.id.ap_scanner_view)
    }


    override fun onResume() {
        startScanner()
        super.onResume()
    }

    private fun startScanner() {
        if (RuntimePermissionHelper.checkCameraPermission(
                requireActivity(),
                Constants.CAMERA_PERMISSION
            )
        ) {

            if (codeScanner == null) {
                codeScanner = CodeScanner(requireActivity(), scannerView)
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

                    requireActivity().runOnUiThread {

                        if (it.text.isNotEmpty() && it.text.matches(Regex("[0-9]+"))) {
                            appSettings.putString("AP_BARCODE_ID",it.text)
                            Toast.makeText(requireActivity(),"Barcode ID has been saved!-> ${it.text}",Toast.LENGTH_SHORT).show()
                        } else {
                            BaseActivity.showAlert(
                                requireActivity(),
                                "${getString(R.string.barcode_data_format_error_text)}\nBarcode Data: ${it.text}"
                            )
                        }
                    }
                }
                errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                    if (RuntimePermissionHelper.checkCameraPermission(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                       // initMlScanner()
                    }
                    requireActivity().runOnUiThread {
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

        when(requestCode){
            Constants.CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanner()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                        RuntimePermissionHelper.checkStoragePermission(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    } else {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(requireActivity().resources.getString(R.string.camera_permission_failed_text))
                            .setCancelable(false)
                            .setPositiveButton(requireActivity().resources.getString(R.string.ok_text)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .create().show()
                    }
                }
            }
            else->{

            }
        }
    }
}