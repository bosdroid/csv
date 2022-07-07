package com.boris.expert.csvmagic.view.fragments

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.camera.core.Camera
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.BarcodeImageAdapter
import com.boris.expert.csvmagic.adapters.InternetImageAdapter
import com.boris.expert.csvmagic.adapters.TablesDataAdapter
import com.boris.expert.csvmagic.customviews.CustomTextInputEditText
import com.boris.expert.csvmagic.interfaces.*
import com.boris.expert.csvmagic.model.Sheet
import com.boris.expert.csvmagic.model.TableObject
import com.boris.expert.csvmagic.room.AppViewModel
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.activities.*
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit


class ScanFragment : Fragment(), TablesDataAdapter.OnItemClickListener,
    CustomAlertDialog.CustomDialogListener,
    View.OnFocusChangeListener, View.OnClickListener {

    //    private lateinit var qrCodeHistoryRecyclerView: RecyclerView
//    private lateinit var emptyView: MaterialTextView
//    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
//    private lateinit var adapter: QrCodeHistoryAdapter
//    private lateinit var appViewModel: AppViewModel
    private lateinit var internetImageDoneBtn: MaterialButton
    private lateinit var tableDataRecyclerView: RecyclerView
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesDataAdapter
    private lateinit var fabUploadFile: FloatingActionButton
    private lateinit var fabAddOption: FloatingActionButton
    private var listener: ScannerInterface? = null
    private var fragmentChangeListener: FragmentChangeListener? = null
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
    private lateinit var barcodeImageadapter: BarcodeImageAdapter
    var currentPhotoPath: String? = null

    //private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView
    private lateinit var appViewModel: AppViewModel
    private lateinit var tipsSwitchBtn: SwitchMaterial
    private var tableName: String = ""
    private lateinit var tablesSpinner: AppCompatSpinner
    private lateinit var sheetsSpinner: AppCompatSpinner
    private lateinit var modesSpinner: AppCompatSpinner
    private lateinit var barcodeLocalImageScannerView: AppCompatImageView
    private var textInputIdsList = mutableListOf<Pair<String, CustomTextInputEditText>>()
    private var spinnerIdsList = mutableListOf<Pair<String, AppCompatSpinner>>()
    private lateinit var addNewTableBtn: MaterialButton
    private lateinit var appSettings: AppSettings
    private var imageDrivePath = ""
    private var isFileSelected = false
    private var cameraProviderFuture: ListenableFuture<*>? = null
    private var cameraExecutor: ExecutorService? = null
    private var mContext: AppCompatActivity? = null
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var textRecognitionButtonsLayout: LinearLayout? = null

    //private var previewView: PreviewView? = null
    private var imageAnalyzer: ScannerFragment.MyImageAnalyzer? = null
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
    private var galleryIntentType = 0
    private var userCurrentCredits = ""
    private lateinit var alert: AlertDialog
    private lateinit var addImageCheckBox: MaterialCheckBox
    private lateinit var tableDetailLayoutWrapper: LinearLayout
    private lateinit var barcodeDetailParentLayout: LinearLayout
    var multiImagesList = mutableListOf<String>()
    var uploadedUrlList = mutableListOf<String>()
    var array: List<String> = mutableListOf()
    val handler = Handler(Looper.myLooper()!!)
    var totalImageSize: Long = 0
    var index = 0
    var url = " "
    private lateinit var itemDetail: TableObject
    private lateinit var csvItemDetail: List<Pair<String, String>>
    private lateinit var tableMainLayout: TableLayout
    private var dataList = mutableListOf<TableObject>()
    private var dataListCsv = mutableListOf<List<Pair<String, String>>>()
    private var sortingImages = mutableListOf<AppCompatImageView>()
    private lateinit var csvExportImageView: AppCompatImageView
    private lateinit var quickEditCheckbox: MaterialCheckBox
    private var currentColumn = ""
    private var currentOrder = ""
    private var quickEditFlag = false
    val layoutParams = TableRow.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        2f
    )

    private lateinit var internetImageAdapter: InternetImageAdapter
    private lateinit var searchBtnView: ImageButton
    private lateinit var searchBoxView: TextInputEditText
    private lateinit var loader: ProgressBar
    private lateinit var voiceSearchIcon:AppCompatImageView
    private var voiceLanguageCode = "en"
    val searchedImagesList = mutableListOf<String>()

    interface FragmentChangeListener {
        fun onChange()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
        listener = context as ScannerInterface
        fragmentChangeListener = context as FragmentChangeListener
//        appViewModel = ViewModelProvider(
//            this,
//            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
//        ).get(AppViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_scan, container, false)

        initViews(v)
//        getDisplayScanHistory()
        //displayTableList()
        return v
    }

    private fun initViews(view: View) {
        tablesSpinner = view.findViewById(R.id.tables_spinner)
        tableGenerator = TableGenerator(requireActivity())
        tableDataRecyclerView = view.findViewById(R.id.tables_data_recyclerview)
        fabUploadFile = view.findViewById(R.id.fab_upload_file)
        fabAddOption = view.findViewById(R.id.fab_add_option)
        tableDataRecyclerView.layoutManager = LinearLayoutManager(context)
        tableDataRecyclerView.hasFixedSize()
        adapter = TablesDataAdapter(requireActivity(), tableList as ArrayList<String>)
        tableDataRecyclerView.adapter = adapter
        tableMainLayout = view.findViewById(R.id.table_main)

        csvExportImageView = view.findViewById(R.id.export_csv)
        quickEditCheckbox = view.findViewById(R.id.quick_edit_table_view_checkbox)
        quickEditCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            quickEditFlag = isChecked
        }

        csvExportImageView.setOnClickListener {
            if (tableName.contains("import")) {
                exportCsv1(tableName)
            } else {
                exportCsv(tableName)
            }

        }

//        fabUploadFile.setOnClickListener {
//            if (Constants.userData != null) {
//                importCsv()
//            } else {
//                //showAlert(context,"You can not create dynamic table without account login!")
//                MaterialAlertDialogBuilder(requireActivity())
//                    .setTitle(getString(R.string.alert_text))
//                    .setMessage(getString(R.string.login_error_text))
//                    .setNegativeButton(getString(R.string.later_text)) { dialog, which ->
//                        dialog.dismiss()
//                    }
//                    .setPositiveButton(getString(R.string.login_text)) { dialog, which ->
//                        dialog.dismiss()
//                        listener!!.login(object : LoginCallback {
//                            override fun onSuccess() {
//                                Log.d("TEST199", "success")
//                                displayTableList()
//                            }
//
//                        })
////                         importCsv()
//
//                    }
//                    .create().show()
//            }
//        }

        fabAddOption.setOnClickListener {

            val optionLayout = LayoutInflater.from(requireActivity())
                .inflate(R.layout.option_selection_dialog, null)

            val scanBtn = optionLayout.findViewById<MaterialTextView>(R.id.os_item_scanner_view)
            val manualBtn = optionLayout.findViewById<MaterialTextView>(R.id.os_item_manual_view)
            val importFile = optionLayout.findViewById<MaterialTextView>(R.id.os_item_import_file)
            val cancelBtn = optionLayout.findViewById<MaterialTextView>(R.id.os_item_cancel_view)

            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setView(optionLayout)
            builder.setCancelable(false)
            val alert = builder.create()
            alert.show()

            cancelBtn.setOnClickListener {
                alert.dismiss()
            }

            scanBtn.setOnClickListener {
                alert.dismiss()
                fragmentChangeListener!!.onChange()
            }

            manualBtn.setOnClickListener {
                alert.dismiss()
                displayDataSubmitDialog()
            }

            importFile.setOnClickListener {
                if (Constants.userData != null) {
                    alert.dismiss()
                    importCsv()
                } else {
                    //showAlert(context,"You can not create dynamic table without account login!")
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(getString(R.string.alert_text))
                        .setMessage(getString(R.string.login_error_text))
                        .setNegativeButton(getString(R.string.later_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.login_text)) { dialog, which ->
                            dialog.dismiss()
                            alert.dismiss()
                            listener!!.login(object : LoginCallback {
                                override fun onSuccess() {
                                    Log.d("TEST199", "success")
                                    displayTableList()
                                }

                            })
//                         importCsv()

                        }
                        .create().show()
                }

            }


        }

