package com.boris.expert.csvmagic.view.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.BarcodeImageAdapter
import com.boris.expert.csvmagic.adapters.InternetImageAdapter
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.ImageManager
import com.boris.expert.csvmagic.utils.RuntimePermissionHelper
import com.boris.expert.csvmagic.view.activities.BarcodeReaderActivity
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.boris.expert.csvmagic.view.activities.UserScreenActivity
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.util.*

class ApImageUploadFragment : Fragment() {

    private lateinit var internetImageDoneBtn: MaterialButton
    private lateinit var selectedImageView: AppCompatImageView
    private var currentPhotoPath: String? = null
    private var selectedImageBase64String: String = ""
    private lateinit var appSettings: AppSettings
    private var selectedInternetImage = ""
    private var userCurrentCredits = ""
    private var intentType = 0
    private lateinit var viewModel: SalesCustomersViewModel
    private var email = ""
    private var password = ""
    private var shopName = ""
    private lateinit var internetImageAdapter: InternetImageAdapter
    private lateinit var searchBtnView: ImageButton
    private lateinit var searchBoxView: TextInputEditText
    private lateinit var loader: ProgressBar
    private lateinit var voiceSearchIcon: AppCompatImageView
    private var voiceLanguageCode = "en"
    val searchedImagesList = mutableListOf<String>()
    private lateinit var imagesRecyclerView: RecyclerView
    private var barcodeImageList = mutableListOf<String>()
    var multiImagesList = mutableListOf<String>()
    private lateinit var adapter: BarcodeImageAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(SalesCustomersViewModel()).createFor()
        )[SalesCustomersViewModel::class.java]
        shopName = appSettings.getString("INSALES_SHOP_NAME") as String
        email = appSettings.getString("INSALES_EMAIL") as String
        password = appSettings.getString("INSALES_PASSWORD") as String

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_ap_image_upload, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View) {
        selectedImageView = view.findViewById(R.id.selected_insales_add_product_image_view)
        imagesRecyclerView = view.findViewById(R.id.ap_image_fragment_recyclerview)
        val apSubmitBtn = view.findViewById<MaterialButton>(R.id.ap_dialog_submit_btn)
        val cameraImageView =
            view.findViewById<AppCompatImageView>(R.id.camera_image_view)
        val imagesImageView =
            view.findViewById<AppCompatImageView>(R.id.images_image_view)
        val internetImageView =
            view.findViewById<AppCompatImageView>(R.id.internet_image_view)

        cameraImageView.setOnClickListener {
            intentType = 1
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
            intentType = 2
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
            intentType = 3
            val tempImageList = mutableListOf<String>()
            val internetSearchLayout = LayoutInflater.from(context)
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
            val barcodeImage = internetSearchLayout.findViewById<AppCompatImageView>(
                R.id
                    .barcode_img_search_internet_images
            )
            internetImageDoneBtn = internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setCancelable(false)
            builder.setView(internetSearchLayout)
            val iAlert = builder.create()
            iAlert.show()

            internetImageDoneBtn.setOnClickListener {
                iAlert.dismiss()
            }

            barcodeImage.setOnClickListener {
                val intent = Intent(requireActivity(), BarcodeReaderActivity::class.java)
                barcodeImageResultLauncher.launch(intent)
            }

            closeBtn.setOnClickListener {
                iAlert.dismiss()
            }

            internetImageRecyclerView.layoutManager = StaggeredGridLayoutManager(
                2,
                LinearLayoutManager.VERTICAL
            )//GridLayoutManager(context, 2)
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

                override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                    //iAlert.dismiss()
                    selectedInternetImage = searchedImagesList[position]
                    Glide.with(requireActivity())
                        .load(selectedInternetImage)
                        .thumbnail(
                            Glide.with(requireActivity()).load(R.drawable.placeholder)
                        )
                        .fitCenter()
                        .into(selectedImageView)
                    if (btn.text.toString()
                            .toLowerCase(Locale.ENGLISH) == "attach"
                    ) {
                        barcodeImageList.add(selectedInternetImage)
                        multiImagesList.add(selectedInternetImage)
                        btn.text = requireActivity().resources.getString(R.string.attached_text)
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.dark_gray
                            )
                        )
                    } else {
                        btn.text = requireActivity().resources.getString(R.string.attach_text)
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primary_positive_color
                            )
                        )
                        barcodeImageList.removeAt(position)
                        multiImagesList.removeAt(position)
                    }
                    adapter.notifyDataSetChanged()
                    Log.d("TEST199", multiImagesList.toString())
                }

            })

            voiceSearchIcon.setOnClickListener {
                voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                val voiceLayout = LayoutInflater.from(context)
                    .inflate(R.layout.voice_language_setting_layout, null)
                val voiceLanguageSpinner =
                    voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
                val voiceLanguageSaveBtn =
                    voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

                if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                    voiceLanguageSpinner.setSelection(0, false)
                } else {
                    voiceLanguageSpinner.setSelection(1, false)
                }

                voiceLanguageSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            voiceLanguageCode =
                                if (parent!!.selectedItem.toString().toLowerCase(Locale.ENGLISH)
                                        .contains("english")
                                ) {
                                    "en"
                                } else {
                                    "ru"
                                }
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
//                                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//
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
//                                        .setMessage(getString(R.string.low_credites_error_message))
//                                        .setCancelable(false)
//                                        .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                                            dialog.dismiss()
//                                        }
//                                        .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                                            dialog.dismiss()
//                                            startActivity(
//                                                Intent(
//                                                    requireActivity(),
//                                                    UserScreenActivity::class.java
//                                                )
//                                            )
//                                        }
//                                        .create().show()
//                                }
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
//                        getString(R.string.empty_text_error)
//                    )
//                }
                startSearch(
                    searchBoxView,
                    searchBtnView,
                    loader,
                    searchedImagesList,
                    internetImageAdapter
                )
            }

            searchBoxView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList,
                            internetImageAdapter
                        )
                    }
                    return false
                }

            })

        }


        apSubmitBtn.setOnClickListener {
            val barcodeId = appSettings.getString("AP_BARCODE_ID") as String
            val selectedCategoryId = appSettings.getInt("AP_PRODUCT_CATEGORY")
            val finalTitleText = appSettings.getString("AP_PRODUCT_TITLE") as String
            val finalDescriptionText = appSettings.getString("AP_PRODUCT_DESCRIPTION") as String
            val finalQuantityText = appSettings.getString("AP_PRODUCT_QUANTITY") as String
            val finalPriceText = appSettings.getString("AP_PRODUCT_PRICE") as String

            when {
                selectedCategoryId == 0 -> {
                    BaseActivity.showAlert(
                        requireActivity(),
                        "Product category is missing!"
                    )
                }
                finalTitleText.isEmpty() -> {
                    BaseActivity.showAlert(
                        requireActivity(),
                        "Product title is missing!"
                    )
                }
                finalDescriptionText.isEmpty() -> {
                    BaseActivity.showAlert(
                        requireActivity(),
                        "Product description is missing!"
                    )
                }
                finalQuantityText.isEmpty() -> {
                    BaseActivity.showAlert(
                        requireActivity(),
                        "Product quantity is missing!"
                    )
                }
                finalPriceText.isEmpty() -> {
                    BaseActivity.showAlert(
                        requireActivity(),
                        "Product price is missing!"
                    )
                }
                else -> {
                    BaseActivity.startLoading(requireActivity())
                    viewModel.callAddProduct(
                        requireActivity(),
                        shopName,
                        email,
                        password,
                        selectedCategoryId,
                        finalTitleText,
                        finalDescriptionText,
                        finalQuantityText,
                        finalPriceText,
                        barcodeId
                    )
                    viewModel.getAddProductResponse()
                        .observe(requireActivity(), Observer { response ->
                            if (response != null) {
                                if (response.get("status").asString == "200") {

                                    val details = response.getAsJsonObject("details")
                                    val productId = details.get("id").asInt

                                    if (multiImagesList.isNotEmpty()) {
                                        BaseActivity.dismiss()
                                        Constants.startImageUploadService(productId,multiImagesList.joinToString(","),"add_product")
                                        Constants.multiImagesSelectedListSize = multiImagesList.size
                                        multiImagesList.clear()
//                                        BaseActivity.startLoading(requireActivity())
//                                        uploadImages(
//                                            productId,
//                                            multiImagesList,
//                                            object : ResponseListener {
//                                                override fun onSuccess(result: String) {
//                                                    if (result.contains("success")) {
                                                        resetFieldValues()
//                                                        Handler(Looper.myLooper()!!).postDelayed(
//                                                            {
//                                                                BaseActivity.dismiss()
                                                                creditCharged()
                                                                val intent =
                                                                    Intent("dialog-dismiss")
                                                                LocalBroadcastManager.getInstance(
                                                                    requireActivity()
                                                                ).sendBroadcast(intent)
//                                                            },
//                                                            6000
//                                                        )
//                                                    }
//                                                }
//
//                                            })
//                                        viewModel.callAddProductImage(
//                                            requireActivity(),
//                                            shopName,
//                                            email,
//                                            password,
//                                            selectedImageBase64String,
//                                            productId,
//                                            "${System.currentTimeMillis()}.jpg",
//                                            if (intentType != 3) {
//                                                ""
//                                            } else {
//                                                selectedInternetImage
//                                            }
//                                        )
//                                        viewModel.getAddProductImageResponse()
//                                            .observe(
//                                                requireActivity(),
//                                                Observer { response ->
//
//                                                    if (response != null) {
//                                                        if (response.get("status").asString == "200") {
//                                                            selectedImageBase64String = ""
//                                                            selectedInternetImage = ""
//                                                            Handler(Looper.myLooper()!!).postDelayed(
//                                                                {
//                                                                    BaseActivity.dismiss()
//                                                                    creditCharged()
//                                                                    val intent = Intent("dialog-dismiss")
//                                                                    LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
//                                                                },
//                                                                2000
//                                                            )
//                                                        } else {
//                                                            BaseActivity.dismiss()
//                                                            BaseActivity.showAlert(
//                                                                requireActivity(),
//                                                                response.get("message").asString
//                                                            )
//                                                        }
//                                                    } else {
//                                                        BaseActivity.dismiss()
//                                                        BaseActivity.showAlert(
//                                                            requireActivity(),
//                                                            getString(R.string.something_wrong_error)
//                                                        )
//                                                    }
//                                                })
                                    } else {
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            Constants.multiImagesSelectedListSize = 0
                                            resetFieldValues()
                                            BaseActivity.dismiss()
                                            creditCharged()
                                            resetFieldValues()
                                            val intent = Intent("dialog-dismiss")
                                            LocalBroadcastManager.getInstance(requireActivity())
                                                .sendBroadcast(intent)

                                        }, 3000)
                                    }

                                } else {
                                    resetFieldValues()
                                    BaseActivity.dismiss()
                                    BaseActivity.showAlert(
                                        requireActivity(),
                                        response.get("message").asString
                                    )
                                }
                            } else {
                                BaseActivity.dismiss()
                            }
                        })
                }
            }
        }

        imagesRecyclerView.layoutManager = LinearLayoutManager(
            requireActivity(), RecyclerView.HORIZONTAL,
            false
        )
        imagesRecyclerView.hasFixedSize()
        adapter = BarcodeImageAdapter(
            requireContext(),
            barcodeImageList as ArrayList<String>
        )
        imagesRecyclerView.adapter = adapter
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
                    adapter.notifyItemRemoved(position)
                    if (barcodeImageList.size == 0) {
                        Glide.with(requireActivity())
                            .load("")
                            .placeholder(R.drawable.placeholder)
                            .centerInside()
                            .into(selectedImageView)
                    }
                }
                val alert = builder.create()
                alert.show()

            }

            override fun onAddItemEditClick(position: Int) {

            }

            override fun onImageClick(position: Int) {

            }

        })

    }

    private var barcodeImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.hasExtra("SCANNED_BARCODE_VALUE")) {
                    val barcodeId =
                        result.data!!.getStringExtra("SCANNED_BARCODE_VALUE") as String
                    if (barcodeId.isNotEmpty()) {
                        searchBoxView.setText(barcodeId)
                        Constants.hideKeyboar(requireActivity())
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList as ArrayList<String>,
                            internetImageAdapter
                        )
                    }
                }


            }
        }

    var index = 0
    private fun uploadImages(
        productId: Int,
        listImages: List<String>,
        responseListener: ResponseListener
    ) {

        var imageType = ""
        val imageFile = listImages[index]
        if (imageFile.contains("http")) {
            imageType = "src"
            selectedInternetImage = imageFile
        } else {
            imageType = "attachment"
            selectedImageBase64String = ImageManager.convertImageToBase64(
                requireActivity(),
                imageFile
            )
        }

        viewModel.callAddProductImage(
            requireActivity(),
            shopName,
            email,
            password,
            selectedImageBase64String,
            productId,
            "${System.currentTimeMillis()}.jpg",
            if (imageType == "attachment") {
                ""
            } else {
                selectedInternetImage
            }
        )
        viewModel.getAddProductImageResponse()
            .observe(
                requireActivity(),
                Observer { response ->
                    if (response != null) {
//                        if (response.get("status").asString == "200") {
                            selectedImageBase64String = ""
                            selectedInternetImage = ""

                            if (index == listImages.size-1){
                                index = 0
                                responseListener.onSuccess("success")
                            }
                            else{
                                index++
                                uploadImages(productId,listImages,responseListener)
                            }
//                        } else {
//                            BaseActivity.dismiss()
//                            BaseActivity.showAlert(
//                                requireActivity(),
//                                response.get("message").asString
//                            )
//                        }
                    } else {
                        BaseActivity.dismiss()
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.something_wrong_error)
                        )
                    }
                })
    }

    private fun resetFieldValues() {
        appSettings.remove("AP_BARCODE_ID")
        appSettings.remove("AP_PRODUCT_CATEGORY")
        appSettings.remove("AP_PRODUCT_TITLE")
        appSettings.remove("AP_PRODUCT_DESCRIPTION")
        appSettings.remove("AP_PRODUCT_QUANTITY")
        appSettings.remove("AP_PRODUCT_PRICE")
        multiImagesList.clear()
        barcodeImageList.clear()
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
                startSearch(
                    searchBoxView, searchBtnView, loader,
                    searchedImagesList as ArrayList<String>, internetImageAdapter
                )
            }
        }

    private fun startSearch(
        searchBoxView: TextInputEditText,
        searchBtnView: ImageButton,
        loader: ProgressBar,
        searchedImagesList: ArrayList<String>,
        internetImageAdapter: InternetImageAdapter
    ) {
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

                        userCurrentCredits =
                            appSettings.getString(Constants.userCreditsValue) as String

                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
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
                        } else {
                            MaterialAlertDialogBuilder(requireActivity())
                                .setMessage(getString(R.string.low_credites_error_message))
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                                    dialog.dismiss()
                                    startActivity(Intent(context, UserScreenActivity::class.java))
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


    private fun creditCharged() {
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        val remaining = userCurrentCredits.toFloat() - 0.1
        userCurrentCredits = remaining.toString()
        hashMap["credits"] = userCurrentCredits
        firebaseDatabase.child(Constants.firebaseUserCredits)
            .child(Constants.firebaseUserId)
            .updateChildren(hashMap)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val bitmap = data!!.extras!!.get("data") as Bitmap
                createImageFile(bitmap)
//                selectedImageBase64String =
//                    ImageManager.convertImageToBase64(
//                        requireActivity(),
//                        currentPhotoPath!!
//                    )
//                Log.d("TEST199DIALOG", selectedImageBase64String)
                Glide.with(requireActivity())
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)
                barcodeImageList.add(currentPhotoPath!!)
                multiImagesList.add(currentPhotoPath!!)
                adapter.notifyDataSetChanged()
                Log.d("TEST199", multiImagesList.toString())
            }
        }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val imageUri = result.data!!
                    currentPhotoPath = ImageManager.getRealPathFromUri(
                        requireActivity(),
                        imageUri.data
                    )
//                    selectedImageBase64String =
//                        ImageManager.convertImageToBase64(
//                            requireActivity(),
//                            currentPhotoPath!!
//                        )
//                    Log.d("TEST199", selectedImageBase64String)
                    Glide.with(requireActivity())
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(selectedImageView)
                    barcodeImageList.add(currentPhotoPath!!)
                    multiImagesList.add(currentPhotoPath!!)
                    adapter.notifyDataSetChanged()
                    Log.d("TEST199", multiImagesList.toString())
                }

            }
        }

}