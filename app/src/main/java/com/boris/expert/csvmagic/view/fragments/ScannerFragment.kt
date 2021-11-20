package com.boris.expert.csvmagic.view.fragments

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.BarcodeImageAdapter
import com.boris.expert.csvmagic.customviews.CustomTextInputEditText
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.LoginCallback
import com.boris.expert.csvmagic.interfaces.UploadImageCallback
import com.boris.expert.csvmagic.model.CodeHistory
import com.boris.expert.csvmagic.model.Sheet
import com.boris.expert.csvmagic.model.TableObject
import com.boris.expert.csvmagic.room.AppViewModel
import com.boris.expert.csvmagic.singleton.DriveService
import com.boris.expert.csvmagic.singleton.SheetService
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.activities.*
import com.boris.expert.csvmagic.view.activities.BaseActivity.Companion.rateUs
import com.budiyev.android.codescanner.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.Result
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ScannerFragment : Fragment(), CustomAlertDialog.CustomDialogListener,
    View.OnFocusChangeListener {

    private var codeDataTInputView: CustomTextInputEditText? = null
    private var columns = mutableListOf<String>()
    private var availableStorageMemory: Float = 0F
    private var numRows: Int = 0
    private var customAlertDialog: CustomAlertDialog? = null
    private var updateInputBox: CustomTextInputEditText? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var arrayList = mutableListOf<String>()
    private var filePathView: MaterialTextView? = null
    private var barcodeImagesRecyclerView: RecyclerView? = null
    private var barcodeImageList = mutableListOf<String>()
    private lateinit var adapter: BarcodeImageAdapter
    var currentPhotoPath: String? = null
    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView
    private lateinit var appViewModel: AppViewModel
    private lateinit var tableGenerator: TableGenerator
    private lateinit var tipsSwitchBtn: SwitchMaterial
    private var tableName: String = ""
    private lateinit var tablesSpinner: AppCompatSpinner
    private lateinit var sheetsSpinner: AppCompatSpinner
    private lateinit var modesSpinner: AppCompatSpinner
    private var textInputIdsList = mutableListOf<Pair<String, CustomTextInputEditText>>()
    private var spinnerIdsList = mutableListOf<Pair<String, AppCompatSpinner>>()
    private lateinit var addNewTableBtn: MaterialButton
    private lateinit var appSettings: AppSettings
    private var imageDrivePath = ""
    private var isFileSelected = false
    private var listener: ScannerInterface? = null
    private var cameraProviderFuture: ListenableFuture<*>? = null
    private var cameraExecutor: ExecutorService? = null
    private var mContext: AppCompatActivity? = null
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var textRecognitionButtonsLayout: LinearLayout? = null

    //private var previewView: PreviewView? = null
    private var imageAnalyzer: MyImageAnalyzer? = null
    private var isFlashOn = false
    private lateinit var cam: Camera
    private lateinit var container: FrameLayout
    private lateinit var previewView: PreviewView
    private lateinit var flashImg: ImageView
    private val TAG = ScannerFragment::class.java.name
    private var sheetsList = mutableListOf<Sheet>()
    private var userRecoverableAuthType = 0
    private var selectedSheetId: String = ""
    private var selectedSheetName: String = ""
    private var scannedText: String = ""
    private lateinit var connectGoogleSheetsTextView: MaterialTextView
    private lateinit var sheetsTopLayout: LinearLayout
    var values: List<Any>? = null
    var isScanResultDialogShowing = false

    interface ScannerInterface {
        fun login(callback: LoginCallback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as AppCompatActivity
        listener = context as ScannerInterface
        appSettings = AppSettings(requireActivity())
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(AppViewModel::class.java)
        tableGenerator = TableGenerator(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_scanner, container, false)

        initViews(v)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefDate = DialogPrefs.getDate(requireContext())
        if (prefDate == null) {
            DialogPrefs.setDate(requireContext(), DateUtils.getCurrentDate())
        }
        val scans = DialogPrefs.getSuccessScan(requireContext())
        val isSharedQr = DialogPrefs.getShared(requireContext())
        if ((getDateDifference() >= 3 && scans >= 2) || (getDateDifference() >= 3 && isSharedQr)) {
            mContext.let {
                if (it != null) {
                    rateUs(it)
                }
            }
        }
    }

    private fun getDateDifference(): Int {
        var days = 0
        val myFormat = SimpleDateFormat(DateUtils.DATE_FORMAT)
        val currentDate = DateUtils.getCurrentDate()
//        val currentDate = "2021-07-19"
        val prefsDate = DialogPrefs.getDate(requireContext())
        val dateCurrent = myFormat.parse(currentDate)
        if (prefsDate != null) {
            val datePrefs = myFormat.parse(prefsDate)
            val timeCurrent = dateCurrent?.time
            val timePrefs = datePrefs?.time
            if (timeCurrent != null && timePrefs != null) {
                val difference = timeCurrent - timePrefs
                days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
            }
            Log.d(TAG, "getDateDifference: $days, $currentDate, $datePrefs")
        }
        return days
    }

    private fun initViews(view: View) {
        scannerView = view.findViewById(R.id.scanner_view)
        tablesSpinner = view.findViewById(R.id.tables_spinner)
        sheetsSpinner = view.findViewById(R.id.sheets_spinner)
        modesSpinner = view.findViewById(R.id.modes_spinner)
        addNewTableBtn = view.findViewById(R.id.add_new_table_btn)
        container = view.findViewById(R.id.container)
        previewView = view.findViewById(R.id.previewview)
        tipsSwitchBtn = view.findViewById(R.id.home_tips_switch)
        connectGoogleSheetsTextView = view.findViewById(R.id.connect_google_sheets_text_view)
        sheetsTopLayout = view.findViewById(R.id.sheets_top_layout)
        flashImg = view.findViewById(R.id.flashImg)
        addNewTableBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), TablesActivity::class.java))
        }

        connectGoogleSheetsTextView.setOnClickListener {
            listener!!.login(object : LoginCallback {
                override fun onSuccess() {
                    Log.d("TEST199", "success")
                    onResume()
                }

            })
        }

    }


    private fun getModeList() {
        val modeList = requireActivity().resources.getStringArray(R.array.mode_list)
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            modeList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modesSpinner.adapter = adapter

        if (appSettings.getString(requireActivity().getString(R.string.key_mode))!!.isNotEmpty()) {
            var isFound = false
            for (i in modeList.indices) {
                if ("$i" == appSettings.getString(requireActivity().getString(R.string.key_mode))) {
                    modesSpinner.setSelection(i)
                    appSettings.putString(requireActivity().getString(R.string.key_mode), "$i")
                    isFound = true
                    break
                } else {
                    isFound = false
                }
            }

            if (!isFound) {
                appSettings.putString(requireActivity().getString(R.string.key_mode), "0")
            }
        }

        modesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                //val mode = adapterView!!.getItemAtPosition(i).toString()
                appSettings.putString(requireActivity().getString(R.string.key_mode), "$i")
            }
        }

    }

    private fun getTableList() {
        val tablesList = mutableListOf<String>()
        tablesList.addAll(tableGenerator.getAllDatabaseTables())
        if (tablesList.isNotEmpty()) {
            tableName = tablesList[0]
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                tablesList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tablesSpinner.adapter = adapter

            if (appSettings.getString("SCAN_SELECTED_TABLE")!!.isNotEmpty()) {
                for (i in 0 until tablesList.size) {
                    val value = tablesList[i]
                    if (value == appSettings.getString("SCAN_SELECTED_TABLE")) {
                        tablesSpinner.setSelection(i)
                        tableName = value
                        break
                    }
                }
            }
        }

        tablesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                tableName = adapterView!!.getItemAtPosition(i).toString()
                appSettings.putString("SCAN_SELECTED_TABLE", tableName)
            }
        }
    }

    private fun initMlScanner() {
        //requireActivity().window.setFlags(1024, 1024)
        container.visibility = View.VISIBLE
        scannerView.visibility = View.GONE
        flashImg.setOnClickListener { view: View? ->
            if (cam != null) {
                Log.d("TAG", "initMlScanner: ")
                if (cam.cameraInfo.hasFlashUnit()) {
                    if (isFlashOn) {
                        isFlashOn = false
                        flashImg.setImageResource(R.drawable.ic_flash_off)
                        cam.cameraControl.enableTorch(isFlashOn)
                    } else {

                        isFlashOn = true
                        flashImg.setImageResource(R.drawable.ic_flash_on)
                        cam.cameraControl.enableTorch(isFlashOn)
                    }
                }
            }
        }
        imageAnalyzer = MyImageAnalyzer(requireActivity().supportFragmentManager)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        (cameraProviderFuture as ListenableFuture<ProcessCameraProvider>).addListener(Runnable {
            try {
                val processCameraProvider =
                    (cameraProviderFuture as ListenableFuture<ProcessCameraProvider>).get() as ProcessCameraProvider
                bindPreview(processCameraProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    var url = " "
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
                        if (isScanResultDialogShowing) {
                            return@runOnUiThread
                        }
                        if (it.text.isNotEmpty() && it.text.matches(Regex("[0-9]+"))) {

                            val isFound = tableGenerator.searchItem(tableName, it.text)
                            if (isFound && appSettings.getString(getString(R.string.key_mode)) == "0") {
                                val quantity = tableGenerator.getScanQuantity(tableName, it.text)
                                var qty = 0
                                qty = if (quantity != null) {
                                    quantity.toInt() + 1
                                } else {
                                    1
                                }

                                val isUpdate =
                                    tableGenerator.updateScanQuantity(tableName, it.text, qty)
                                if (isUpdate) {
                                    //showAlert(requireActivity(),requireActivity().getString(R.string.scan_quantity_increase_success_text))
                                    Toast.makeText(
                                        requireActivity(),
                                        requireActivity().getString(R.string.scan_quantity_increase_success_text),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Handler(Looper.myLooper()!!).postDelayed({
                                        startPreview()
                                    }, 2000)

                                }
                            } else {
                                displayDataSubmitDialog(it, "")
                            }
                        } else {
                            showAlert(
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
                        initMlScanner()
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

    private lateinit var alert: AlertDialog
    private lateinit var addImageCheckBox: MaterialCheckBox
    private lateinit var tableDetailLayoutWrapper: LinearLayout
    private lateinit var barcodeDetailParentLayout: LinearLayout
    private fun displayDataSubmitDialog(it: Result?, scanText: String) {
        var text = ""
        text = if (it == null) {
            scanText
        } else {
            it.text
        }
        playSound(true)
        generateVibrate()

        if (appSettings.getString(getString(R.string.key_mode)) == "1") {
            val isFound = tableGenerator.searchItem(tableName, text)
            if (isFound) {
                val quantity = tableGenerator.getScanQuantity(tableName, text)
                var qty = 0
                if (quantity != null) {
                    qty = quantity.toInt() + 1

                    if (qty != -1) {
                        if (qty > 0) {
                            qty -= 1
                            val isUpdate = tableGenerator.updateScanQuantity(tableName, text, qty)
                            if (isUpdate) {
                                Toast.makeText(
                                    requireActivity(),
                                    "${getString(R.string.scan_quantity_update_success_text)} ${
                                        getString(
                                            R.string.scan_quantity_remaining_text
                                        )
                                    } $qty",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Handler(Looper.myLooper()!!).postDelayed({
                                    codeScanner!!.startPreview()
                                }, 2000)
                            }
                        } else {
                            val isSuccess = tableGenerator.deleteItem(tableName, text)
                            if (isSuccess) {
                                Toast.makeText(
                                    requireActivity(),
                                    requireActivity().getString(R.string.scan_item_delete_success_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Handler(Looper.myLooper()!!).postDelayed({
                                    codeScanner!!.startPreview()
                                }, 2000)

                            }
                        }
                    }
                }
            } else {
                showAlert(
                    requireActivity(),
                    getString(R.string.scan_item_not_found_text)
                )
            }
        } else if (appSettings.getString(getString(R.string.key_mode)) == "2") {
            val searchTableObject = tableGenerator.getScanItem(tableName, text)

            if (searchTableObject != null) {
                renderQuickLinksDialog(searchTableObject)

            } else {
                displayItemNotFoundDialog(text)
            }

        } else {
            copyToClipBoard(text)
            if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(it!!.barcodeFormat) || scanText.isNotEmpty()) {

                if (tableName.isEmpty()) {
                    showAlert(requireActivity(), text)
                } else {

//                    requireActivity().startActivity(
//                        Intent(
//                            requireActivity(),
//                            ScanDataActivity::class.java
//                        ).apply {
//                            putExtra("TABLE_NAME", tableName)
//                            putExtra("SCAN_TEXT", text)
//                            putExtra("SHEET_NAME", selectedSheetName)
//                            putExtra("SHEET_ID", selectedSheetId)
//                        })


                    val scanResultLayout = LayoutInflater.from(requireActivity())
                        .inflate(R.layout.scan_result_dialog, null)
                    codeDataTInputView =
                        scanResultLayout.findViewById<CustomTextInputEditText>(R.id.scan_result_dialog_code_data)
                    tableDetailLayoutWrapper =
                        scanResultLayout.findViewById<LinearLayout>(R.id.table_detail_layout_wrapper)
                    val submitBtn =
                        scanResultLayout.findViewById<MaterialButton>(R.id.scan_result_dialog_submit_btn)
                    val scanResultCancelBtn =
                        scanResultLayout.findViewById<MaterialButton>(R.id.scan_result_dialog_cancel_btn)
                    val scanResultAddFieldBtn =
                        scanResultLayout.findViewById<MaterialButton>(R.id.scan_result_dialog_add_field_btn)
                    addImageCheckBox =
                        scanResultLayout.findViewById<MaterialCheckBox>(R.id.add_image_checkbox)
                    val severalImagesHintView =
                        scanResultLayout.findViewById<MaterialTextView>(R.id.several_images_hint_view)
                    val imageSourcesWrapperLayout =
                        scanResultLayout.findViewById<LinearLayout>(R.id.image_sources_layout)
                    filePathView =
                        scanResultLayout.findViewById<MaterialTextView>(R.id.filePath)
                    barcodeImagesRecyclerView =
                        scanResultLayout.findViewById(R.id.selected_barcode_images_recyclerview)
                    barcodeImagesRecyclerView!!.layoutManager = LinearLayoutManager(
                        requireActivity(), RecyclerView.HORIZONTAL,
                        false
                    )
                    barcodeImagesRecyclerView!!.hasFixedSize()
                    adapter = BarcodeImageAdapter(
                        requireContext(),
                        barcodeImageList as ArrayList<String>
                    )
                    barcodeImagesRecyclerView!!.adapter = adapter
                    adapter.setOnItemClickListener(object :
                        BarcodeImageAdapter.OnItemClickListener {
                        override fun onItemDeleteClick(position: Int) {
//                            val image = barcodeImageList[position]
                            val builder = MaterialAlertDialogBuilder(requireActivity())
                            builder.setMessage(getString(R.string.delete_barcode_image_message))
                            builder.setCancelable(false)
                            builder.setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                                dialog.dismiss()
                            }
                            builder.setPositiveButton(getString(R.string.yes_text)) { dialog, which ->
                                dialog.dismiss()
                                barcodeImageList.removeAt(position)
                                multiImagesList.removeAt(position)
                                filePathView!!.text = multiImagesList.joinToString(",")
                                adapter.notifyItemRemoved(position)
                            }
                            val alert = builder.create()
                            alert.show()

                        }

                        override fun onAddItemEditClick(position: Int) {

                        }

                        override fun onImageClick(position: Int) {

                        }

                    })
                    val imageRecognitionBtn =
                        scanResultLayout.findViewById<LinearLayout>(R.id.image_recognition_btn)
                    val photoRecognitionBtn =
                        scanResultLayout.findViewById<LinearLayout>(R.id.photo_recognition_btn)
                    textRecognitionButtonsLayout =
                        scanResultLayout.findViewById(R.id.text_recognition_buttons_layout)
                    val moreBtnView =
                        scanResultLayout.findViewById<AppCompatImageView>(R.id.more_btn)
                    moreBtnView.setOnClickListener {
                        openDialog()
                    }

                    scanResultAddFieldBtn.setOnClickListener {
                        requireActivity().startActivity(
                            Intent(
                                requireActivity(),
                                CreateTableActivity::class.java
                            ).apply {
                                putExtra("TABLE_NAME", tableName)
                                putExtra("FROM", "scan_dialog")
                            })
                    }

                    imageRecognitionBtn.setOnClickListener {
                        if (RuntimePermissionHelper.checkCameraPermission(
                                requireActivity(),
                                Constants.READ_STORAGE_PERMISSION
                            )
                        ) {
                            BaseActivity.hideSoftKeyboard(requireActivity(), imageRecognitionBtn)
                            pickImageFromGallery()
                        }
                    }

                    photoRecognitionBtn.setOnClickListener {
                        if (RuntimePermissionHelper.checkCameraPermission(
                                requireActivity(), Constants.CAMERA_PERMISSION
                            )
                        ) {
                            BaseActivity.hideSoftKeyboard(requireActivity(), photoRecognitionBtn)
                            pickImageFromCamera()
                        }


                    }

                    addImageCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            if (Constants.userData == null) {
                                addImageCheckBox.isChecked = false
                                MaterialAlertDialogBuilder(requireActivity())
                                    .setTitle(requireActivity().resources.getString(R.string.alert_text))
                                    .setMessage(requireActivity().resources.getString(R.string.login_error_text))
                                    .setNegativeButton(requireActivity().resources.getString(R.string.later_text)) { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    .setPositiveButton(requireActivity().resources.getString(R.string.login_text)) { dialog, which ->
                                        dialog.dismiss()
                                        listener!!.login(object : LoginCallback {
                                            override fun onSuccess() {
                                                Log.d("TEST199", "success")

                                                onResume()
                                            }

                                        })
                                    }
                                    .create().show()
                            } else {
                                severalImagesHintView.visibility = View.VISIBLE
                                imageSourcesWrapperLayout.visibility = View.VISIBLE
                                filePathView!!.visibility = View.GONE
                            }

                        } else {
                            severalImagesHintView.visibility = View.GONE
                            imageSourcesWrapperLayout.visibility = View.GONE
                            filePathView!!.visibility = View.GONE
                        }
                    }

                    val cameraImageView =
                        scanResultLayout.findViewById<AppCompatImageView>(R.id.camera_image_view)
                    val imagesImageView =
                        scanResultLayout.findViewById<AppCompatImageView>(R.id.images_image_view)

                    cameraImageView.setOnClickListener {
                        if (RuntimePermissionHelper.checkCameraPermission(
                                requireActivity(),
                                Constants.CAMERA_PERMISSION
                            )
                        ) {
                            //dispatchTakePictureIntent()
                            val cameraIntent =
                                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraResultLauncher.launch(cameraIntent)
                        }
                    }

                    imagesImageView.setOnClickListener {
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            getImageFromGallery()
                        } else {
                            requestPermissions(
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                Constants.READ_STORAGE_REQUEST_CODE
                            )
                        }
                    }
                    columns.addAll(tableGenerator.getTableColumns(tableName)!!.toList())
                    Log.d("TEST1999", columns.toString())
                    for (i in columns.indices) {
                        val value = columns[i]
                        if (value == "id" || value == "quantity") {
                            continue
                        } else if (value == "code_data") {
                            textInputIdsList.add(Pair(value, codeDataTInputView!!))
                            codeDataTInputView!!.setText(text)
                        } else {
                            val tableRowLayout =
                                LayoutInflater.from(requireContext())
                                    .inflate(
                                        R.layout.scan_result_table_row_layout,
                                        null
                                    )
                            val columnName =
                                tableRowLayout.findViewById<MaterialTextView>(R.id.table_column_name)
                            val columnValue =
                                tableRowLayout.findViewById<CustomTextInputEditText>(R.id.table_column_value)
                            val columnDropdown =
                                tableRowLayout.findViewById<AppCompatSpinner>(R.id.table_column_dropdown)
                            val columnDropDwonLayout =
                                tableRowLayout.findViewById<LinearLayout>(R.id.table_column_dropdown_layout)
                            columnName.text = value
                            val moreBtn =
                                tableRowLayout.findViewById<AppCompatImageView>(R.id.table_more_btn)
                            moreBtn.setOnClickListener {
                                openDialog()
                            }
                            val pair = tableGenerator.getFieldList(value, tableName)

                            if (pair != null) {
                                arrayList = mutableListOf()
                                if (!pair.first.contains(",") && pair.second == "listWithValues") {
                                    arrayList.add(pair.first)
                                    moreBtn.visibility = View.INVISIBLE
                                    columnValue.visibility = View.GONE
                                    columnDropDwonLayout.visibility = View.VISIBLE
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        arrayList
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    columnDropdown.adapter = adapter
                                    spinnerIdsList.add(Pair(value, columnDropdown))
                                } else if (pair.first.contains(",") && pair.second == "listWithValues") {

                                    arrayList.addAll(pair.first.split(","))
                                    moreBtn.visibility = View.INVISIBLE
                                    columnValue.visibility = View.GONE
                                    columnDropDwonLayout.visibility = View.VISIBLE
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        arrayList
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    columnDropdown.adapter = adapter
                                    spinnerIdsList.add(Pair(value, columnDropdown))
                                } else {
                                    moreBtn.visibility = View.VISIBLE
                                    columnDropDwonLayout.visibility = View.GONE
                                    columnValue.visibility = View.VISIBLE
                                    columnValue.setText(
                                        pair.first
                                    )
                                    columnValue.isEnabled = false
                                    columnValue.isFocusable = false
                                    columnValue.isFocusableInTouchMode = false
                                }

                            } else {
                                if (value == "image") {
                                    textInputIdsList.add(Pair(value, columnValue))
                                    continue
                                }
                                moreBtn.visibility = View.VISIBLE
                                columnDropDwonLayout.visibility = View.GONE
                                columnValue.visibility = View.VISIBLE

                                if (value == "date") {
                                    columnValue.setText(
                                        BaseActivity.getDateTimeFromTimeStamp(
                                            System.currentTimeMillis()
                                        )
                                    )
                                    moreBtn.visibility = View.INVISIBLE
                                    columnValue.isEnabled = false
                                    columnValue.isFocusable = false
                                    columnValue.isFocusableInTouchMode = false
                                } else {
                                    moreBtn.visibility = View.VISIBLE
                                    columnValue.isEnabled = true
                                    columnValue.isFocusable = true
//                                    columnValue.isFocusableInTouchMode = true
                                    columnValue.setText("")
                                }
                                textInputIdsList.add(Pair(value, columnValue))
                            }
                            tableDetailLayoutWrapper.addView(tableRowLayout)
                        }
                    }


                    for (i in 0 until textInputIdsList.size) {
                        textInputIdsList[i].second.onFocusChangeListener = this
                    }

                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setView(scanResultLayout)
                    builder.setCancelable(false)
                    alert = builder.create()
                    alert.show()
                    isScanResultDialogShowing = true
                    scanResultCancelBtn.setOnClickListener {
                        alert.dismiss()
                        columns.clear()
                        isScanResultDialogShowing = false
                        codeScanner!!.startPreview()
                    }

                    if (appSettings.getBoolean(getString(R.string.key_tips))) {
                        val duration = appSettings.getLong("tt2")
                        if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                                1
                            )
                        ) {
                            SimpleTooltip.Builder(requireActivity())
                                .anchorView(scanResultLayout)
                                .text(getString(R.string.after_scan_result_tip_text))
                                .gravity(Gravity.BOTTOM)
                                .animated(true)
                                .transparentOverlay(false)
                                .onDismissListener { tooltip ->
                                    tooltip.dismiss()
                                    appSettings.putLong("tt2", System.currentTimeMillis())
                                    openAddImageTooltip(addImageCheckBox, submitBtn)
                                }
                                .build()
                                .show()
                        }
                    }
                    submitBtn.setOnClickListener {
                        if (Constants.userData != null) {
                            availableStorageMemory =
                                if (appSettings.getString(Constants.memory)!!.isEmpty()) {
                                    0F
                                } else {
                                    Constants.convertMegaBytesToBytes(
                                        appSettings.getString(
                                            Constants.memory
                                        )!!.toFloat()
                                    )
                                }
                        }

//                        if(!checkBothListSame()){
//                            createNewSpreadsheetDialog(tableName)
//                        }
//                        else
                        if (totalImageSize.toInt() == 0) {
                            alert.dismiss()
                            saveDataIntoTable()
                        } else if (addImageCheckBox.isChecked && totalImageSize.toInt() == 0) {
                            alert.dismiss()
                            saveDataIntoTable()
                        } else if (addImageCheckBox.isChecked && totalImageSize <= availableStorageMemory) {
                            val expiredAt: Long = appSettings.getLong(Constants.expiredAt)
                            if (System.currentTimeMillis() > expiredAt && expiredAt.toInt() != 0) {
                                BaseActivity.showAlert(
                                    requireActivity(),
                                    getString(R.string.subscription_expired_text)
                                )
                            } else {
                                alert.dismiss()
                                saveDataIntoTable()
                            }

                        } else {
                            val b1 = MaterialAlertDialogBuilder(requireActivity())
                                .setCancelable(false)
                                .setTitle(requireActivity().resources.getString(R.string.alert_text))
                                .setMessage(requireActivity().resources.getString(R.string.storage_available_error_text))
                                .setNegativeButton(requireActivity().resources.getString(R.string.close_text)) { dialog, which ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(requireActivity().resources.getString(R.string.save_without_image_text)) { dialog, which ->
                                    dialog.dismiss()
                                    alert.dismiss()
                                    addImageCheckBox.isChecked = false
                                    saveDataIntoTable()
                                }
                                .setNeutralButton(getString(R.string.buy_storage_text)) { dialog, which ->
                                    dialog.dismiss()
                                    alert.dismiss()
                                    requireActivity().startActivity(
                                        Intent(
                                            requireActivity(),
                                            UserScreenActivity::class.java
                                        )
                                    )
                                }
                            val iAlert = b1.create()
                            iAlert.show()
                            iAlert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.purple_700
                                )
                            )

                        }
                    }

                }
            } else {
                val bundle = Bundle()
                bundle.putString("second scanner", "triggers")
                mFirebaseAnalytics?.logEvent("scanner", bundle)
                var qrHistory: CodeHistory? = null
                val type =
                    if (text.contains("http") || text.contains("https") || text.contains(
                            "www"
                        )
                    ) {
                        "link"
                    } else if (text.isDigitsOnly()) {
                        "number"
                    } else if (text.contains("VCARD") || text.contains("vcard")) {
                        "contact"
                    } else if (text.contains("WIFI:") || text.contains("wifi:")) {
                        "wifi"
                    } else if (text.contains("tel:")) {
                        "phone"
                    } else if (text.contains("smsto:") || text.contains("sms:")) {
                        "sms"
                    } else if (text.contains("instagram")) {
                        "instagram"
                    } else if (text.contains("whatsapp")) {
                        "whatsapp"
                    } else {
                        "text"
                    }
                if (text.isNotEmpty()) {

                    if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(it.barcodeFormat)) {
                        qrHistory = CodeHistory(
                            "qrmagicapp",
                            "${System.currentTimeMillis()}",
                            text,
                            "code",
                            "free",
                            "barcode",
                            "scan",
                            "",
                            "0",
                            "",
                            System.currentTimeMillis().toString(),
                            ""
                        )

                        appViewModel.insert(qrHistory)

                    } else {
                        qrHistory = CodeHistory(
                            "qrmagicapp",
                            "${System.currentTimeMillis()}",
                            text,
                            type,
                            "free",
                            "qr",
                            "scan",
                            "",
                            "0",
                            "",
                            System.currentTimeMillis().toString(),
                            ""
                        )
                        appViewModel.insert(qrHistory)
                    }
                    saveSuccessScans()
                    Toast.makeText(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.scan_data_save_success_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.myLooper()!!).postDelayed({
                        val intent = Intent(context, CodeDetailActivity::class.java)
                        intent.putExtra("HISTORY_ITEM", qrHistory)
                        requireActivity().startActivity(intent)
                    }, 2000)
                }
            }
        }
    }

    private fun renderTableColumnViews() {

        val updatedColumns = mutableListOf<String>()

        updatedColumns.addAll(tableGenerator.getTableColumns(tableName)!!)
        updatedColumns.removeAll(columns)

        for (i in updatedColumns.indices) {
            val value = updatedColumns[i]

            val tableRowLayout =
                LayoutInflater.from(requireContext())
                    .inflate(
                        R.layout.scan_result_table_row_layout,
                        null
                    )
            val columnName =
                tableRowLayout.findViewById<MaterialTextView>(R.id.table_column_name)
            val columnValue =
                tableRowLayout.findViewById<CustomTextInputEditText>(R.id.table_column_value)
            val columnDropdown =
                tableRowLayout.findViewById<AppCompatSpinner>(R.id.table_column_dropdown)
            val columnDropDwonLayout =
                tableRowLayout.findViewById<LinearLayout>(R.id.table_column_dropdown_layout)
            columnName.text = value
            val moreBtn =
                tableRowLayout.findViewById<AppCompatImageView>(R.id.table_more_btn)
            moreBtn.setOnClickListener {
                openDialog()
            }
            val pair = tableGenerator.getFieldList(value, tableName)

            if (pair != null) {
                arrayList = mutableListOf()
                if (!pair.first.contains(",") && pair.second == "listWithValues") {
                    arrayList.add(pair.first)
                    moreBtn.visibility = View.INVISIBLE
                    columnValue.visibility = View.GONE
                    columnDropDwonLayout.visibility = View.VISIBLE
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        arrayList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    columnDropdown.adapter = adapter
                    spinnerIdsList.add(Pair(value, columnDropdown))
                } else if (pair.first.contains(",") && pair.second == "listWithValues") {

                    arrayList.addAll(pair.first.split(","))
                    moreBtn.visibility = View.INVISIBLE
                    columnValue.visibility = View.GONE
                    columnDropDwonLayout.visibility = View.VISIBLE
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        arrayList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    columnDropdown.adapter = adapter
                    spinnerIdsList.add(Pair(value, columnDropdown))
                } else {
                    moreBtn.visibility = View.VISIBLE
                    columnDropDwonLayout.visibility = View.GONE
                    columnValue.visibility = View.VISIBLE
                    columnValue.setText(
                        pair.first
                    )
                    columnValue.isEnabled = false
                    columnValue.isFocusable = false
                    columnValue.isFocusableInTouchMode = false
                }

            } else {
                if (value == "image") {
                    textInputIdsList.add(Pair(value, columnValue))
                    continue
                }
                moreBtn.visibility = View.VISIBLE
                columnDropDwonLayout.visibility = View.GONE
                columnValue.visibility = View.VISIBLE

                if (value == "date") {
                    columnValue.setText(
                        BaseActivity.getDateTimeFromTimeStamp(
                            System.currentTimeMillis()
                        )
                    )
                    moreBtn.visibility = View.INVISIBLE
                    columnValue.isEnabled = false
                    columnValue.isFocusable = false
                    columnValue.isFocusableInTouchMode = false
                } else {
                    moreBtn.visibility = View.VISIBLE
                    columnValue.isEnabled = true
                    columnValue.isFocusable = true
//                                    columnValue.isFocusableInTouchMode = true
                    columnValue.setText("")
                }
                textInputIdsList.add(Pair(value, columnValue))
            }
            tableDetailLayoutWrapper.addView(tableRowLayout)
        }
        columns.addAll(updatedColumns)
    }


    fun pickImageFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher1.launch(
            Intent.createChooser(
                pickPhoto, getString(R.string.choose_image_gallery)
            )
        )
    }

    private var resultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val cropPicUri = CropImage.getPickImageResultUri(requireActivity(), data)
                cropImage(cropPicUri)
            }
        }

    private var cameraResultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                val bitmap = data!!.extras!!.get("data") as Bitmap