//        emptyView = view.findViewById(R.id.emptyView)
//        qrCodeHistoryRecyclerView = view.findViewById(R.id.qr_code_history_recyclerview)
//        qrCodeHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
//        qrCodeHistoryRecyclerView.hasFixedSize()
//        adapter = QrCodeHistoryAdapter(requireActivity(), qrCodeHistoryList as ArrayList<CodeHistory>)
//        qrCodeHistoryRecyclerView.adapter = adapter
//        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener{
//            override fun onItemClick(position: Int) {
//                val historyItem = qrCodeHistoryList[position]
////                showAlert(context,historyItem.toString())
//                val intent = Intent(context, CodeDetailActivity::class.java)
//                intent.putExtra("HISTORY_ITEM",historyItem)
//                startActivity(intent)
//            }
//        })
    }

    private fun displayTableData() {
        if (tableMainLayout.childCount > 0){
            tableMainLayout.removeAllViews()
        }
        val columns = tableGenerator.getTableColumns(tableName)
        val tableHeaders = TableRow(context)
        tableHeaders.weightSum = columns!!.size * 2F
        for (i in 0 until columns.size + 1) {
//             if (i == 0 && tableName.contains("import")){
//                 continue
//             }
            if (i == 0) {
                val headerLayout =
                    LayoutInflater.from(context).inflate(R.layout.header_table_row_cell, null)
                headerLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.purple_dark
                    )
                )
                val sortImageView =
                    headerLayout.findViewById<AppCompatImageView>(R.id.sort_image)
                headerLayout.layoutParams = layoutParams
                sortImageView.visibility = View.INVISIBLE
                tableHeaders.addView(headerLayout)
            } else {
                val headerLayout =
                    LayoutInflater.from(context).inflate(R.layout.header_table_row_cell, null)
                headerLayout.layoutParams = layoutParams
                val textView = headerLayout.findViewById<MaterialTextView>(R.id.header_cell_name)
                val sortImageView =
                    headerLayout.findViewById<AppCompatImageView>(R.id.sort_image)
                sortImageView.visibility = View.VISIBLE
                sortImageView.id = i
                sortingImages.add(sortImageView)

                headerLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.purple_dark
                    )
                )
                headerLayout.layoutParams = layoutParams
                textView.text = columns[i - 1].toUpperCase(Locale.ENGLISH)
                textView.setBackgroundResource(R.drawable.left_border)
                headerLayout.id = i - 1
                headerLayout.tag = columns[i - 1].toLowerCase(Locale.ENGLISH)
                headerLayout.setOnClickListener(this)
                tableHeaders.addView(headerLayout)
            }
        }

        tableMainLayout.addView(tableHeaders)

        if (tableName.contains("import")) {
            getTableDataFromCsv(tableName, "", "")
        } else {
            getTableData(tableName, "", "")
        }
    }


    private fun getTableList() {

        val tablesList = mutableListOf<String>()
//        if (Constants.unlimitedTablesFeatureStatus == 0) {
//            tablesList.add(tableGenerator.getAllDatabaseTables()[0])
//        } else {
        tablesList.addAll(tableGenerator.getAllDatabaseTables())
//        }

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
                        tablesSpinner.setSelection(i,false)
                        tableName = value
                        break
                    }
                }
            }
            displayTableData()
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
                    displayTableData()
            }
        }
    }

    private fun displayDataSubmitDialog() {
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
        barcodeImageadapter = BarcodeImageAdapter(
            requireContext(),
            barcodeImageList as ArrayList<String>
        )
        barcodeImagesRecyclerView!!.adapter = barcodeImageadapter
        barcodeImageadapter.setOnItemClickListener(object :
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
                    if (multiImagesList.isNotEmpty()) {
                        filePathView!!.text = multiImagesList.joinToString(",")
                        getTotalImagesSize(
                            filePathView!!.text.split(",").toMutableList()
                        )
                    }
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
        val internetImageView =
            scanResultLayout.findViewById<AppCompatImageView>(R.id.internet_image_view)

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
            galleryIntentType = 0
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

        internetImageView.setOnClickListener {
            val tempImageList = mutableListOf<String>()
            val internetSearchLayout = LayoutInflater.from(requireActivity())
                .inflate(R.layout.internet_image_search_dialog_layout, null)
            loader =
                internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
            searchBoxView =
                internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
            searchBtnView =
                internetSearchLayout.findViewById<ImageButton>(R.id.internet_image_search_btn)
            val internetImageRecyclerView =
                internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
            val closeBtn =
                internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
            voiceSearchIcon = internetSearchLayout.findViewById(R.id.voice_search_internet_images)
            internetImageDoneBtn = internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setCancelable(false)
            builder.setView(internetSearchLayout)
            val iAlert = builder.create()
            iAlert.show()

            internetImageDoneBtn.setOnClickListener {
                iAlert.dismiss()
            }

            closeBtn.setOnClickListener {
                if (tempImageList.isNotEmpty()) {
                    multiImagesList.addAll(tempImageList)
                    filePathView!!.text = multiImagesList.joinToString(",")
                    getTotalImagesSize(
                        filePathView!!.text.split(
                            ","
                        ).toMutableList()
                    )
                    barcodeImageList.clear()
                    tempImageList.clear()
                    barcodeImageList.addAll(multiImagesList)
                    adapter.notifyDataSetChanged()
                }
                iAlert.dismiss()
            }

            internetImageRecyclerView.layoutManager =
                StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            internetImageRecyclerView.hasFixedSize()
            internetImageAdapter = InternetImageAdapter(
                requireActivity(),
                searchedImagesList as ArrayList<String>
            )
            internetImageRecyclerView.adapter = internetImageAdapter
            internetImageAdapter.setOnItemClickListener(object :
                InternetImageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedImage = searchedImagesList[position]
                    FullImageFragment(selectedImage).show(childFragmentManager, "full-image-dialog")
                }

                override fun onItemAttachClick(
                    btn: MaterialButton,
                    position: Int
                ) {
                    if (btn.text.toString()
                            .toLowerCase(Locale.ENGLISH) == "attach"
                    ) {


                        requireActivity().runOnUiThread {
//                                        //loader.visibility = View.VISIBLE
                            btn.text = requireActivity().resources.getString(R.string.please_wait)
                        }

                        val selectedImage = searchedImagesList[position]
                        val bitmap: Bitmap? = ImageManager.getBitmapFromURL(
                            requireActivity(),
                            selectedImage
                        )
                        if (bitmap != null) {
                            ImageManager.saveMediaToStorage(
                                requireActivity(),
                                bitmap,
                                object : ResponseListener {
                                    override fun onSuccess(result: String) {
                                        if (loader.visibility == View.VISIBLE) {
                                            loader.visibility = View.INVISIBLE
                                        }

                                        if (result.isNotEmpty()) {
                                            tempImageList.add(
                                                ImageManager.getRealPathFromUri(
                                                    requireActivity(),
                                                    Uri.parse(result)
                                                )!!
                                            )
                                            btn.text =
                                                requireActivity().resources.getString(R.string.attached_text)
                                            btn.setBackgroundColor(
                                                ContextCompat.getColor(
                                                    requireActivity(),
                                                    R.color.dark_gray
                                                )
                                            )
                                        } else {
                                            btn.text =
                                                requireActivity().resources.getString(R.string.attach_text)
                                            BaseActivity.showAlert(
                                                requireActivity(),
                                                requireActivity().resources.getString(R.string.something_wrong_error)
                                            )
                                        }
                                    }

                                })
                        } else {
//                                        if (loader.visibility == View.VISIBLE) {
//                                            loader.visibility = View.INVISIBLE
//                                        }
                            btn.text = requireActivity().resources.getString(R.string.attach_text)
                            BaseActivity.showAlert(
                                requireActivity(),
                                requireActivity().resources.getString(R.string.something_wrong_error)
                            )
                        }
                    } else {
                        btn.text = requireActivity().resources.getString(R.string.attach_text)
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primary_positive_color
                            )
                        )
                        tempImageList.removeAt(position)
                    }
                }

            })

            voiceSearchIcon.setOnClickListener {
                voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                val voiceLayout = LayoutInflater.from(context).inflate(R.layout.voice_language_setting_layout, null)
                val voiceLanguageSpinner = voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
                val voiceLanguageSaveBtn = voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

                if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                    voiceLanguageSpinner.setSelection(0,false)
                } else {
                    voiceLanguageSpinner.setSelection(1,false)
                }

                voiceLanguageSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(Locale.ENGLISH).contains("english")){"en"}else{"ru"}
                            appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setView(voiceLayout)
                val alert = builder.create();
                alert.show()
                voiceLanguageSaveBtn.setOnClickListener {
                    alert.dismiss()
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode)

                    }
                    voiceResultLauncher.launch(intent)
                }
            }

            searchBtnView.setOnClickListener {
//                var creditChargePrice: Float = 0F
//                if (searchBoxView.text.toString().trim().isNotEmpty()) {
//
//
//                    val firebaseDatabase = FirebaseDatabase.getInstance().reference
//                    firebaseDatabase.child("SearchImagesLimit")
//                        .addListenerForSingleValueEvent(object :
//                            ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                val creditPrice = snapshot.child("credits")
//                                    .getValue(Int::class.java) as Int
//                                val images = snapshot.child("images")
//                                    .getValue(Int::class.java) as Int
//                                creditChargePrice = creditPrice.toFloat() / images
//
//                                userCurrentCredits =
//                                    appSettings.getString(Constants.userCreditsValue) as String
//                                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
//                                    BaseActivity.hideSoftKeyboard(
//                                        requireActivity(),
//                                        searchBtnView
//                                    )
//                                    //Constants.hideKeyboar(requireActivity())
//                                    val query = searchBoxView.text.toString().trim()
//                                    requireActivity().runOnUiThread {
//                                        loader.visibility = View.VISIBLE
//                                    }
//
//                                    BaseActivity.searchInternetImages(
//                                        requireActivity(),
//                                        query,
//                                        object : APICallback {
//                                            override fun onSuccess(response: JSONObject) {
//                                                if (loader.visibility == View.VISIBLE) {
//                                                    loader.visibility =
//                                                        View.INVISIBLE
//                                                }
//
//                                                val items =
//                                                    response.getJSONArray("items")
//                                                if (items.length() > 0) {
//                                                    searchedImagesList.clear()
//                                                    for (i in 0 until items.length()) {
//                                                        val item =
//                                                            items.getJSONObject(i)
//                                                        if (item.has("link")) {
//                                                            searchedImagesList.add(
//                                                                item.getString(
//                                                                    "link"
//                                                                )
//                                                            )
//                                                        }
//                                                    }
//                                                    internetImageAdapter.notifyItemRangeChanged(
//                                                        0,
//                                                        searchedImagesList.size
//                                                    )
//
//                                                }
//                                                //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                                                val hashMap = HashMap<String, Any>()
//                                                val remaining =
//                                                    userCurrentCredits.toFloat() - creditChargePrice
//                                                Log.d("TEST199", "$remaining")
//                                                hashMap["credits"] =
//                                                    remaining.toString()
//                                                firebaseDatabase.child(Constants.firebaseUserCredits)
//                                                    .child(Constants.firebaseUserId)
//                                                    .updateChildren(hashMap)
//                                                    .addOnSuccessListener {
//                                                        BaseActivity.getUserCredits(
//                                                            requireActivity()
//                                                        )
//                                                    }
//                                                    .addOnFailureListener {
//
//                                                    }
//                                            }
//
//                                            override fun onError(error: VolleyError) {
//                                                if (loader.visibility == View.VISIBLE) {
//                                                    loader.visibility =
//                                                        View.INVISIBLE
//                                                }
//
//                                                BaseActivity.showAlert(
//                                                    requireActivity(),
//                                                    error.localizedMessage!!
//                                                )
//                                            }
//
//                                        })
//                                } else {
//                                    MaterialAlertDialogBuilder(requireActivity())
//                                        .setMessage(requireActivity().resources.getString(R.string.low_credites_error_message))
//                                        .setCancelable(false)
//                                        .setNegativeButton(requireActivity().resources.getString(R.string.no_text)) { dialog, which ->
//                                            dialog.dismiss()
//                                        }
//                                        .setPositiveButton(requireActivity().resources.getString(R.string.buy_credits)) { dialog, which ->
//                                            dialog.dismiss()
//                                            requireActivity().startActivity(
//                                                Intent(
//                                                    requireContext(),
//                                                    UserScreenActivity::class.java
//                                                )
//                                            )
//                                        }
//                                        .create().show()
//                                }
//
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//
//                            }
//
//                        })
//
//
//                } else {
//                    if (loader.visibility == View.VISIBLE) {
//                        loader.visibility = View.INVISIBLE
//                    }
//
//                    BaseActivity.showAlert(
//                        requireActivity(),
//                        requireActivity().resources.getString(R.string.empty_text_error)
//                    )
//                }
                startSearch(searchBoxView,searchBtnView,loader,searchedImagesList,internetImageAdapter)
            }

            searchBoxView.setOnEditorActionListener(object : TextView.OnEditorActionListener{
                override fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH){
                        startSearch(searchBoxView,searchBtnView,loader,searchedImagesList,internetImageAdapter)
                    }
                    return false
                }

            })
        }


        columns.addAll(tableGenerator.getTableColumns(tableName)!!.toList())
        Log.d("TEST1999", columns.toString())
        for (i in columns.indices) {
            val value = columns[i]
            if (value == "id" || value == "quantity") {
                continue
            } else if (value == "code_data") {
                textInputIdsList.add(Pair(value, codeDataTInputView!!))
                //codeDataTInputView!!.setText(text)
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
            filePathView!!.setText("")
            barcodeImageList.clear()
            adapter.notifyDataSetChanged()
            isScanResultDialogShowing = false
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

    private var voiceResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText: String =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results!![0]
                        }

                searchBoxView.setText(spokenText)
                Constants.hideKeyboar(requireActivity())
                startSearch(searchBoxView,searchBtnView,loader,
                    searchedImagesList as ArrayList<String>,internetImageAdapter)
            }
        }

    private fun startSearch(searchBoxView:TextInputEditText,searchBtnView:ImageButton,loader:ProgressBar,searchedImagesList:ArrayList<String>,internetImageAdapter:InternetImageAdapter){
        var creditChargePrice: Float = 0F
        if (searchBoxView.text.toString().trim().isNotEmpty()) {


            val firebaseDatabase = FirebaseDatabase.getInstance().reference
            firebaseDatabase.child("SearchImagesLimit")
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val creditPrice = snapshot.child("credits")
                            .getValue(Int::class.java) as Int
                        val images = snapshot.child("images")
                            .getValue(Int::class.java) as Int
                        creditChargePrice = creditPrice.toFloat() / images

                        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice)
                        {
                            BaseActivity.hideSoftKeyboard(
                                requireActivity(),
                                searchBtnView
                            )
                            //Constants.hideKeyboar(requireActivity())
                            val query = searchBoxView.text.toString().trim()
                            requireActivity().runOnUiThread {
                                loader.visibility = View.VISIBLE
                            }

                            BaseActivity.searchInternetImages(
                                requireActivity(),
                                query,
                                object : APICallback {
                                    override fun onSuccess(response: JSONObject) {
                                        if (loader.visibility == View.VISIBLE) {
                                            loader.visibility =
                                                View.INVISIBLE
                                        }

                                        val items =
                                            response.getJSONArray("items")
                                        if (items.length() > 0) {
                                            searchedImagesList.clear()
                                            for (i in 0 until items.length()) {
                                                val item =
                                                    items.getJSONObject(i)
                                                if (item.has("link")) {
                                                    searchedImagesList.add(
                                                        item.getString(
                                                            "link"
                                                        )
                                                    )
                                                }
                                            }
                                            internetImageAdapter.notifyItemRangeChanged(
                                                0,
                                                searchedImagesList.size
                                            )
                                            internetImageDoneBtn.visibility = View.VISIBLE
                                        }
                                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                                        val hashMap = HashMap<String, Any>()
                                        val remaining =
                                            userCurrentCredits.toFloat() - creditChargePrice
                                        Log.d("TEST199", "$remaining")
                                        hashMap["credits"] =
                                            remaining.toString()
                                        firebaseDatabase.child(Constants.firebaseUserCredits)
                                            .child(Constants.firebaseUserId)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener {
                                                BaseActivity.getUserCredits(
                                                    requireActivity()
                                                )
                                            }
                                            .addOnFailureListener {

                                            }
                                    }

                                    override fun onError(error: VolleyError) {
                                        if (loader.visibility == View.VISIBLE) {
                                            loader.visibility =
                                                View.INVISIBLE
                                        }

                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            error.localizedMessage!!
                                        )
                                    }

                                })
                        } else
                        {
                            MaterialAlertDialogBuilder(requireActivity())
                                .setMessage(getString(R.string.low_credites_error_message))
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.no_text)){dialog,which->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(getString(R.string.buy_credits)){dialog,which ->
                                    dialog.dismiss()
                                    startActivity(Intent(context,UserScreenActivity::class.java))
                                }
                                .create().show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


        } else {
            if (loader.visibility == View.VISIBLE) {
                loader.visibility = View.INVISIBLE
            }

            BaseActivity.showAlert(
                requireActivity(),
                getString(R.string.empty_text_error)
            )
        }
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
                                                if (tableName.contains("import")) {
                                                    getTableDataFromCsv(tableName, "", "")
                                                } else {
                                                    getTableData(tableName, "", "")
                                                }
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
                                filePathView!!.setText("")
                                isScanResultDialogShowing = false
                                barcodeImageList.clear()
                                adapter.notifyDataSetChanged()
                                if (tableName.contains("import")) {
                                    getTableDataFromCsv(tableName, "", "")
                                } else {
                                    getTableData(tableName, "", "")
                                }
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
//                            openHistoryBtnTip()
                            val bundle = Bundle()
                            bundle.putString("success", "success")
                            mFirebaseAnalytics?.logEvent("scanner", bundle)
                            if (tableName.contains("import")) {
                                getTableDataFromCsv(tableName, "", "")
                            } else {
                                getTableData(tableName, "", "")
                            }
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

    private fun saveSuccessScans() {
        var scans = DialogPrefs.getSuccessScan(requireActivity())
        if (scans >= 0) {
            scans += 1
            DialogPrefs.setSuccessScan(requireActivity(), scans)
        }
        Log.d("TAG", "ScanCount: $scans")
    }

    private fun uploadImageOnFirebaseStorage(listener: UploadImageCallback) {
        BaseActivity.startLoading(requireActivity())
        val stringRequest = object : StringRequest(
            Method.POST, "https://itmagic.app/api/get_user_packages.php",
            Response.Listener {
                val response = JSONObject(it)
                if (response.getInt("status") == 200) {
                    BaseActivity.dismiss()
                    if (response.has("package") && !response.isNull("package")) {
                        val packageDetail: JSONObject? = response.getJSONObject("package")

                        val availableSize = packageDetail!!.getString("size")
                        Constants.userServerAvailableStorageSize = availableSize
                        appSettings.putString(Constants.memory, availableSize)
                        val endDate = packageDetail.getString("end_date")
                        val expiredTimeMili =
                            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate)!!.time

                        var currentStorageSize: Float = 0F
                        if (availableSize.isNotEmpty()) {
                            currentStorageSize =
                                Constants.convertMegaBytesToBytes(availableSize.toFloat())
                            if (totalImageSize <= currentStorageSize) {
                                val currentMiliSeconds = System.currentTimeMillis()
                                if (expiredTimeMili >= currentMiliSeconds) {
                                    val imageList = filePathView!!.text.toString()
                                    if (imageList.contains(",")) {
                                        array = imageList.split(",")
                                        uploadImage(listener)
                                    } else {
                                        val bundle = Bundle()
                                        bundle.putString("starts", "starts")
                                        mFirebaseAnalytics?.logEvent("upload image", bundle)

                                        if (FirebaseAuth.getInstance().currentUser != null) {
                                            val userId =
                                                FirebaseAuth.getInstance().currentUser!!.uid

                                            val imageBase64String =
                                                ImageManager.convertImageToBase64(
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
                                } else {
                                    BaseActivity.showAlert(
                                        requireActivity(),
                                        requireActivity().resources.getString(R.string.subscription_expired_text)
                                    )
                                }
                            } else {
                                BaseActivity.showAlert(
                                    requireActivity(),
                                    requireActivity().resources.getString(R.string.insufficient_storage_error_message)
                                )
                            }
                        }


                    }
                }
            }, Response.ErrorListener {
                BaseActivity.dismiss()
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

    private fun uploadImage(callback: UploadImageCallback) {
        BaseActivity.startLoading(requireActivity())
        if (FirebaseAuth.getInstance().currentUser != null) {

            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val imagePath = array[index]
            val imageBase64String = ImageManager.convertImageToBase64(requireActivity(), imagePath)
            Handler(Looper.myLooper()!!).postDelayed({
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
                                BaseActivity.dismiss()
                                callback.onSuccess("")
                            } else {
                                index++
                                uploadImage(callback)
                            }
                        }

                    })
            }, 1000)
        }
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
                        val stringRequest = object : StringRequest(
                            Method.POST, "https://itmagic.app/api/get_user_packages.php",
                            Response.Listener {
                                val updatedResponse = JSONObject(it)
                                if (updatedResponse.getInt("status") == 200) {
                                    if (updatedResponse.has("package") && !updatedResponse.isNull("package")) {
                                        val packageDetail: JSONObject? =
                                            updatedResponse.getJSONObject("package")
                                        if (packageDetail != null) {
                                            val availableSize = packageDetail.getString("size")
                                            Constants.userServerAvailableStorageSize = availableSize
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

                    override fun onError(error: VolleyError) {

                    }

                })
        } else {
            val stringRequest = object : StringRequest(
                Method.POST, "https://itmagic.app/api/get_user_packages.php",
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

    private fun openDialog() {
        if (updateInputBox != null) {
            updateInputBox!!.clearFocus()
            customAlertDialog = CustomAlertDialog()
            customAlertDialog!!.setFocusListener(this)
            customAlertDialog!!.show(childFragmentManager, "dialog")
        } else {
            Toast.makeText(
                requireActivity(),
                requireActivity().resources.getString(R.string.input_box_focused_error_text),
                Toast.LENGTH_SHORT
            ).show()
        }
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

    private fun cropImage(imageUri: Uri) {

        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(requireActivity())
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

    private fun getImageFromGallery() {
        if (galleryIntentType == 0) {
            val fileIntent = Intent(Intent.ACTION_PICK)
            fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            fileIntent.type = "image/*"
            resultLauncher.launch(fileIntent)
        } else {
            val fileIntent = Intent(Intent.ACTION_PICK)
            fileIntent.type = "image/*"
            resultLauncher.launch(fileIntent)
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (galleryIntentType == 0) {
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
                } else {
                    val data: Intent? = result.data
                    val path = ImageManager.getRealPathFromUri(requireActivity(), data!!.data!!)
                    val bitmap = ImageManager.getBitmapFromURL(
                        requireActivity(),
                        path
                    )

                    if (bitmap != null) {
                        BaseActivity.startLoading(requireActivity())
                        ImageManager.getTextFromBarcodeImage(
                            requireActivity(),
                            bitmap,
                            object : ResponseListener {
                                override fun onSuccess(result: String) {
                                    BaseActivity.dismiss()
                                    if (result.isNotEmpty() && !result.contains("ERROR")) {
                                        //displayDataSubmitDialog(null, result)
                                    } else {
                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            requireActivity().resources.getString(R.string.barcode_scan_image_error)
                                        )
                                    }
                                }

                            })
                    }
                }
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

    private fun getTotalImagesSize(uploadedUrlList: MutableList<String>) {
        //var total: Long = 0
        totalImageSize = 0
        for (i in 0 until uploadedUrlList.size) {
            Handler(Looper.myLooper()!!).postDelayed({
                lifecycleScope.launch {
                    totalImageSize += if (Constants.compressorFeatureStatus == 0) {
                        //val compressedImageFile = Compressor.compress(requireActivity(), File(uploadedUrlList[i]))
                        ImageManager.getFileSize(File(uploadedUrlList[i]).absolutePath)
                    } else {
                        val compressedImageFile =
                            Compressor.compress(requireActivity(), File(uploadedUrlList[i]))
                        ImageManager.getFileSize(compressedImageFile.absolutePath)
                    }
                    Log.d("TEST199TOTALSIZE", "$totalImageSize")

                }
            }, (1000 * i + 1).toLong())
        }
//        return totalImageSize
    }

    private fun importCsv() {
        openFilePicker()
    }

    private fun openQuickEditDialog(item: TableObject) {

        val quickEditParentLayout =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.update_quick_edit_table_layout, null)
        val cancelDialogBtn =
            quickEditParentLayout.findViewById<MaterialButton>(R.id.quick_edit_dialog_cancel_btn)
        val updateDialogBtn =
            quickEditParentLayout.findViewById<MaterialButton>(R.id.quick_edit_dialog_update_btn)
        val quickEditWrapperLayout =
            quickEditParentLayout.findViewById<LinearLayout>(R.id.quick_edit_parent_layout)

        val codeDataLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
        val codeDataValue =
            codeDataLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
        val codeDataClearBrushView =
            codeDataLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        codeDataClearBrushView.id = counter
        codeDataClearBrushView.tag = "qe"

        barcodeEditList.add(
            Triple(
                codeDataValue,
                codeDataClearBrushView,
                "code_data"
            )
        )
        codeDataClearBrushView.setOnClickListener(this)
        codeDataValue.setText(item.code_data)
        codeDataValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDialogBtn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        quickEditWrapperLayout.addView(codeDataLayout)
        val dateLayout = LayoutInflater.from(context)
            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
        val dateValue =
            dateLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
        val dateClearBrushView =
            dateLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        counter += 1
        dateClearBrushView.id = counter
        dateClearBrushView.tag = "qe"

        barcodeEditList.add(Triple(dateValue, dateClearBrushView, "date"))
        dateClearBrushView.setOnClickListener(this)
        dateValue.setText(item.date)
        dateValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDialogBtn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        quickEditWrapperLayout.addView(dateLayout)
//        val imageLayout = LayoutInflater.from(context)
//            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
//        val imageValue =
//            imageLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
//        val imageClearBrushView =
//            imageLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        counter += 1
//        imageClearBrushView.id = counter
//        imageClearBrushView.tag = "qe"
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(10, 5, 5, 5)
        }
        if (item.image.isNotEmpty()) {
            if (item.image.contains(" ")) {
                imageList.addAll(item.image.split(" ").toList())
            } else {
                imageList.add(item.image)
            }


            val barcodeImageRecyclerView = RecyclerView(requireActivity())
            barcodeImageRecyclerView.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.light_gray
                )
            )
            barcodeImageRecyclerView.layoutParams = params
            barcodeImageRecyclerView.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            barcodeImageRecyclerView.hasFixedSize()
            val adapter = BarcodeImageAdapter(
                requireActivity(),
                imageList as ArrayList<String>
            )
            barcodeImageRecyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : BarcodeImageAdapter.OnItemClickListener {
                override fun onItemDeleteClick(position: Int) {
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setMessage(getString(R.string.delete_barcode_image_message))
                    builder.setCancelable(false)
                    builder.setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.setPositiveButton(getString(R.string.yes_text)) { dialog, which ->
                        dialog.dismiss()
                        imageList.removeAt(position)

                        tableGenerator.updateBarcodeDetail(
                            tableName, "image", if (imageList.size > 0) {
                                imageList.joinToString(",")
                            } else {
                                ""
                            }, item.id
                        )
                        adapter.notifyItemRemoved(position)
                        if (tableName.contains("import")) {
                            getTableDataFromCsv(tableName, "", "")
                        } else {
                            getTableData(tableName, "", "")
                        }

                    }
                    val alert = builder.create()
                    alert.show()
                }

                override fun onAddItemEditClick(position: Int) {

                }

                override fun onImageClick(position: Int) {
                    val url = imageList[position]
                    BaseActivity.openLink(requireActivity(), url)
                }

            })
            quickEditWrapperLayout.addView(barcodeImageRecyclerView)
        } else {
            val emptyTextView = MaterialTextView(requireActivity())
            emptyTextView.layoutParams = params
            emptyTextView.text = getString(R.string.empty_image_list_error_message)
            emptyTextView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.dark_gray))
            emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
            quickEditWrapperLayout.addView(emptyTextView)
        }
//        imageListTextView.setOnClickListener {
//            val images = tableGenerator.getBarcodeImages(tableName, item.id)
//            if (images.isNotEmpty()) {
//                startActivity(Intent(context, BarcodeImageListActivity::class.java).apply {
//                    putExtra("TABLE_NAME", tableName)
//                    putExtra("ID", item.id)
//                })
//            }
//            else{
//                Toast.makeText(context,getString(R.string.empty_barcode_image_message),Toast.LENGTH_SHORT).show()
//            }
//        }
//        barcodeEditList.add(Triple(imageValue, imageClearBrushView, "image"))
//        imageClearBrushView.setOnClickListener(this)
//        imageValue.setText(item.image)
//        imageValue.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                updateDialogBtn.isEnabled = true
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//        })


        val quantityLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
        val quantityValue =
            quantityLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
        val quantityClearBrushView =
            quantityLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        counter += 1
        quantityClearBrushView.id = counter
        quantityClearBrushView.tag = "qe"

        barcodeEditList.add(Triple(quantityValue, quantityClearBrushView, "quantity"))
        quantityClearBrushView.setOnClickListener(this)
        quantityValue.setText("${item.quantity}")
        quantityValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDialogBtn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        quickEditWrapperLayout.addView(quantityLayout)

        for (i in 0 until item.dynamicColumns.size) {
            val item1 = item.dynamicColumns[i]

            val layout = LayoutInflater.from(requireActivity())
                .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
            val value =
                layout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
            val clearBrushView =
                layout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
            counter += 1
            clearBrushView.id = counter
            clearBrushView.tag = "qe"

            barcodeEditList.add(Triple(value, clearBrushView, item1.first))
            clearBrushView.setOnClickListener(this)
            value.setText(item1.second)
            value.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateDialogBtn.isEnabled = true
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
            quickEditWrapperLayout.addView(layout)

        }
        counter = 0

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(quickEditParentLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelDialogBtn.setOnClickListener {
            imageList.clear()
            alert.dismiss()
        }
        updateDialogBtn.setOnClickListener {
            BaseActivity.startLoading(requireActivity())
            var flag = false

            for (i in 0 until barcodeEditList.size) {
                val triple = barcodeEditList[i]
                val value = triple.first.text.toString().trim()
                if (value.isEmpty()) {
                    flag = false
                    detailList.clear()
                    break
                } else {
                    flag = true
                    detailList.add(Pair(triple.third, value))
                }
            }
            if (flag) {
                alert.dismiss()
                if (detailList.isNotEmpty()) {
                    val isSuccess = tableGenerator.updateData(tableName, detailList, item.id)
                    if (isSuccess) {
                        imageList.clear()
                        BaseActivity.dismiss()
                        getTableData(tableName, "", "")
                    } else {
                        BaseActivity.dismiss()
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.database_update_failed_error)
                        )
                    }
                }
            } else {
                BaseActivity.dismiss()
                BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
            }

        }

    }

    private fun openQuickEditDialogCsv(item: List<Pair<String, String>>) {

        val quickEditParentLayout =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.update_quick_edit_table_layout, null)
        val cancelDialogBtn =
            quickEditParentLayout.findViewById<MaterialButton>(R.id.quick_edit_dialog_cancel_btn)
        val updateDialogBtn =
            quickEditParentLayout.findViewById<MaterialButton>(R.id.quick_edit_dialog_update_btn)
        val quickEditWrapperLayout =
            quickEditParentLayout.findViewById<LinearLayout>(R.id.quick_edit_parent_layout)


        for (i in 0 until item.size) {
            if (i == 0) {
                continue
            }
            val item1 = item[i]
            val layout = LayoutInflater.from(requireActivity())
                .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
            val columnHeadingView =
                layout.findViewById<MaterialTextView>(R.id.quick_edit_barcode_heading_text_view)
            val value =
                layout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
            val clearBrushView =
                layout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
            counter += 1
            clearBrushView.id = counter
            clearBrushView.tag = "qe"
            columnHeadingView.text = item1.first.toUpperCase(Locale.ENGLISH)
            columnHeadingView.visibility = View.VISIBLE
            barcodeEditList.add(Triple(value, clearBrushView, item1.first))
            clearBrushView.setOnClickListener(this)
            value.setText(item1.second)
            value.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateDialogBtn.isEnabled = true
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
            quickEditWrapperLayout.addView(layout)

        }
        counter = 0

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(quickEditParentLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelDialogBtn.setOnClickListener {
            alert.dismiss()
        }
        updateDialogBtn.setOnClickListener {
            BaseActivity.startLoading(requireActivity())
            var flag = false

            for (i in 0 until barcodeEditList.size) {
                val triple = barcodeEditList[i]
                val value = triple.first.text.toString().trim()
                if (value.isEmpty()) {
                    //flag = false
                    detailList.add(Pair(triple.third, ""))
                    //break
                } else {
                    //flag = true
                    detailList.add(Pair(triple.third, value))
                }
            }
            if (updateDialogBtn.isEnabled) {
                alert.dismiss()
                if (detailList.isNotEmpty()) {
                    val isSuccess =
                        tableGenerator.updateDataCsv(tableName, detailList, item[0].second.toInt())
                    if (isSuccess) {
                        imageList.clear()
                        BaseActivity.dismiss()
                        if (tableName.contains("import")) {
                            getTableDataFromCsv(tableName, "", "")
                        } else {
                            getTableData(tableName, "", "")
                        }

                    } else {
                        BaseActivity.dismiss()
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.database_update_failed_error)
                        )
                    }
                }
            } else {
                BaseActivity.dismiss()
                BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
            }

        }

    }

    private fun exportCsv(tableName: String) {
        if (dataList.isNotEmpty()) {
            BaseActivity.startLoading(requireActivity())
            val columns = tableGenerator.getTableColumns(tableName)
            val builder = StringBuilder()
            builder.append(columns!!.joinToString(","))

            for (j in 0 until dataList.size) {
                var image = ""
                val data = dataList[j]
                image = if (data.image.contains(" ")) {
                    val temp = data.image.replace(",", ", ")
                    "\"$temp\""
                } else {
                    data.image
                }
                builder.append("\n${data.id},${data.code_data},${data.date},$image,${data.quantity}")
                if (data.dynamicColumns.size > 0) {
                    for (k in 0 until data.dynamicColumns.size) {
                        val item = data.dynamicColumns[k]
                        if (k != data.dynamicColumns.size) {
                            builder.append(",")
                        }
                        builder.append(item.second)
                    }
                }
            }

            try {

                val out = requireActivity().openFileOutput("$tableName.csv", Context.MODE_PRIVATE)
                out.write((builder.toString()).toByteArray())
                out.close()

                val file = File(requireActivity().filesDir, "$tableName.csv")
                val path =
                    FileProvider.getUriForFile(
                        requireActivity(),
                        requireActivity().applicationContext.packageName + ".fileprovider",
                        file
                    )
                BaseActivity.dismiss()
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/csv"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(intent, "Share with"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            BaseActivity.showAlert(requireActivity(), getString(R.string.table_export_error_text))
        }
    }

    private fun exportCsv1(tableName: String) {
        if (dataListCsv.isNotEmpty()) {
            BaseActivity.startLoading(requireActivity())
            val columns = mutableListOf<String>()
            columns.addAll(tableGenerator.getTableColumns(tableName)!!.toList())
            val builder = StringBuilder()
            if (columns.isNotEmpty()) {
                if (columns[0].toLowerCase(Locale.ENGLISH) == "_id") {
                    columns.removeAt(0)
                }

                val columnString = columns.joinToString(",")

                builder.append(columnString)
            }
            for (j in 0 until dataListCsv.size) {

                val data = dataListCsv[j]
                if (data.isNotEmpty()) {
                    builder.append("\n")
                    for (k in data.indices) {
                        if (k == 0) {
                            continue
                        }
                        var temp = ""
                        val item = data[k]
                        temp = if (item.second.contains(",")) {
                            "\"${item.second}\""
                        } else {
                            item.second
                        }
                        builder.append(temp)
                        if (k != data.size) {
                            builder.append(",")
                        }
                    }
                }
            }
            try {

                val dir = File(requireActivity().filesDir, "ExportedCsv")
                dir.mkdirs()
                val file = File(dir, "$tableName.csv")

                val fw = FileWriter(file.absolutePath)
                fw.append(builder.toString())
                fw.close()

                val path =
                    FileProvider.getUriForFile(
                        requireActivity(),
                        requireActivity().applicationContext.packageName + ".fileprovider",
                        file
                    )
                BaseActivity.dismiss()
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/csv"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(intent, "Share with"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            BaseActivity.showAlert(requireActivity(), getString(R.string.table_export_error_text))
        }
    }

    fun restart() {
        onResume()
    }

    override fun onResume() {
        super.onResume()
        getTableList()
        displayTableList()

    }

    private fun getTableData(tName: String, column: String, order: String) {
        Log.d("TEST199TABLEDATA","true")
        var tempList: List<TableObject>? = null
        CoroutineScope(Dispatchers.IO).launch {
            tempList = tableGenerator.getTableDate(tName, column, order)
            if (tempList!!.isNotEmpty()) {
                dataList.clear()
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            if (tableMainLayout.childCount > 0) {
                tableMainLayout.removeViews(0, tableMainLayout.childCount - 1)
            }

            dataList.addAll(tempList!!)
            tableMainLayout.weightSum = dataList.size * 2F

            if (dataList.isNotEmpty()) {
                BaseActivity.startLoading(requireActivity())
                for (j in 0 until dataList.size) {

                    val textViewIdLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_row_cell, null)
                    val textViewId =
                        textViewIdLayout.findViewById<MaterialTextView>(R.id.cell_value)
                    val data = dataList[j]
                    val tableRow = TableRow(requireActivity())
                    tableRow.id = j
                    tableRow.tag = "row"
                    tableRow.setOnClickListener(this@ScanFragment)

                    val moreLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_more_option_layout, null)
                    moreLayout.layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val moreImage =
                        moreLayout.findViewById<AppCompatImageView>(R.id.cell_more_image)
                    moreImage.id = j
                    moreImage.tag = "more"
                    moreImage.setOnClickListener(this@ScanFragment)
                    tableRow.addView(moreLayout)

                    textViewId.text = "${data.id}"
                    tableRow.addView(textViewIdLayout)
                    val textViewCodeDateLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_row_cell, null)
                    textViewCodeDateLayout.layoutParams = layoutParams
                    val textViewCodeDate =
                        textViewCodeDateLayout.findViewById<MaterialTextView>(R.id.cell_value)
                    textViewCodeDate.text = data.code_data
                    tableRow.addView(textViewCodeDateLayout)

                    val textViewDateLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_row_cell, null)
                    textViewDateLayout.layoutParams = layoutParams
                    val textViewDate =
                        textViewDateLayout.findViewById<MaterialTextView>(R.id.cell_value)
                    textViewDate.text = data.date
                    tableRow.addView(textViewDateLayout)

                    val textViewImageLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_row_cell, null)
                    textViewImageLayout.layoutParams = layoutParams
                    val textViewImage =
                        textViewImageLayout.findViewById<MaterialTextView>(R.id.cell_value)

                    if (data.image.isNotEmpty() && data.image.length >= 20) {
                        textViewImage.text = data.image.substring(0, 20)
                    } else {
                        textViewImage.text = data.image
                    }
                    tableRow.addView(textViewImageLayout)

                    val textViewQuantityLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_row_cell, null)
                    textViewQuantityLayout.layoutParams = layoutParams
                    val textViewQuantity =
                        textViewQuantityLayout.findViewById<MaterialTextView>(R.id.cell_value)
                    textViewQuantity.text = "${data.quantity}"

                    tableRow.addView(textViewQuantityLayout)

                    if (data.dynamicColumns.size > 0) {
                        for (k in 0 until data.dynamicColumns.size) {
                            val item = data.dynamicColumns[k]
                            val cell =
                                LayoutInflater.from(requireActivity())
                                    .inflate(R.layout.table_row_cell, null)
                            cell.layoutParams = layoutParams
                            val textV = cell.findViewById<MaterialTextView>(R.id.cell_value)

                            textV.text = item.second
                            tableRow.addView(cell)
                        }

                    }
                    if (j % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#EAEAF6"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#f2f2f2"))
                    }
                    tableMainLayout.addView(tableRow)
                }
                BaseActivity.dismiss()
            }
        }


    }

    private fun getTableDataFromCsv(tName: String, column: String, order: String) {
        var tempList: List<List<Pair<String, String>>>? = null
        CoroutineScope(Dispatchers.IO).launch {
            tempList = tableGenerator.getTableDateFromCsv(tName, column, order)
            if (tempList!!.isNotEmpty()) {
                dataListCsv.clear()
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            if (tableMainLayout.childCount > 0) {
                tableMainLayout.removeViews(0, tableMainLayout.childCount - 1)
            }

            dataListCsv.addAll(tempList!!)
            tableMainLayout.weightSum = dataListCsv.size * 2F

            if (dataListCsv.isNotEmpty()) {
                BaseActivity.startLoading(requireActivity())
                for (j in 0 until dataListCsv.size) {

                    val listPair = dataListCsv[j]

                    val tableRow = TableRow(requireActivity())
                    tableRow.id = j
                    tableRow.tag = "row"
                    tableRow.setOnClickListener(this@ScanFragment)

                    val moreLayout =
                        LayoutInflater.from(requireActivity())
                            .inflate(R.layout.table_more_option_layout, null)
                    moreLayout.layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val moreImage =
                        moreLayout.findViewById<AppCompatImageView>(R.id.cell_more_image)
                    moreImage.id = j
                    moreImage.tag = "more"
                    moreImage.setOnClickListener(this@ScanFragment)
                    tableRow.addView(moreLayout)

                    if (listPair.isNotEmpty()) {
                        for (i in 0 until listPair.size) {

                            val item = listPair[i]
                            val cell =
                                LayoutInflater.from(requireActivity())
                                    .inflate(R.layout.table_row_cell, null)
                            cell.layoutParams = layoutParams
                            val textV = cell.findViewById<MaterialTextView>(R.id.cell_value)

                            if (item.second.length > 8) {
                                textV.text = "${item.second.substring(0, 9)}..."
                            } else {
                                textV.text = item.second.trim()
                            }
                            tableRow.addView(cell)
                        }
                    }

                    if (j % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#EAEAF6"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#f2f2f2"))
                    }
                    tableMainLayout.addView(tableRow)
                }
                BaseActivity.dismiss()
            }

        }

    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/*"
        fileResultLauncher.launch(intent)
    }

    private var fileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val filePath = result.data!!.data
                try {
                    val file = FileUtil.from(requireActivity(), filePath!!)
                    val ext = file.name.substring(file.name.lastIndexOf(".") + 1)
                    var fileName = file.name.substring(0, file.name.lastIndexOf("."))
                    fileName = fileName.replace("[-+.^:,]".toRegex(), " ").replace(" ", "_")
                        .replace("[", "").replace("]", "").trim()
                    if (ext != "csv") {
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.csv_file_chooser_error_message_text)
                        )
                    } else {
                        try {
                            BaseActivity.startLoading(requireActivity())
                            val listContents = CSVFile.readFile(requireActivity(), file)
                            Log.d("TEST199", "$listContents")
//                            var nextLine: Array<String>
//                            var counter = 0
                            val columnsList = mutableListOf<String>()
                            var tableData = mutableListOf<Pair<String, String>>()
                            val listRecord = mutableListOf<List<Pair<String, String>>>()
                            val createdTableName = "${fileName}_import"
//                            var tempLine:String = ""
//
//                            BaseActivity.startLoading(requireActivity())
//                            //val reader = CSVReader(FileReader(file))
//                            val reader = BufferedReader(
//                                InputStreamReader(
//                                    file.inputStream(), Charset.forName(
//                                        "UTF-8"
//                                    )
//                                )
//                            )
//                            var line = reader.readLine()
//                            while (line != null) {
//
//                                line = String(line.toByteArray(), Charset.forName("UTF-8"))
//                                if (line.toString().contains("\t".toRegex())){
//                                    tempLine = line.replace("\t".toRegex(), ",")
//                                }
//                                else{
//                                    tempLine = line
//                                }
//
                            val row = listContents[0]
                            val originalColumns = listContents[0]
//
//                                if (counter == 0) {
//                            if (checkCyrillicCharacter(row.joinToString(","))){
//                                for (i in row.indices) {
//                                    columnsList.add(
//                                        Constants.transLit(row[i]!!.trim()).replace(
//                                            "[-+.^:,?'()]".toRegex(),
//                                            ""
//                                        ).replace(" ", "_").toLowerCase(
//                                            Locale.ENGLISH
//                                        )
//                                    )
//                                }
//                            }
//                            else{
                            for (i in row.indices) {
                                columnsList.add(
                                    row[i]!!.trim().replace(
                                        "[-+.^:,?'()]".toRegex(),
                                        ""
                                    ).replace(" ", "_").toLowerCase(
                                        Locale.ENGLISH
                                    )
                                )
                            }
//                            }


                            for (j in 1 until listContents.size) {
                                val row1 = listContents[j]
                                for (k in row1.indices) {
                                    var data = row1[k]!!
                                    if (data.contains("|")) {
                                        data = data.replace("|", ",")
                                    }
                                    tableData.add(Pair(columnsList[k], data.trim()))
                                }
                                listRecord.add(tableData)
                                tableData = mutableListOf()
                            }
//
//                                    counter += 1
//                                    line = reader.readLine()
//                                    continue
//                                }
//                                if (row.isNotEmpty() && columnsList.size == row.size) {
//                                    for (j in row.indices) {
//                                        tableData.add(Pair(columnsList[j], row[j].trim()))
//                                    }
//                                    listRecord.add(tableData)
//                                    tableData = mutableListOf()
//                                    counter += 1
//                                } else {
//                                    break
//                                }
//                                if (reader.readLine() == null) {
//                                    break
//                                }
//                                else{
//                                    line = reader.readLine()
//                                }
//
//                            }

                            if (createdTableName.isNotEmpty() && listRecord.isNotEmpty()) {
                                val isFound = tableGenerator.tableExists(createdTableName)
                                if (isFound) {
                                    BaseActivity.dismiss()
                                    BaseActivity.showAlert(
                                        requireActivity(),
                                        getString(R.string.table_already_exist_message)
                                    )
                                } else {

                                    tableGenerator.createTableFromCsv(
                                        createdTableName,
                                        columnsList as ArrayList<String>
                                    )

                                    Handler(Looper.myLooper()!!).postDelayed({

                                        val isExist = tableGenerator.tableExists(createdTableName)
                                        if (isExist) {
                                            displayTableList()
                                            for (j in 0 until listRecord.size) {
                                                tableGenerator.insertData(
                                                    createdTableName,
                                                    listRecord[j]
                                                )
                                            }
                                            BaseActivity.dismiss()
                                            tableName = createdTableName
                                            appSettings.putString("SCAN_SELECTED_TABLE", tableName)
                                            getTableList()
                                            displayTableData()
//                                            if (checkCyrillicCharacter(originalColumns.joinToString(","))){
//                                                tableGenerator.insertExportColumns(tableName,originalColumns.joinToString(","))
//                                            }
                                            BaseActivity.showAlert(
                                                requireActivity(),
                                                getString(R.string.table_created_success_message)
                                            )
                                        } else {
                                            BaseActivity.dismiss()
                                            BaseActivity.showAlert(
                                                requireActivity(),
                                                getString(R.string.table_created_failed_message)
                                            )
                                        }
                                    }, 5000)
                                }
                            } else {
                                BaseActivity.dismiss()
                                BaseActivity.showAlert(
                                    requireActivity(),
                                    getString(R.string.table_csv_import_error_message)
                                )
                            }

                        } catch (e: IOException) {
                            BaseActivity.dismiss()
                            BaseActivity.showAlert(
                                requireActivity(),
                                "${getString(R.string.table_csv_import_error_message)}\n${e.localizedMessage}\n${
                                    getString(
                                        R.string.csv_correct_error_message
                                    )
                                }"
                            )
                            e.printStackTrace()
                        }
//                        try {
//                            var counter = 0
//                            val columnsList = mutableListOf<String>()
//                            var tableData = mutableListOf<Pair<String, String>>()
//                            val listRecord = mutableListOf<List<Pair<String,String>>>()
//                            val tableName = "${fileName}_import"
//
//                            BaseActivity.startLoading(requireActivity())
//                            val reader = CSVReader(FileReader(file))
//                            var line: Array<String>? = reader.readNext()
//
//                            if (line != null){
//
//                                TranslatorManager.translate(line.joinToString(","),object : TranslationCallback{
//                                    override fun onTextTranslation(translatedText: String) {
//                                        if (translatedText.isNotEmpty()){
//                                            val array = translatedText.split(",")
//                                            val translatedColumnText = mutableListOf<String>()
//                                            for (i in 0 until array.size) {
//                                                translatedColumnText.add(array[i].trim().replace("[-+.^:,?()]".toRegex(), "").replace(" ","_").trim())
//                                            }
//
//                                            while (line != null) {
//                                                // nextLine[] is an array of values from the line
//                                                if (counter == 0) {
//
//                                                    counter += 1
//                                                    line = reader.readNext()
//                                                    continue
//                                                }
//                                                if (line!!.isNotEmpty() && translatedColumnText.size == line!!.size) {
//                                                    for (j in 0 until line!!.size) {
//                                                        tableData.add(Pair(translatedColumnText[j], line!![j]))
//                                                    }
//                                                    listRecord.add(tableData)
//                                                    tableData = mutableListOf()
//                                                    counter += 1
//                                                } else {
//                                                    break
//                                                }
//                                                if (reader.readNext() == null) {
//                                                    break
//                                                }
//                                                line = reader.readNext()
//                                            }
//
//
//                                            if (tableName.isNotEmpty() && listRecord.isNotEmpty()) {
//                                                val isFound = tableGenerator.tableExists(tableName)
//                                                if (isFound) {
//                                                    BaseActivity.dismiss()
//                                                    BaseActivity.showAlert(
//                                                        requireActivity(),
//                                                        getString(R.string.table_already_exist_message)
//                                                    )
//                                                } else {
//
//                                                    tableGenerator.createTableFromCsv(
//                                                        tableName,
//                                                        translatedColumnText as ArrayList<String>
//                                                    )
//
//                                                    Handler(Looper.myLooper()!!).postDelayed({
//
//                                                        val isExist = tableGenerator.tableExists(tableName)
//                                                        if (isExist) {
//                                                            displayTableList()
//                                                            for (j in 0 until listRecord.size){
//                                                                tableGenerator.insertData(tableName, listRecord[j])
//                                                            }
//                                                            BaseActivity.dismiss()
//                                                            BaseActivity.showAlert(
//                                                                requireActivity(),
//                                                                getString(R.string.table_created_success_message)
//                                                            )
//                                                        } else {
//                                                            BaseActivity.dismiss()
//                                                            BaseActivity.showAlert(
//                                                                requireActivity(),
//                                                                getString(R.string.table_created_failed_message)
//                                                            )
//                                                        }
//                                                    }, 5000)
//                                                }
//                                            } else {
//                                                BaseActivity.dismiss()
//                                                BaseActivity.showAlert(
//                                                    requireActivity(),
//                                                    getString(R.string.table_csv_import_error_message)
//                                                )
//                                            }
//
//                                        }
//                                        else
//                                        {
//                                            Log.d("TEST199",translatedText)
//                                        }
//                                    }
//
//                                })
//                            }
//
//
//                        } catch (e: IOException) {
//                            BaseActivity.dismiss()
//                            BaseActivity.showAlert(
//                                requireActivity(),
//                                getString(R.string.table_csv_import_error_message)
//                            )
//                            e.printStackTrace()
//                        }
                    }

                } catch (e: Exception) {
                    BaseActivity.dismiss()
                    BaseActivity.showAlert(
                        requireActivity(),
                        getString(R.string.table_csv_import_error_message)
                    )
                    e.printStackTrace()
                }

            }
        }

    private fun displayTableList() {

        val list = tableGenerator.getAllDatabaseTables()
        if (list.isNotEmpty()) {
            tableList.clear()
        }
        tableList.addAll(list)
        adapter.notifyItemRangeChanged(0, tableList.size)
        adapter.setOnItemClickListener(this)


    }


//    private fun getDisplayScanHistory(){
//        BaseActivity.startLoading(requireActivity())
//        appViewModel.getAllScanQRCodeHistory().observe(this, Observer { list ->
//            BaseActivity.dismiss()
//            if (list.isNotEmpty()){
//                qrCodeHistoryList.clear()
//                emptyView.visibility = View.GONE
//                qrCodeHistoryRecyclerView.visibility = View.VISIBLE
//                qrCodeHistoryList.addAll(list)
//                adapter.notifyDataSetChanged()
//            }
//            else
//            {
//                qrCodeHistoryRecyclerView.visibility = View.GONE
//                emptyView.visibility = View.VISIBLE
//            }
//        })
//    }

    override fun onItemClick(position: Int) {
        val table = tableList[position]
        val intent = Intent(requireActivity(), TableViewActivity::class.java)
        intent.putExtra("TABLE_NAME", table)
        requireActivity().startActivity(intent)
    }

    private fun checkCyrillicCharacter(text: String): Boolean {
        var isCyrillicCharacter = false
        for (c: Char in text.toCharArray()) {
            if (isCyrillicCharacter(c)) {
                isCyrillicCharacter = true
                break
            }
        }
        return isCyrillicCharacter
    }

    private fun isCyrillicCharacter(c: Char): Boolean {
        val isPriorToKitkat = Build.VERSION.SDK_INT < 19
        val block: Character.UnicodeBlock = Character.UnicodeBlock.of(c)!!
        return block == Character.UnicodeBlock.CYRILLIC || block == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY || if (isPriorToKitkat) false else block == Character.UnicodeBlock.CYRILLIC_EXTENDED_A || block == Character.UnicodeBlock.CYRILLIC_EXTENDED_B
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        updateInputBox = v as CustomTextInputEditText?
//        if (hasFocus) {
//            openDialog()
//        }
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

    private var barcodeEditList =
        mutableListOf<Triple<TextInputEditText, AppCompatImageView, String>>()
    private var counter: Int = 0
    private var detailList = mutableListOf<Pair<String, String>>()
    private var imageList = mutableListOf<String>()
    override fun onClick(v: View?) {
        val view = v!!
        if (view.tag == "row") {
            val position = view.id
            if (tableName.contains("import")) {
                val item = dataListCsv[position]
                if (quickEditFlag) {
                    openQuickEditDialogCsv(item)
                } else {
                    Constants.csvItemData = item
                    val intent = Intent(context, CodeDetailActivity::class.java)
                    intent.putExtra("TABLE_NAME", tableName)
                    startActivity(intent)
                }
            } else {
                val item = dataList[position]
                if (quickEditFlag) {
                    openQuickEditDialog(item)
                } else {
                    val intent = Intent(context, CodeDetailActivity::class.java)
                    intent.putExtra("TABLE_NAME", tableName)
                    intent.putExtra("TABLE_ITEM", item)
                    startActivity(intent)
                }
            }


        } else if (view.tag == "qe") {
            val position = view.id
            val triple = barcodeEditList[position]
            triple.first.setText("")
        } else if (view.tag == "more") {

            val position = view.id
            if (tableName.contains("import")) {
                csvItemDetail = dataListCsv[position]
            } else {
                itemDetail = dataList[position]
            }

            val popup = PopupMenu(context, view)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item!!.itemId) {
                        R.id.pp_remove -> {
                            if (tableName.contains("import")) {
                                removeItem(csvItemDetail[0].second.toInt(), position)
                            } else {
                                removeItem(itemDetail.id, position)
                            }

                            true
                        }
                        R.id.pp_copy -> {
                            if (tableName.contains("import")) {
                                val stringBuilder = StringBuilder()
                                for (i in 0 until csvItemDetail.size) {
                                    stringBuilder.append("${csvItemDetail[i].first}: ${csvItemDetail[i].second}")
                                    if (i != csvItemDetail.size - 1) {
                                        stringBuilder.append("\n")
                                    }
                                }
                                copyToClipBoard(stringBuilder.toString())
                            } else {
                                copyToClipBoard(itemDetail.toString())
                            }

                            true
                        }
                        else -> false
                    }
                }

            })
            popup.inflate(R.menu.table_pop_up_menu)
            popup.show()
        } else {
            if (dataList.isNotEmpty()) {
                val tag = view.tag.toString().toLowerCase(Locale.ENGLISH)
                if (currentOrder.isEmpty()) {
                    if (tag == "id") {
                        currentOrder = "DESC"
                    } else {
                        currentOrder = "ASC"
                    }

                } else {
                    currentOrder = if (currentColumn == tag && currentOrder == "DESC") {
                        "ASC"
                    } else {
                        "DESC"
                    }
                }
                currentColumn = tag

                val image = sortingImages[view.id]
                updateSortingImage(image, currentOrder)
                if (tableName.contains("import")) {
                    getTableDataFromCsv(tableName, currentColumn, currentOrder)
                } else {
                    getTableData(tableName, currentColumn, currentOrder)
                }


            }

        }
    }

    private fun updateSortingImage(imageView: AppCompatImageView, order: String) {
        for (i in 0 until sortingImages.size) {
            val sImage = sortingImages[i]
            if (imageView.id == sImage.id && currentOrder == order) {
                sImage.setColorFilter(Color.WHITE)
                if (currentOrder.toLowerCase(Locale.ENGLISH) == "asc") {
                    sImage.setImageResource(R.drawable.ic_sort_asc)
                } else {
                    sImage.setImageResource(R.drawable.ic_sort_desc)
                }

            } else {
                sImage.setColorFilter(Color.parseColor("#808080"))
            }
        }
    }

    private fun copyToClipBoard(content: String) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Barcode Detail", content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show()

    }

    private fun removeItem(id: Int, position: Int) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(getString(R.string.remove_item_alert_message_text))
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.remove_text)) { dialog, which ->
                dialog.dismiss()
                val isSuccess = tableGenerator.removeItem(tableName, id)
                if (isSuccess) {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.remove_item_success_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (tableName.contains("import")) {
                        dataListCsv.removeAt(position)
                        getTableDataFromCsv(tableName, "", "")
                    } else {
                        dataList.removeAt(position)
                        getTableData(tableName, "", "")
                    }

                }
            }
            .create().show()
    }

}