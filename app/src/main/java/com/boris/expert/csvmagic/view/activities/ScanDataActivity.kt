package com.boris.expert.csvmagic.view.activities

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.customviews.CustomTextInputEditText
import com.boris.expert.csvmagic.interfaces.LoginCallback
import com.boris.expert.csvmagic.interfaces.ScannerInterface
import com.boris.expert.csvmagic.interfaces.UploadImageCallback
import com.boris.expert.csvmagic.model.Sheet
import com.boris.expert.csvmagic.room.AppViewModel
import com.boris.expert.csvmagic.singleton.DriveService
import com.boris.expert.csvmagic.singleton.SheetService
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.fragments.ScannerFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ScanDataActivity : BaseActivity(),CustomAlertDialog.CustomDialogListener,
    View.OnFocusChangeListener {

    private var columns: Array<String>?=null
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var customAlertDialog: CustomAlertDialog? = null
    private var updateInputBox: CustomTextInputEditText? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var scanDataParentLayout:LinearLayout

    private var arrayList = mutableListOf<String>()
    private var filePathView: MaterialTextView? = null
    var currentPhotoPath: String? = null
    private lateinit var appViewModel: AppViewModel
    private lateinit var tableGenerator: TableGenerator
    private var tableName: String = ""
    private var scanText: String = ""
    private var textInputIdsList = mutableListOf<Pair<String, CustomTextInputEditText>>()
    private var spinnerIdsList = mutableListOf<Pair<String, AppCompatSpinner>>()
    private lateinit var appSettings: AppSettings
    private var isFileSelected = false
    private var listener: ScannerInterface? = null
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private var sheetsList = mutableListOf<Sheet>()
    private var userRecoverableAuthType = 0
    private var selectedSheetId: String = ""
    private var selectedSheetName: String = ""
    private lateinit var connectGoogleSheetsTextView: MaterialTextView
    private lateinit var sheetsTopLayout: LinearLayout
    var values: List<Any>? = null
    private lateinit var addImageCheckBox: MaterialCheckBox
    private lateinit var tableDetailLayoutWrapper: LinearLayout
    var multiImagesList = mutableListOf<String>()
    var uploadedUrlList = mutableListOf<String>()
    var array: List<String> = mutableListOf()
    val handler = Handler(Looper.myLooper()!!)
    var totalImageSize: Long = 0
    var url = " "
    var values_JSON = JSONArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_data)

        initViews()
        setUpToolbar()
        renderTableViews()
    }

    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        appSettings = AppSettings(context)
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(AppViewModel::class.java)
        tableGenerator = TableGenerator(context)


        scanDataParentLayout = findViewById(R.id.scan_data_parent_layout)
        if (intent != null && intent.hasExtra("TABLE_NAME")){
            tableName = intent.getStringExtra("TABLE_NAME") as String
        }
        else{
            finish()
        }

        if (intent != null && intent.hasExtra("SHEET_NAME")){
            selectedSheetName = intent.getStringExtra("SHEET_NAME") as String
            selectedSheetId = intent.getStringExtra("SHEET_ID") as String
            fetchSheetColumns()
        }

        if (intent != null && intent.hasExtra("SCAN_TEXT")){
            scanText = intent.getStringExtra("SCAN_TEXT") as String
        }
        else{
            finish()
        }

        columns = tableGenerator.getTableColumns(tableName)
        val codeDataTInputView = findViewById<CustomTextInputEditText>(R.id.scan_result_dialog_code_data)
        tableDetailLayoutWrapper = findViewById<LinearLayout>(R.id.table_detail_layout_wrapper)
        val submitBtn = findViewById<MaterialButton>(R.id.scan_result_dialog_submit_btn)
        addImageCheckBox = findViewById<MaterialCheckBox>(R.id.add_image_checkbox)
        val severalImagesHintView = findViewById<MaterialTextView>(R.id.several_images_hint_view)
        val imageSourcesWrapperLayout = findViewById<LinearLayout>(R.id.image_sources_layout)
        filePathView = findViewById<MaterialTextView>(R.id.filePath)
        val imageRecognitionBtn = findViewById<LinearLayout>(R.id.image_recognition_btn)
        val photoRecognitionBtn = findViewById<LinearLayout>(R.id.photo_recognition_btn)

        imageRecognitionBtn.setOnClickListener {
            if (RuntimePermissionHelper.checkCameraPermission(
                    context,
                    Constants.READ_STORAGE_PERMISSION
                )
            ) {
                hideSoftKeyboard(context, imageRecognitionBtn)
                pickImageFromGallery()
            }
        }

        photoRecognitionBtn.setOnClickListener {
            if (RuntimePermissionHelper.checkCameraPermission(
                    context, Constants.CAMERA_PERMISSION
                )
            ) {
                hideSoftKeyboard(context, photoRecognitionBtn)
                pickImageFromCamera()
            }


        }

        addImageCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (Constants.userData == null) {
                    addImageCheckBox.isChecked = false
                    MaterialAlertDialogBuilder(context)
                        .setTitle(getString(R.string.alert_text))
                        .setMessage(getString(R.string.login_error_text))
                        .setNegativeButton(getString(R.string.later_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.login_text)) { dialog, which ->
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
                    filePathView!!.visibility = View.VISIBLE
                }

            } else {
                severalImagesHintView.visibility = View.GONE
                imageSourcesWrapperLayout.visibility = View.GONE
                filePathView!!.visibility = View.GONE
            }
        }

        val cameraImageView = findViewById<AppCompatImageView>(R.id.camera_image_view)
        val imagesImageView = findViewById<AppCompatImageView>(R.id.images_image_view)

        cameraImageView.setOnClickListener {
            if (RuntimePermissionHelper.checkCameraPermission(
                    context,
                    Constants.CAMERA_PERMISSION
                )
            ) {
                //dispatchTakePictureIntent()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(cameraIntent)
            }
        }

        imagesImageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getImageFromGallery()
            } else {
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),Constants.READ_STORAGE_REQUEST_CODE)
//                requestPermissions(
//                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    Constants.READ_STORAGE_REQUEST_CODE
//                )
            }
        }

        for (i in columns!!.indices) {
            val value = columns!![i]
            if (value == "id" || value == "quantity") {
                continue
            } else if (value == "code_data") {
                textInputIdsList.add(Pair(value, codeDataTInputView))
                codeDataTInputView.setText(scanText)
            } else {
                val tableRowLayout =
                    LayoutInflater.from(context)
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
                val pair = tableGenerator.getFieldList(value, tableName)

                if (pair != null) {
                    arrayList = mutableListOf()
                    if (!pair.first.contains(",") && pair.second == "listWithValues") {
                        arrayList.add(pair.first)

                        columnValue.visibility = View.GONE
                        columnDropDwonLayout.visibility = View.VISIBLE
                        val adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_item,
                            arrayList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        columnDropdown.adapter = adapter
                        spinnerIdsList.add(Pair(value, columnDropdown))
                    } else if (pair.first.contains(",") && pair.second == "listWithValues") {

                        arrayList.addAll(pair.first.split(","))

                        columnValue.visibility = View.GONE
                        columnDropDwonLayout.visibility = View.VISIBLE
                        val adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_item,
                            arrayList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        columnDropdown.adapter = adapter
                        spinnerIdsList.add(Pair(value, columnDropdown))
                    } else {
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

                    columnDropDwonLayout.visibility = View.GONE
                    columnValue.visibility = View.VISIBLE

                    if (value == "date") {
                        columnValue.setText(
                            getDateTimeFromTimeStamp(
                                System.currentTimeMillis()
                            )
                        )
                        columnValue.isEnabled = false
                        columnValue.isFocusable = false
                        columnValue.isFocusableInTouchMode = false
                    } else {
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

        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt2")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(context)
                    .anchorView(scanDataParentLayout)
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
            val availableStorageMemory = Constants.convertMegaBytesToBytes(
                appSettings.getString(
                    Constants.memory
                )!!.toFloat()
            )

            if (totalImageSize.toInt() == 0) {
                saveDataIntoTable()
            } else if (addImageCheckBox.isChecked && totalImageSize <= availableStorageMemory) {
                val expiredAt: Long = appSettings.getLong(Constants.expiredAt)
                if (System.currentTimeMillis() > expiredAt && expiredAt.toInt() != 0) {
                    showAlert(
                        context,
                        getString(R.string.subscription_expired_text)
                    )
                } else {
                    saveDataIntoTable()
                }

            } else {
                val b1 = MaterialAlertDialogBuilder(context)
                    .setCancelable(true)
                    .setTitle(getString(R.string.alert_text))
                    .setMessage(getString(R.string.storage_available_error_text))
                    .setNegativeButton(getString(R.string.close_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.save_without_image_text)) { dialog, which ->
                        dialog.dismiss()
                        addImageCheckBox.isChecked = false
                        saveDataIntoTable()
                    }
                val iAlert = b1.create()
                iAlert.show()
                iAlert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.purple_700
                    )
                )

            }
        }

    }

    private fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.scan_data_text)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }


    private fun renderTableViews(){

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
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
                val cropPicUri = CropImage.getPickImageResultUri(context, data)
                cropImage(cropPicUri)
            }
        }

    private fun cropImage(imageUri: Uri) {

        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(this)
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
                                    context,
                                    imageUri
                                )!!
                            )
                        }
                        filePathView!!.text = multiImagesList.joinToString(",")
                        isFileSelected = true
                        //Log.d("TEST199",multiImagesList.toString())
                    }
                } else {
                    if (data.data != null) {
                        val imageUri = data.data!!
                        multiImagesList.add(
                            ImageManager.getRealPathFromUri(
                                context,
                                imageUri
                            )!!
                        )
                        filePathView!!.text = multiImagesList.joinToString(",")
                        isFileSelected = true
                    }
                }
                totalImageSize = if (filePathView!!.text.contains(",")) {
                    getTotalImagesSize(filePathView!!.text.split(",").toMutableList())
                } else {
                    ImageManager.getFileSize(filePathView!!.text.toString())
                }
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

                totalImageSize = if (filePathView!!.text.contains(",")) {
                    getTotalImagesSize(filePathView!!.text.split(",").toMutableList())
                } else {
                    ImageManager.getFileSize(filePathView!!.text.toString())
                }

                Log.d("TEST199", "$totalImageSize")
            }
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
                    //startScanner()
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
                            .setMessage(getString(R.string.camera_permission_failed_text))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok_text)) { dialog, which ->
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
                            this,
                            Constants.READ_STORAGE_PERMISSION
                        )
                    ) {
                        RuntimePermissionHelper.checkStoragePermission(
                            context,
                            Constants.READ_STORAGE_PERMISSION
                        )
                    } else {
                        MaterialAlertDialogBuilder(context)
                            .setMessage(getString(R.string.external_storage_permission_error2))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok_text)) { dialog, which ->
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imgUri = result.uri
            try {
                if (updateInputBox != null) {
                    TextRecogniser.runTextRecognition(context, updateInputBox!!, imgUri)
                } else {
                    showAlert(
                        context,
                        getString(R.string.textinput_not_focused_error_text)
                    )
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(context, bitmap).absolutePath
        //Constants.captureImagePath = currentPhotoPath
        multiImagesList.add(currentPhotoPath!!)
        filePathView!!.text = multiImagesList.joinToString(",")
        isFileSelected = true
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    private fun uploadImageOnFirebaseStorage(listener: UploadImageCallback) {
        val imageList = filePathView!!.text.toString()
        if (imageList.contains(",")) {
            array = imageList.split(",")

            for (i in array.indices) {

                handler.postDelayed({

                    val imagePath = array[i]

                    if (FirebaseAuth.getInstance().currentUser != null) {
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid

                        val file = Uri.fromFile(File(imagePath))
                        val fileRef =
                            storageReference.child("${Constants.firebaseBarcodeImages}/$userId/${System.currentTimeMillis()}.jpg")
                        val uploadTask = fileRef.putFile(file)
                        uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            fileRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                uploadedUrlList.add(downloadUri.toString())
                                if (i == array.size - 1) {
                                    url = uploadedUrlList.joinToString(" ")
                                    uploadedUrlList.clear()
                                    listener.onSuccess("")
                                }
                            }
                        }
                    }
                }, (1000 * i + 1).toLong())

            }

        } else {
            val bundle = Bundle()
            bundle.putString("starts", "starts")
            mFirebaseAnalytics?.logEvent("upload image", bundle)

            if (FirebaseAuth.getInstance().currentUser != null) {
                val userId = FirebaseAuth.getInstance().currentUser!!.uid

                val file = Uri.fromFile(File(filePathView!!.text.toString()))
                val fileRef =
                    storageReference.child("BarcodeImages/$userId/${System.currentTimeMillis()}.jpg")
                val uploadTask = fileRef.putFile(file)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    fileRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        url = downloadUri.toString()
                        listener.onSuccess("")
                    }
                }
            }
        }

    }

    private fun getTotalImagesSize(uploadedUrlList: MutableList<String>): Long {
        var total: Long = 0
        for (i in 0 until uploadedUrlList.size) {
            total += ImageManager.getFileSize(uploadedUrlList[i])
        }
        return total
    }


    private fun updateStorageSize(size: Long, type: String) {
//        var currentStorageSize: Long = 0
//        databaseReference.child(Constants.firebaseUserFeatureDetails)
//            .child(Constants.firebaseUserId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.hasChildren()) {
//                        val pSize: Int? = snapshot.child("memory").getValue(Int::class.java)
//                        if (pSize != null) {
//                            currentStorageSize = if (type == "add") {
//                                Constants.convertMegaBytesToBytes(pSize) + size
//                            } else {
//                                Constants.convertMegaBytesToBytes(pSize) - size
//                            }
//                            val remainingMb: Int = Constants.convertBytesToMegaBytes(
//                                currentStorageSize
//                            )
//                            val params = HashMap<String, Any>()
//                            params["memory"] = remainingMb
//                            databaseReference.child(Constants.firebaseUserFeatureDetails)
//                                .child(Constants.firebaseUserId).updateChildren(params)
//                            totalImageSize = 0
//                            appSettings.putInt(Constants.memory, remainingMb)
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

    private fun checkBothListSame() {
        val first = mutableListOf<Any>()
        first.addAll(tableGenerator.getTableColumns(tableName)!!.toList())
        first.removeAt(0)
        if (values == null || !isEqual(first, values as List<Any>)) {
            MaterialAlertDialogBuilder(context)
                .setMessage("Do you want to create a new spreadsheet to save data? Because format of *$selectedSheetName is not correct")
                .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                    dialog.dismiss()
                }.setPositiveButton(getString(R.string.create)) { dialog, which ->
                    dialog.dismiss()
                    first.removeAt(0)
                    createNewSpreadsheet(tableName, first)
                }.create().show()
        }
    }

    private fun <T> isEqual(first: List<T>, second: List<T>): Boolean {

        if (first.size != second.size) {
            return false
        }

        return first.zip(second).all { (x, y) -> x == y }
    }

    private fun createNewSpreadsheet(tableName: String, headingsList: List<Any>) {
        startLoading(context)
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
                for (i in 0 until headingsList.size) {
                    values_JSON.put(headingsList[i].toString())
                }
//                getAllSheets()
                dismiss()
                appendRow(headingsList)
            } catch (e: Exception) {
                dismiss()
                e.printStackTrace()
            }

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
                    val numRows = if (response.getValues() != null) response.getValues().size else 0
                    if (numRows > 0) {
                        values = response.getValues()[0]
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        checkBothListSame()
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

            val result = SheetService.instance!!.spreadsheets().values().append(selectedSheetId, range, body)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute()
            Log.d("TEST199ROW", "${result.updates.updatedRows}")
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
                        getAllSheets()
                    }

                }

            }
        }

    private fun saveDataIntoTable() {

        if (isNetworkAvailable(context)) {
            dismiss()
            startLoading(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val params = mutableListOf<Pair<String, String>>()

                    // THIS IF PART WILL RUN WHEN ADD IMAGE CHECK BOX IS CHECKED
                    if (addImageCheckBox.isChecked && filePathView!!.text.toString()
                            .isNotEmpty()
                    ) {
                        uploadImageOnFirebaseStorage(object : UploadImageCallback {
                            override fun onSuccess(imageUrl:String) {
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
                                                dismiss()
                                                saveSuccessScans()
                                                Toast.makeText(
                                                    context,
                                                    getString(R.string.scan_data_save_success_text),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                textInputIdsList.clear()
                                                spinnerIdsList.clear()

                                                tableDetailLayoutWrapper.removeAllViews()
                                                filePathView!!.setText("")
                                                val tempList = mutableListOf<Any>()
                                                for (i in 0 until params.size) {
                                                    val pair = params[i]
                                                    //values_JSON.put(pair.second)
                                                    tempList.add(pair.second)
                                                }
                                                if (tempList.size > 0) {
                                                    // sendRequest()
                                                    appendRow(tempList)
                                                }
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
                                dismiss()
                                saveSuccessScans()
                                Toast.makeText(
                                    context,
                                    getString(R.string.scan_data_save_success_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                textInputIdsList.clear()
                                spinnerIdsList.clear()

                                tableDetailLayoutWrapper.removeAllViews()
                                filePathView!!.setText("")
//                                openHistoryBtnTip()
                                if (Constants.userData != null) {
                                    val tempList = mutableListOf<Any>()
                                    for (i in 0 until params.size) {
                                        val pair = params[i]
                                        //values_JSON.put(pair.second)
                                        tempList.add(pair.second)
                                    }
                                    if (tempList.size > 0) {
                                        // sendRequest()
                                        appendRow(tempList)
                                    }
//                                    for (i in 0 until params.size) {
//                                        val pair = params[i]
//                                        values_JSON.put(pair.second)
//                                    }
//                                    if (values_JSON.length() > 0) {
//                                        sendRequest()
//                                    }
                                }
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

            val b = MaterialAlertDialogBuilder(context)
                .setCancelable(true)
                .setTitle(getString(R.string.alert_text))
                .setMessage(getString(R.string.image_upload_internet_error_text))
                .setNegativeButton(resources.getString(R.string.close_text)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.save_without_image_text)) { dialog, which ->
                    dialog.dismiss()
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
                            dismiss()
                            saveSuccessScans()
                            Toast.makeText(
                                context,
                                getString(R.string.scan_data_save_success_text),
                                Toast.LENGTH_SHORT
                            ).show()
                            textInputIdsList.clear()
                            spinnerIdsList.clear()
                            params.clear()
                            tableDetailLayoutWrapper.removeAllViews()
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
                    context,
                    R.color.purple_700
                )
            )
        }

    }

    private fun openAddImageTooltip(addImageBox: MaterialCheckBox, submitBtn: MaterialButton) {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt3")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(context)
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
                SimpleTooltip.Builder(context)
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

    private fun saveSuccessScans() {
        var scans = DialogPrefs.getSuccessScan(context)
        if (scans >= 0) {
            scans += 1
            DialogPrefs.setSuccessScan(context, scans)
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
//                                displaySheetSpinner()
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

    override fun onEraseBtnClick(alertDialog: CustomAlertDialog) {
        updateInputBox!!.setText("")
    }

    override fun onImageRecognitionBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()

            hideSoftKeyboard(context, updateInputBox!!)
            pickImageFromGallery()

        }
    }

    override fun onPhotoRecognitionBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()

            hideSoftKeyboard(context, updateInputBox!!)
            pickImageFromCamera()

        }
    }

    override fun onDismissBtnClick(alertDialog: CustomAlertDialog) {
        if (customAlertDialog != null) {
            customAlertDialog!!.dismiss()
            updateInputBox!!.requestFocus()
            Constants.openKeyboar(context)
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        updateInputBox = v as CustomTextInputEditText?
        if (hasFocus) {
            openDialog()
        }
    }

    private fun openDialog() {
        updateInputBox!!.clearFocus()
        customAlertDialog = CustomAlertDialog()
        customAlertDialog!!.setFocusListener(this)
        customAlertDialog!!.show(supportFragmentManager, "dialog")
    }
}