//                val file = ImageManager.readWriteImage(requireActivity(), bitmap)
//                cropImage(Uri.fromFile(file))
                val text = result.data!!.getStringExtra("SCAN_TEXT")
                updateInputBox!!.setText(text)
                updateInputBox!!.setSelection(updateInputBox!!.text.toString().length)
            }
        }

    fun pickImageFromCamera() {
        //        startActivity(Intent(context,OcrActivity::class.java))
        val takePictureIntent = Intent(context, OcrActivity::class.java)
        cameraResultLauncher1.launch(takePictureIntent)
    }

    private fun cropImage(imageUri: Uri) {

        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(requireActivity())
    }

    private fun renderQuickLinksDialog(searchTableObject: TableObject) {

        val quickLinksLayout =
            LayoutInflater.from(requireActivity()).inflate(R.layout.quick_links_dialog_layout, null)
        val typeTextHeading =
            quickLinksLayout.findViewById<MaterialTextView>(R.id.quick_links_code_detail_type_text_heading)
        typeTextHeading.text = getString(R.string.barcode_text_data_heding)
        val encodeDataTextView =
            quickLinksLayout.findViewById<MaterialTextView>(R.id.quick_links_code_detail_encode_data)
        encodeDataTextView.text = searchTableObject.code_data
        val clipboardCopyView =
            quickLinksLayout.findViewById<MaterialTextView>(R.id.quick_links_code_detail_clipboard_copy_view)
        clipboardCopyView.setOnClickListener {
            val clipboard: ClipboardManager =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                clipboard.primaryClipDescription!!.label,
                encodeDataTextView.text.toString()
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                requireActivity(),
                getString(R.string.text_saved_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        }
        barcodeDetailParentLayout =
            quickLinksLayout.findViewById(R.id.quick_links_barcode_detail_wrapper_layout)
        displayBarcodeDetail(searchTableObject)

        val cancelButton =
            quickLinksLayout.findViewById<AppCompatImageView>(R.id.quick_links_code_detail_cancel)
        val moreDetailButton =
            quickLinksLayout.findViewById<AppCompatImageButton>(R.id.quick_links_code_detail_more_button)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(quickLinksLayout)
        builder.setCancelable(false)
        val alertdialog = builder.create()
        alertdialog.show()
        cancelButton.setOnClickListener {
            alertdialog.dismiss()
            codeScanner!!.startPreview()
        }

        moreDetailButton.setOnClickListener {
            alertdialog.dismiss()
            val intent = Intent(requireActivity(), CodeDetailActivity::class.java)
            intent.putExtra("TABLE_NAME", tableName)
            intent.putExtra("TABLE_ITEM", searchTableObject)
            requireActivity().startActivity(intent)
        }

    }

    private fun displayItemNotFoundDialog(text: String) {
        val itemNotFoundLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.quick_links_item_not_found_dialog, null)
        val qcTableSpinner =
            itemNotFoundLayout.findViewById<AppCompatSpinner>(R.id.quick_links_tables_spinner)
        val cancelButton =
            itemNotFoundLayout.findViewById<MaterialButton>(R.id.quick_links_dialog_cancel_btn)
        val selectButton =
            itemNotFoundLayout.findViewById<MaterialButton>(R.id.quick_links_dialog_select_btn)
        val tablesList = mutableListOf<String>()
        tablesList.addAll(tableGenerator.getAllDatabaseTables())
        if (tablesList.isNotEmpty()) {
            tableName = tablesList[0]
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                tablesList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            qcTableSpinner.adapter = adapter

            if (appSettings.getString("SCAN_SELECTED_TABLE")!!.isNotEmpty()) {
                for (i in 0 until tablesList.size) {
                    val value = tablesList[i]
                    if (value == appSettings.getString("SCAN_SELECTED_TABLE")) {
                        qcTableSpinner.setSelection(i)
                        tableName = value
                        break
                    }
                }
            }
        }

        qcTableSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                tableName = adapterView!!.getItemAtPosition(i).toString()
                appSettings.putString("SCAN_SELECTED_TABLE", tableName)
            }
        }

        val builder1 = MaterialAlertDialogBuilder(requireActivity())
        builder1.setView(itemNotFoundLayout)
        val alertdialog1 = builder1.create()
        alertdialog1.show()
        selectButton.setOnClickListener {
            alertdialog1.dismiss()
            val searchObj = tableGenerator.getScanItem(tableName, text)
            if (searchObj != null) {
                renderQuickLinksDialog(searchObj)
            } else {
                displayItemNotFoundDialog(text)
            }
        }
        cancelButton.setOnClickListener {
            alertdialog1.dismiss()
            codeScanner!!.startPreview()
        }
    }

    var barcodeEditList = mutableListOf<Triple<AppCompatImageView, String, String>>()
    private var counter: Int = 0
    private fun displayBarcodeDetail(tableObject: TableObject) {


        if (barcodeDetailParentLayout.childCount > 0) {
            barcodeDetailParentLayout.removeAllViews()
        }

        barcodeEditList.add(
            Triple(
                AppCompatImageView(requireActivity()),
                tableObject.id.toString(),
                "id"
            )
        )
        val codeDataLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
        val codeDataColumnValue =
            codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
        val codeDataColumnName =
            codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
        val codeDataColumnEditView =
            codeDataLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
        codeDataColumnEditView.id = counter
        barcodeEditList.add(
            Triple(
                codeDataColumnEditView,
                tableObject.code_data,
                "code_data"
            )
        )
        codeDataColumnEditView.visibility = View.GONE
        codeDataColumnValue.text = tableObject.code_data
        codeDataColumnName.text = "code_data"
        barcodeDetailParentLayout.addView(codeDataLayout)
        val dateLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
        val dateColumnValue =
            dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
        val dateColumnName =
            dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
        val dateColumnEditView = dateLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
        counter += 1
        dateColumnEditView.id = counter
        barcodeEditList.add(Triple(dateColumnEditView, tableObject.date, "date"))
        dateColumnEditView.visibility = View.GONE
        dateColumnValue.text = tableObject.date
        dateColumnName.text = "date"
        barcodeDetailParentLayout.addView(dateLayout)
        val imageLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
        val imageColumnValue =
            imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
        val imageColumnName =
            imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
        val imageColumnEditView =
            imageLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
        counter += 1
        imageColumnEditView.id = counter
        barcodeEditList.add(Triple(imageColumnEditView, tableObject.image, "image"))
        imageColumnEditView.visibility = View.GONE
        imageColumnValue.text = tableObject.image
        imageColumnName.text = "image"
        barcodeDetailParentLayout.addView(imageLayout)

        for (i in 0 until tableObject.dynamicColumns.size) {
            val item = tableObject.dynamicColumns[i]

            val layout = LayoutInflater.from(requireActivity())
                .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
            val columnValue = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val columnName = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val columnEditView = layout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            counter += 1
            columnEditView.id = counter
            barcodeEditList.add(Triple(columnEditView, item.second, item.first))
            columnEditView.visibility = View.GONE
            columnValue.text = item.second
            columnName.text = item.first
            barcodeDetailParentLayout.addView(layout)

        }
        counter = 0
    }

    private fun saveDataIntoTable() {

        if (BaseActivity.isNetworkAvailable(requireActivity())) {
            BaseActivity.dismiss()
            BaseActivity.startLoading(requireActivity())
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val params = mutableListOf<Pair<String, String>>()

                    // THIS IF PART WILL RUN WHEN ADD IMAGE CHECK BOX IS CHECKED
                    if (addImageCheckBox.isChecked && filePathView!!.text.toString()
                            .isNotEmpty()
                    ) {
                        uploadImageOnFirebaseStorage(object : UploadImageCallback {
                            override fun onSuccess(imageUrl: String) {
                                updateStorageSize(totalImageSize, "minus")
                                // IF isUpload IS TRUE THEN DATA SAVE WITH IMAGE URL
                                // ELSE DISPLAY THE EXCEPTION MESSAGE WITHOUT DATA SAVING
                                if (url.isNotEmpty()) {
                                    if (multiImagesList.isNotEmpty()) {
                                        multiImagesList.clear()
                                    }
                                    if (params.size > 0) {
                                        params.clear()
                                    }
                                    // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED EDIT TEXT
                                    for (i in 0 until textInputIdsList.size) {
                                        val pair = textInputIdsList[i]
                                        // THIS WILL CHECK IF TEXTINPUTIDSLIST HAVE IMAGE PARAMETER THEN SET THE URL
                                        // WITH COLUMN IMAGE ELSE MAP THE OTHER TEXTINPUTIDS LIST OBJECTS
                                        if (pair.first == "image") {
                                            params.add(
                                                Pair(
                                                    pair.first,
                                                    url
                                                )
                                            )
                                        } else {
                                            params.add(
                                                Pair(
                                                    pair.first,
                                                    pair.second.text.toString()
                                                        .trim()
                                                )
                                            )
                                        }
                                    }

                                    // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED DROPDOWNS
                                    for (j in 0 until spinnerIdsList.size) {
                                        val pair = spinnerIdsList[j]
                                        params.add(
                                            Pair(
                                                pair.first,
                                                pair.second.selectedItem.toString()
                                            )
                                        )
                                    }
                                    tableGenerator.insertData(tableName, params)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Handler(Looper.myLooper()!!).postDelayed(
                                            {
                                                isFileSelected = false
                                                BaseActivity.dismiss()
                                                saveSuccessScans()
                                                Toast.makeText(
                                                    requireActivity(),
                                                    requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                textInputIdsList.clear()
                                                spinnerIdsList.clear()
                                                columns.clear()
                                                tableDetailLayoutWrapper.removeAllViews()
                                                filePathView!!.setText("")
                                                isScanResultDialogShowing = false
                                                barcodeImageList.clear()
                                                adapter.notifyDataSetChanged()
//                                                val tempList = mutableListOf<Any>()
//                                                for (i in 0 until params.size) {
//                                                    val pair = params[i]
//                                                    //values_JSON.put(pair.second)
//                                                    tempList.add(pair.second)
//                                                }
//                                                if (tempList.size > 0) {
//                                                    // sendRequest()
//                                                    appendRow(tempList)
//                                                }
                                                params.clear()
                                                val bundle = Bundle()
                                                bundle.putString("success", "success")
                                                mFirebaseAnalytics?.logEvent("scanner", bundle)
                                            },
                                            1000
                                        )
                                    }
                                }
                            }

                        })
                    }
                    // THIS ELSE PART WILL RUN WHEN ADD IMAGE CHECK BOX IS UN-CHECKED
                    else {
                        // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED EDIT TEXT
                        for (i in 0 until textInputIdsList.size) {
                            val pair = textInputIdsList[i]
                            // THIS WILL CHECK IF TEXTINPUTIDSLIST HAVE IMAGE PARAMETER THEN SET THE URL
                            // WITH COLUMN IMAGE ELSE MAP THE OTHER TEXTINPUTIDS LIST OBJECTS
                            if (pair.first == "image") {
                                params.add(
                                    Pair(
                                        pair.first,
                                        pair.second.text.toString()
                                            .trim()
                                    )
                                )
                            } else {
                                params.add(
                                    Pair(
                                        pair.first,
                                        pair.second.text.toString()
                                            .trim()
                                    )
                                )
                            }
                        }
                        // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED DROPDOWNS
                        for (j in 0 until spinnerIdsList.size) {
                            val pair = spinnerIdsList[j]
                            params.add(
                                Pair(
                                    pair.first,
                                    pair.second.selectedItem.toString()
                                )
                            )
                        }
                        tableGenerator.insertData(tableName, params)
                        CoroutineScope(Dispatchers.Main).launch {
                            Handler(Looper.myLooper()!!).postDelayed({
                                isFileSelected = false
                                BaseActivity.dismiss()
                                saveSuccessScans()
                                Toast.makeText(
                                    requireActivity(),
                                    requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                textInputIdsList.clear()
                                spinnerIdsList.clear()
                                columns.clear()
                                tableDetailLayoutWrapper.removeAllViews()
                                codeScanner!!.startPreview()
                                filePathView!!.setText("")
                                isScanResultDialogShowing = false
                                barcodeImageList.clear()
                                adapter.notifyDataSetChanged()
//                                openHistoryBtnTip()
//                                if (Constants.userData != null) {
//                                    val tempList = mutableListOf<Any>()
//                                    for (i in 0 until params.size) {
//                                        val pair = params[i]
//                                        //values_JSON.put(pair.second)
//                                        tempList.add(pair.second)
//                                    }
//                                    if (tempList.size > 0) {
//                                        // sendRequest()
//                                        appendRow(tempList)
//                                    }
////                                    for (i in 0 until params.size) {
////                                        val pair = params[i]
////                                        values_JSON.put(pair.second)
////                                    }
////                                    if (values_JSON.length() > 0) {
////                                        sendRequest()
////                                    }
//                                }
                                params.clear()
                            }, 1000)
                        }
                    }

                } catch (e: Exception) {
                    val bundle = Bundle()
                    bundle.putString("failure", "Error" + e.message)
                    mFirebaseAnalytics?.logEvent("scanner", bundle)
                    e.printStackTrace()
                }
            }
        } else {

            val b = MaterialAlertDialogBuilder(requireActivity())
                .setCancelable(true)
                .setTitle(requireActivity().resources.getString(R.string.alert_text))
                .setMessage(requireActivity().resources.getString(R.string.image_upload_internet_error_text))
                .setNegativeButton(requireActivity().resources.getString(R.string.close_text)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(requireActivity().resources.getString(R.string.save_without_image_text)) { dialog, which ->
                    dialog.dismiss()
                    alert.dismiss()
                    val params = mutableListOf<Pair<String, String>>()
                    for (i in 0 until textInputIdsList.size) {
                        val pair = textInputIdsList[i]
                        // THIS WILL CHECK IF TEXTINPUTIDSLIST HAVE IMAGE PARAMETER THEN SET THE URL
                        // WITH COLUMN IMAGE ELSE MAP THE OTHER TEXTINPUTIDS LIST OBJECTS
                        if (pair.first == "image") {
                            params.add(
                                Pair(
                                    pair.first,
                                    pair.second.text.toString()
                                        .trim()
                                )
                            )
                        } else {
                            params.add(
                                Pair(
                                    pair.first,
                                    pair.second.text.toString()
                                        .trim()
                                )
                            )
                        }
                    }
                    // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED DROPDOWNS
                    for (j in 0 until spinnerIdsList.size) {
                        val pair = spinnerIdsList[j]
                        params.add(
                            Pair(
                                pair.first,
                                pair.second.selectedItem.toString()
                            )
                        )
                    }
                    tableGenerator.insertData(tableName, params)
                    CoroutineScope(Dispatchers.Main).launch {
                        Handler(Looper.myLooper()!!).postDelayed({
                            isFileSelected = false
                            BaseActivity.dismiss()
                            saveSuccessScans()
                            Toast.makeText(
                                requireActivity(),
                                requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                Toast.LENGTH_SHORT
                            ).show()
                            textInputIdsList.clear()
                            spinnerIdsList.clear()
                            params.clear()
                            isScanResultDialogShowing = false
                            tableDetailLayoutWrapper.removeAllViews()
                            codeScanner!!.startPreview()
//                            openHistoryBtnTip()
                            val bundle = Bundle()
                            bundle.putString("success", "success")
                            mFirebaseAnalytics?.logEvent("scanner", bundle)
                        }, 1000)
                    }
                }
            val iAlert = b.create()
            iAlert.show()
            iAlert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.purple_700
                )
            )
        }

    }

    // THIS FUNCTION WILL ALERT THE DIFFERENT MESSAGES
    fun showAlert(context: Context, message: String) {
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
                codeScanner!!.startPreview()
            }
            .create().show()
    }

    private fun openAddImageTooltip(addImageBox: MaterialCheckBox, submitBtn: MaterialButton) {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt3")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(addImageBox)
                    .text(getString(R.string.add_image_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        tooltip.dismiss()
                        appSettings.putLong("tt3", System.currentTimeMillis())
                        openSubmitBtnTip(submitBtn)
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openSubmitBtnTip(submitBtn: MaterialButton) {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt4")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(submitBtn)
                    .text(getString(R.string.submit_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt4", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openHistoryBtnTip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt5")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(MainActivity.historyBtn)
                    .text(getString(R.string.history_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt5", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    var uploadedUrlList = mutableListOf<String>()
    var array: List<String> = mutableListOf()
    val handler = Handler(Looper.myLooper()!!)
    var totalImageSize: Long = 0
    var index = 0
    private fun uploadImageOnFirebaseStorage(listener: UploadImageCallback) {
        val imageList = filePathView!!.text.toString()
        if (imageList.contains(",")) {
            array = imageList.split(",")
            uploadImage(listener)
        } else {
            val bundle = Bundle()
            bundle.putString("starts", "starts")
            mFirebaseAnalytics?.logEvent("upload image", bundle)

            if (FirebaseAuth.getInstance().currentUser != null) {
                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                val imageBase64String = ImageManager.convertImageToBase64(
                    requireActivity(),
                    filePathView!!.text.toString()
                )
                BaseActivity.uploadImageOnServer(
                    requireActivity(),
                    imageBase64String,
                    userId,
                    object : UploadImageCallback {
                        override fun onSuccess(imageUrl: String) {
                            url = imageUrl
                            listener.onSuccess("")
                        }

                    })
            }
        }

    }

    private fun uploadImage(callback: UploadImageCallback) {
        if (FirebaseAuth.getInstance().currentUser != null) {

            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val imagePath = array[index]
            val imageBase64String = ImageManager.convertImageToBase64(requireActivity(), imagePath)
            BaseActivity.uploadImageOnServer(
                requireContext(),
                imageBase64String,
                userId,
                object : UploadImageCallback {
                    override fun onSuccess(imageUrl: String) {
                        Log.d("TEST199", imageUrl)
                        uploadedUrlList.add(imageUrl)
                        if (index == array.size - 1) {
                            url = uploadedUrlList.joinToString(" ")
                            uploadedUrlList.clear()
                            index = 0
                            callback.onSuccess("")
                        } else {
                            index++
                            uploadImage(callback)
                        }
                    }

                })
        }
    }

    private fun getTotalImagesSize(uploadedUrlList: MutableList<String>) {
        //var total: Long = 0
        totalImageSize = 0
        for (i in 0 until uploadedUrlList.size) {
            Handler(Looper.myLooper()!!).postDelayed({
                lifecycleScope.launch {
                    val compressedImageFile =
                        Compressor.compress(requireActivity(), File(uploadedUrlList[i]))
                    totalImageSize += ImageManager.getFileSize(compressedImageFile.absolutePath)
                    Log.d("TEST199TOTALSIZE", "$totalImageSize")
                }
            }, (1000 * i + 1).toLong())
        }
//        return totalImageSize
    }


    private fun updateStorageSize(size: Long, type: String) {
        var currentStorageSize: Float = 0F
        if (Constants.userServerAvailableStorageSize.isNotEmpty()) {
            currentStorageSize =
                Constants.convertMegaBytesToBytes(Constants.userServerAvailableStorageSize.toFloat()) - size

            val remainingMb = Constants.convertBytesToMegaBytes(currentStorageSize).toString()
            appSettings.putString(Constants.memory, remainingMb)
            BaseActivity.updateMemorySize(
                requireActivity(),
                remainingMb,
                0,
                Constants.firebaseUserId,
                0,
                "",
                object : APICallback {
                    override fun onSuccess(response: JSONObject) {

                    }

                    override fun onError(error: VolleyError) {

                    }

                })
        } else {
            val stringRequest = object : StringRequest(
                Method.POST, "https://itmagicapp.com/api/get_user_packages.php",
                Response.Listener {
                    val response = JSONObject(it)
                    if (response.getInt("status") == 200) {
                        if (response.has("package") && !response.isNull("package")) {
                            val packageDetail: JSONObject? = response.getJSONObject("package")
                            if (packageDetail != null) {
                                val availableSize = packageDetail.getString("size")
                                Constants.userServerAvailableStorageSize = availableSize

                                currentStorageSize =
                                    Constants.convertMegaBytesToBytes(availableSize.toFloat()) - size

                                val remainingMb =
                                    Constants.convertBytesToMegaBytes(currentStorageSize).toString()
                                BaseActivity.updateMemorySize(
                                    requireActivity(),
                                    remainingMb,
                                    0,
                                    Constants.firebaseUserId,
                                    0,
                                    "",
                                    object : APICallback {
                                        override fun onSuccess(response: JSONObject) {

                                        }

                                        override fun onError(error: VolleyError) {

                                        }

                                    })
                            }
                        }
                    }
                }, Response.ErrorListener {
                    Log.d("TEST199", it.localizedMessage!!)

                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user_id"] = Constants.firebaseUserId
                    return params
                }
            }

            stringRequest.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            }

            VolleySingleton(requireActivity()).addToRequestQueue(stringRequest)
        }

//        databaseReference.child(Constants.firebaseUserFeatureDetails)
//            .child(Constants.firebaseUserId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.hasChildren()) {
//                        val pSize: Float? =
//                            snapshot.child("memory").getValue(String::class.java)!!.toFloat()
//                        if (pSize != null) {
//                            currentStorageSize = if (type == "add") {
//                                Constants.convertMegaBytesToBytes(pSize) + size
//                            } else {
//                                Constants.convertMegaBytesToBytes(pSize) - size
//                            }
//                            val remainingMb = Constants.convertBytesToMegaBytes(
//                                currentStorageSize
//                            ).toString()
//
//                            val params = HashMap<String, Any>()
//                            params["memory"] = remainingMb
//                            databaseReference.child(Constants.firebaseUserFeatureDetails)
//                                .child(Constants.firebaseUserId).updateChildren(params)
//                            totalImageSize = 0
//                            appSettings.putString(
//                                Constants.memory,
//                                remainingMb
//                            )
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
    }

    @Throws(java.lang.Exception::class)
    private fun insertPermission(fileId: String) {
        try {
            val newPermission = Permission()
            newPermission.type = "anyone"
            newPermission.role = "writer"
            DriveService.instance!!.permissions().create(fileId, newPermission).execute()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var userAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                chooseAccountLauncher.launch(MainActivity.credential!!.newChooseAccountIntent())
            }
        }


    private var chooseAccountLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val accountName: String? =
                    result.data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    //MainActivity.credential!!.backOff = ExponentialBackOff()
                    MainActivity.credential!!.selectedAccountName = accountName
                    appSettings.putString("ACCOUNT_NAME", accountName)
                    if (userRecoverableAuthType == 0) {
                        saveDataIntoTable()
                    } else {
                        //getAllSheets()
                    }

                }

            }
        }


    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
        //Constants.captureImagePath = currentPhotoPath
        multiImagesList.add(currentPhotoPath!!)
        filePathView!!.text = multiImagesList.joinToString(",")
        barcodeImageList.clear()
        barcodeImageList.addAll(multiImagesList)
        adapter.notifyDataSetChanged()
        isFileSelected = true
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }


    var multiImagesList = mutableListOf<String>()
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
//                val path: String? = null
                val data: Intent? = result.data
                val clipData: ClipData? = data!!.clipData

                if (clipData != null) {
                    if (clipData.itemCount > 0) {
                        for (i in 0 until clipData.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            multiImagesList.add(
                                ImageManager.getRealPathFromUri(
                                    requireActivity(),
                                    imageUri
                                )!!
                            )
                        }
                        filePathView!!.text = multiImagesList.joinToString(",")
                        barcodeImageList.clear()
                        barcodeImageList.addAll(multiImagesList)
                        adapter.notifyDataSetChanged()
                        isFileSelected = true
                        //Log.d("TEST199",multiImagesList.toString())
                    }
                } else {
                    if (data.data != null) {
                        val imageUri = data.data!!
                        multiImagesList.add(
                            ImageManager.getRealPathFromUri(
                                requireActivity(),
                                imageUri
                            )!!
                        )
                        filePathView!!.text = multiImagesList.joinToString(",")
                        barcodeImageList.clear()
                        barcodeImageList.addAll(multiImagesList)
                        adapter.notifyDataSetChanged()
                        isFileSelected = true
                    }
                }
//                totalImageSize = if (filePathView!!.text.contains(",")) {
                getTotalImagesSize(filePathView!!.text.split(",").toMutableList())
//                } else {
//                    ImageManager.getFileSize(filePathView!!.text.toString())
//                }
                Log.d("TEST199", "$totalImageSize")
            }
        }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val bitmap = data!!.extras!!.get("data") as Bitmap
                createImageFile(bitmap)

//                totalImageSize = if (filePathView!!.text.contains(",")) {
                getTotalImagesSize(filePathView!!.text.split(",").toMutableList())
//                } else {
//                    ImageManager.getFileSize(filePathView!!.text.toString())
//                }

                Log.d("TEST199", "$totalImageSize")
            }
        }

    override fun onResume() {
        super.onResume()
        startScanner()
        getTableList()
        getModeList()
        //getAllSheets()
        val flag = appSettings.getBoolean(requireActivity().getString(R.string.key_tips))
        if (flag) {
            tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_on_text))
        } else {
            tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_off_text))
        }
        tipsSwitchBtn.isChecked = flag

        tipsSwitchBtn.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_on_text))
                } else {
                    tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_off_text))
                }
                appSettings.putBoolean(requireActivity().getString(R.string.key_tips), isChecked)
            }
        })

        setEventListener(
            requireActivity(),
            requireActivity(),
            KeyboardVisibilityEventListener { isOpen ->
                // some code depending on keyboard visiblity status
                if (textRecognitionButtonsLayout != null) {
                    if (isOpen) {
//                    textRecognitionButtonsLayout!!.visibility = View.VISIBLE
                        //openDialog()
                    }
//                else{
////                    textRecognitionButtonsLayout!!.visibility = View.GONE
//                }
                }
            })

        if (Constants.isDefaultTableFieldAdded) {
            Constants.isDefaultTableFieldAdded = false
            renderTableColumnViews()
        }
    }


    override fun onPause() {
        if (codeScanner != null) {
            codeScanner!!.releaseResources()
        }
        super.onPause()
    }

    // THIS FUNCTION WILL HANDLE THE RUNTIME PERMISSION RESULT
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
            Constants.READ_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Constants.READ_STORAGE_PERMISSION
                        )
                    ) {
                        RuntimePermissionHelper.checkStoragePermission(
                            requireActivity(),
                            Constants.READ_STORAGE_PERMISSION
                        )
                    } else {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(requireActivity().resources.getString(R.string.external_storage_permission_error2))
                            .setCancelable(false)
                            .setPositiveButton(requireActivity().resources.getString(R.string.ok_text)) { dialog, which ->
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

    private fun playSound(isSuccess: Boolean) {
        val isSounding = appSettings.getBoolean(requireContext().getString(R.string.key_sound))
        if (isSounding) {
            var player: MediaPlayer? = null
            player = if (isSuccess) {
                MediaPlayer.create(requireContext(), R.raw.succes_beep)
            } else {
                MediaPlayer.create(requireContext(), R.raw.error_beep)

            }
            player.start()
        }
    }

    private fun generateVibrate() {
        val isVibrate = appSettings.getBoolean(requireContext().getString(R.string.key_vibration))
        if (isVibrate) {
            if (Build.VERSION.SDK_INT >= 26) {
                (requireContext().getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                (requireContext().getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(150)
            }
        }
    }

    private fun copyToClipBoard(content: String) {
        val isAllowCopy = appSettings.getBoolean(requireContext().getString(R.string.key_clipboard))
        if (isAllowCopy) {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Scan code", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "copied", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imgUri = result.uri
            try {
                if (updateInputBox != null) {
                    TextRecogniser.runTextRecognition(requireActivity(), updateInputBox!!, imgUri)
                } else {
                    showAlert(
                        requireActivity(),
                        getString(R.string.textinput_not_focused_error_text)
                    )
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun bindPreview(processCameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val imageCapture = ImageCapture.Builder().build()


        val imageAnalysis =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ImageAnalysis.Builder()
                    .setTargetResolution(Size(1200, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also {
                        cameraExecutor?.let { it1 ->
                            imageAnalyzer?.let { it2 ->
                                it.setAnalyzer(
                                    it1,
                                    it2
                                )
                            }
                        }
                    }
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }

        processCameraProvider.unbindAll()
        cam = processCameraProvider.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
    }

    inner class MyImageAnalyzer(supportFragmentManager: FragmentManager) : ImageAnalysis.Analyzer {
        var count = 0
        private var fragmentManager: FragmentManager? = null
        val appViewModel = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
//        var bottomDialog: BottomDialog? = null

        init {
            this.fragmentManager = supportFragmentManager
//            bottomDialog = BottomDialog()
        }

        override fun analyze(image: ImageProxy) {
            scanBarCode(image)
        }


        private fun scanBarCode(image: ImageProxy) {
            @SuppressLint("UnsafeOptInUsageError") val image1 = image.image!!
            val inputImage = InputImage.fromMediaImage(image1, image.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODABAR,
                    Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E, Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_PDF417, Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_DATA_MATRIX
                )
                .build()
            val scanner = BarcodeScanning.getClient(options)
            val result = scanner.process(inputImage)
                .addOnSuccessListener { barcodes -> // Task completed successfully

                    readBarCodeData(barcodes)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }.addOnCompleteListener { barcodes ->
                    image.close()
                }

        }

        private fun readBarCodeData(barcodes: List<Barcode>) {
            if (barcodes.isNotEmpty()) {
                count++
                if (count == 1) {
//                val bounds = barcodes[0].boundingBox
//                val corners = barcodes[0].cornerPoints
                    val rawValue = barcodes[0].rawValue
                    val valueType = barcodes[0].valueType
                    // See API reference for complete list of supported types
                    Log.d("TEST199", "readBarCodeData: $rawValue")
                    if (barcodes[0].rawValue != null) {
                        displayDataSubmitDialog(null, rawValue!!)
                    }
                }

            }
        }
    }

    fun showTableSelectTip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt10")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(addNewTableBtn)
                    .text(getString(R.string.table_selector_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt10", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun saveSuccessScans() {
        var scans = DialogPrefs.getSuccessScan(requireActivity())
        if (scans >= 0) {
            scans += 1
            DialogPrefs.setSuccessScan(requireActivity(), scans)
        }
        Log.d("TAG", "ScanCount: $scans")
    }

    private fun getAllSheets() {
        if (Constants.userData != null) {
            connectGoogleSheetsTextView.visibility = View.GONE
            sheetsTopLayout.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result: FileList = DriveService.instance!!.files().list()
                        .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                        .execute()

                    val files = result.files

                    if (files != null) {
                        if (files.size > 0) {
                            sheetsList.clear()
                        }
                        for (file in files) {
                            sheetsList.add(Sheet(file.id, file.name))
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            if (sheetsList.isNotEmpty()) {
                                Constants.sheetsList.addAll(sheetsList)
                                displaySheetSpinner()
                            } else {
                                MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage(getString(R.string.google_sheet_empty_list_error))
                                    .setCancelable(false)
                                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    .setPositiveButton(getString(R.string.create)) { dialog, which ->
                                        dialog.dismiss()
                                        val defaultSheetArray = mutableListOf<Any>()
                                        createNewSpreadsheet("default_sheet", defaultSheetArray)
                                    }.create().show()
                            }
                        }
                    }
                } catch (userRecoverableException: UserRecoverableAuthIOException) {
                    userRecoverableAuthType = 1
                    userAuthLauncher.launch(userRecoverableException.intent)
                }
            }
        } else {
            sheetsTopLayout.visibility = View.GONE
            connectGoogleSheetsTextView.visibility = View.VISIBLE
        }
    }

    private fun displaySheetSpinner() {
        if (sheetsList.isNotEmpty()) {
            selectedSheetId = sheetsList[0].id
            selectedSheetId = sheetsList[0].name
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                sheetsList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetsSpinner.adapter = adapter

            if (appSettings.getString("SELECTED_SHEET")!!.isNotEmpty()) {
                for (i in 0 until sheetsList.size) {
                    val value = sheetsList[i].id
                    if (value == appSettings.getString("SELECTED_SHEET")) {
                        sheetsSpinner.setSelection(i)
                        selectedSheetId = value
                        selectedSheetName = sheetsList[i].name
                        break
                    }
                }
            }
            fetchSheetColumns()
        }

        sheetsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                selectedSheetId = sheetsList[i].id//adapterView!!.getItemAtPosition(i).toString()
                selectedSheetName = sheetsList[i].name
                appSettings.putString("SELECTED_SHEET", selectedSheetId)
                fetchSheetColumns()
            }
        }
    }

    var values_JSON = JSONArray()
    private fun sendRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val sr: StringRequest = object : StringRequest(
                    Method.POST,
                    Constants.googleAppScriptUrl,
                    object : Response.Listener<String?> {
                        override fun onResponse(response: String?) {

                            CoroutineScope(Dispatchers.Main).launch {
                                if (response!!.toLowerCase(Locale.ENGLISH).contains("success")) {
                                    Log.d("TEST199", "sheet data success")
                                } else {
                                    val permissionDeniedLayout = LayoutInflater.from(context)
                                        .inflate(
                                            R.layout.spreadsheet_permission_failed_dialog,
                                            null
                                        )
                                    val builder = MaterialAlertDialogBuilder(requireActivity())
                                    builder.setCancelable(false)
                                    builder.setView(permissionDeniedLayout)
                                    builder.setPositiveButton("Ok") { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    val alert = builder.create()
                                    alert.show()
                                }
                                values_JSON = JSONArray()

                            }
                        }
                    },
                    object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError?) {
//                            Toast.makeText(context, error!!.toString(), Toast.LENGTH_SHORT).show()
                            BaseActivity.dismiss()
                        }
                    }) {

                    override fun getBodyContentType(): String {
                        return "application/x-www-form-urlencoded"
                    }

                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["sheetName"] = selectedSheetName
                        params["number"] = "${values_JSON.length()}"
                        params["id"] = selectedSheetId
                        params["value"] = "$values_JSON"
                        return params
                    }

                }
                sr.retryPolicy = DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                VolleySingleton(requireActivity()).addToRequestQueue(sr)

            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        updateInputBox = v as CustomTextInputEditText?
//        if (hasFocus) {
//            openDialog()
//        }
    }

    fun restart() {
        onResume()
    }

    private fun openDialog() {
        if (updateInputBox != null) {
            updateInputBox!!.clearFocus()
            customAlertDialog = CustomAlertDialog()
            customAlertDialog!!.setFocusListener(this)
            customAlertDialog!!.show(childFragmentManager, "dialog")
        } else {
            Toast.makeText(
                requireActivity(),
                "Set the input box focus before click on More Button",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onEraseBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()
            updateInputBox!!.requestFocus()
        }
        updateInputBox!!.setText("")
    }

    override fun onImageRecognitionBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()

            BaseActivity.hideSoftKeyboard(requireActivity(), updateInputBox!!)
            pickImageFromGallery()

        }
    }

    override fun onPhotoRecognitionBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()

            BaseActivity.hideSoftKeyboard(requireActivity(), updateInputBox!!)
            pickImageFromCamera()

        }
    }

    override fun onDismissBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()
            updateInputBox!!.requestFocus()
            Constants.openKeyboar(requireActivity())
        }
    }

    private fun fetchSheetColumns() {
        CoroutineScope(Dispatchers.IO).launch {
            val range = "A:Z"
            var response: ValueRange? = null
            try {
                val request = SheetService.instance!!.spreadsheets().values().get(
                    selectedSheetId,
                    range
                )
                response = request.execute()

                if (response != null) {
                    numRows = if (response.getValues() != null) response.getValues().size else 0
                    if (numRows > 0) {
                        values = response.getValues()[0]
                    }
//                    else{
                    Log.d("TEST199", "$numRows")
//                    }

//                val detail = StringBuilder()
//                for(i in values!!.indices){
//                    detail.append("C ${i+1}: ${values!![i]}")
//                    if (i != values!!.size-1){
//                        detail.append("\n")
//                    }
//                }
//                Log.d("TEST199SHEETCOLUMNS",values.toString())
//                CoroutineScope(Dispatchers.Main).launch {
//                    BaseActivity.showAlert(requireContext(),detail.toString())
//                }
                }

            } catch (e: UserRecoverableAuthIOException) {
                userAuthLauncher.launch(e.intent)
                //Toast.makeText(getApplicationContext(), "Can't Fetch columns of your sheet", Toast.LENGTH_LONG).show();
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun appendRow(list: List<Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            val range = "A:Z"
            val rows = listOf(list)
            val body = ValueRange().setValues(rows)

            val result =
                SheetService.instance!!.spreadsheets().values().append(selectedSheetId, range, body)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute()
            fetchSheetColumns()
            Log.d("TEST199ROW", "${result.updates.updatedRows}")
        }
    }

    private fun checkBothListSame(): Boolean {
        val first = mutableListOf<Any>()
        first.addAll(tableGenerator.getTableColumns(tableName)!!.toList())
        first.removeAt(0)
        if (numRows == 0) {
            appendRow(first)
            return true
        } else if (values != null && isEqual(first, values as List<Any>)) {
            return true
        }
        return false
    }

    private fun createNewSpreadsheetDialog(tableName: String) {
        val first = mutableListOf<Any>()
        first.addAll(tableGenerator.getTableColumns(tableName)!!.toList())
        first.removeAt(0)

        MaterialAlertDialogBuilder(requireActivity())
            .setMessage("Do you want to create a new spreadsheet to save data? Because format of *$selectedSheetName is not correct")
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.create)) { dialog, which ->
                dialog.dismiss()

                createNewSpreadsheet(tableName, first)
            }.create().show()
    }

    private fun createNewSpreadsheet(tableName: String, headingsList: List<Any>) {
        BaseActivity.startLoading(requireActivity())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var spreadsheet = Spreadsheet()
                spreadsheet.properties = SpreadsheetProperties()
                    .setTitle(tableName)
                spreadsheet = SheetService.instance!!.spreadsheets().create(spreadsheet)
                    .setFields("spreadsheetId")
                    .execute()

                selectedSheetId = spreadsheet.spreadsheetId
                selectedSheetName = tableName

                insertPermission(selectedSheetId)

                appSettings.putString("SELECTED_SHEET", selectedSheetId)
                if (values_JSON.length() > 0) {
                    values_JSON = JSONArray()
                }

                getAllSheets()
                BaseActivity.dismiss()
                if (headingsList.isNotEmpty()) {
                    for (i in 0 until headingsList.size) {
                        values_JSON.put(headingsList[i].toString())
                    }
                    appendRow(headingsList)
                }

            } catch (e: Exception) {
                BaseActivity.dismiss()
                e.printStackTrace()
            }

        }
    }

    private fun <T> isEqual(first: List<T>, second: List<T>): Boolean {

        if (first.size != second.size) {
            return false
        }

        return first.zip(second).all { (x, y) -> x == y }
    }

}