package com.boris.expert.csvmagic.view.activities

import android.Manifest
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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.InSalesProductsAdapter
import com.boris.expert.csvmagic.adapters.InternetImageAdapter
import com.boris.expert.csvmagic.adapters.ProductImagesAdapter
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.BackupListener
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.model.ProductImages
import com.boris.expert.csvmagic.model.Product
import com.boris.expert.csvmagic.utils.*
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
import com.google.gson.JsonObject
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class SalesCustomersActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var appSettings: AppSettings
    private lateinit var viewModel: SalesCustomersViewModel
    private lateinit var insalesLoginWrapperLayout: CardView
    private lateinit var insalesDataWrapperLayout: LinearLayout
    private lateinit var insalesShopNameBox: TextInputEditText
    private lateinit var insalesEmailBox: TextInputEditText
    private lateinit var insalesPasswordBox: TextInputEditText
    private lateinit var insalesLoginBtn: MaterialButton
    private var productsList = mutableListOf<Product>()
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var adapter: InSalesProductsAdapter
    private var galleryIntentType = 0
    var currentPhotoPath: String? = null
    var selectedImageBase64String: String = ""
    private var email = ""
    private var password = ""
    private var shopName = ""
    private var intentType = 0
    private var selectedInternetImage = ""
    private var userCurrentCredits = ""
    private var menu:Menu?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_customers)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(SalesCustomersViewModel()).createFor()
        )[SalesCustomersViewModel::class.java]
        appSettings = AppSettings(context)
        toolbar = findViewById(R.id.toolbar)

        insalesLoginWrapperLayout = findViewById(R.id.insales_login_wrapper_layout)
        insalesDataWrapperLayout = findViewById(R.id.insales_data_wrapper_layout)
        insalesShopNameBox = findViewById(R.id.insales_login_shop_name_box)
        insalesEmailBox = findViewById(R.id.insales_login_email_box)
        insalesPasswordBox = findViewById(R.id.insales_login_password_box)
        insalesLoginBtn = findViewById(R.id.insales_login_btn)
        insalesLoginBtn.setOnClickListener(this)
        productsRecyclerView = findViewById(R.id.insales_products_recyclerview)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.insales_customers)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        }
        else if(item.itemId == R.id.insales_logout){
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_insales_warning_text))
                .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.logout)) { dialog, which ->
                    dialog.dismiss()
                    appSettings.remove("INSALES_STATUS")
                    appSettings.remove("INSALES_SHOP_NAME")
                    appSettings.remove("INSALES_EMAIL")
                    appSettings.remove("INSALES_PASSWORD")
                    startActivity(Intent(context,SalesCustomersActivity::class.java))

                }
                .create().show()
            true
        }
        else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun inSalesLogin(shopName: String, email: String, password: String) {

        startLoading(context,getString(R.string.please_wait_login_message))
        viewModel.callSalesAccount(context, shopName, email, password)
        viewModel.getSalesAccountResponse().observe(this, Observer { response ->
            dismiss()
            if (response != null) {
                if (response.get("status").asString == "200") {
                    appSettings.putString("INSALES_STATUS", "logged")
                    appSettings.putString("INSALES_SHOP_NAME", shopName)
                    appSettings.putString("INSALES_EMAIL", email)
                    appSettings.putString("INSALES_PASSWORD", password)
                    this.email = email
                    this.password = password
                    this.shopName = shopName

                    insalesLoginWrapperLayout.visibility = View.GONE
                    insalesDataWrapperLayout.visibility = View.VISIBLE
                    menu!!.findItem(R.id.insales_logout).isVisible = true
                    showProducts()
                } else {
                    showAlert(context, response.get("message").asString)
                }
            }
        })
    }

    lateinit var selectedImageView: AppCompatImageView
    private fun showProducts() {
        productsRecyclerView.layoutManager = LinearLayoutManager(context)
        productsRecyclerView.hasFixedSize()
        adapter = InSalesProductsAdapter(
            context,
            productsList as ArrayList<Product>
        )
        productsRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : InSalesProductsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

            }

            override fun onItemEditClick(position: Int, imagePosition: Int) {
                val imageItem = productsList[position].productImages!![imagePosition]
                val insalesUpdateProductImageLayout = LayoutInflater.from(context).inflate(
                    R.layout.insales_product_image_update_dialog, null
                )
                selectedImageView =
                    insalesUpdateProductImageLayout.findViewById(R.id.selected_insales_product_image_view)
                val cameraImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.camera_image_view)
                val imagesImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.images_image_view)
                val internetImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.internet_image_view)
                val cancelDialogBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_cancel_btn)
                val updateImageBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_update_btn)

                cameraImageView.setOnClickListener {
                    intentType = 1
                    if (RuntimePermissionHelper.checkCameraPermission(
                            context,
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
                            context,
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
                    val searchedImagesList = mutableListOf<String>()
                    val tempImageList = mutableListOf<String>()
                    val internetSearchLayout = LayoutInflater.from(context)
                        .inflate(R.layout.internet_image_search_dialog_layout, null)
                    val loader =
                        internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
                    val searchBoxView =
                        internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
                    val searchBtnView =
                        internetSearchLayout.findViewById<MaterialButton>(R.id.internet_image_search_btn)
                    val internetImageRecyclerView =
                        internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
                    val closeBtn =
                        internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
                    val builder = MaterialAlertDialogBuilder(context)
                    builder.setCancelable(false)
                    builder.setView(internetSearchLayout)
                    val iAlert = builder.create()
                    iAlert.show()

                    closeBtn.setOnClickListener {
                        iAlert.dismiss()
                    }

                    internetImageRecyclerView.layoutManager = StaggeredGridLayoutManager(
                        2,
                        LinearLayoutManager.VERTICAL
                    )//GridLayoutManager(context, 2)
                    internetImageRecyclerView.hasFixedSize()
                    val internetImageAdapter = InternetImageAdapter(
                        context,
                        searchedImagesList as java.util.ArrayList<String>
                    )
                    internetImageRecyclerView.adapter = internetImageAdapter
                    internetImageAdapter.setOnItemClickListener(object :
                        InternetImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedImage = searchedImagesList[position]

                        }

                        override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                            btn.text = getString(R.string.please_wait)

                            val selectedImage = searchedImagesList[position]
                            val bitmap: Bitmap? = ImageManager.getBitmapFromURL(
                                context,
                                selectedImage
                            )
                            if (bitmap != null) {
                                ImageManager.saveMediaToStorage(
                                    context,
                                    bitmap,
                                    object : ResponseListener {
                                        override fun onSuccess(result: String) {
                                            if (loader.visibility == View.VISIBLE) {
                                                loader.visibility = View.INVISIBLE
                                            }

                                            if (result.isNotEmpty()) {
                                                currentPhotoPath = ImageManager.getRealPathFromUri(context, Uri.parse(result))!!
                                                Glide.with(context)
                                                    .load(currentPhotoPath)
                                                    .placeholder(R.drawable.placeholder)
                                                    .centerInside()
                                                    .into(selectedImageView)
                                                selectedImageBase64String = ImageManager.convertImageToBase64(context, currentPhotoPath!!)
                                            iAlert.dismiss()
                                            } else {
                                                showAlert(
                                                    context,
                                                    getString(R.string.something_wrong_error)
                                                )
                                            }
                                        }

                                    })
                            } else {
                                if (loader.visibility == View.VISIBLE) {
                                    loader.visibility = View.INVISIBLE
                                }
                                showAlert(
                                    context,
                                    getString(R.string.something_wrong_error)
                                )
                            }
                        }

                    })


                    searchBtnView.setOnClickListener {
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
                                            hideSoftKeyboard(
                                                context,
                                                searchBtnView
                                            )
                                            //Constants.hideKeyboar(requireActivity())
                                            val query = searchBoxView.text.toString().trim()
                                            runOnUiThread {
                                                loader.visibility = View.VISIBLE
                                            }

                                            searchInternetImages(
                                                context,
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
                                                                getUserCredits(
                                                                    context
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

                                                        showAlert(
                                                            context,
                                                            error.localizedMessage!!
                                                        )
                                                    }

                                                })
                                        } else {
                                            MaterialAlertDialogBuilder(context)
                                                .setMessage(getString(R.string.low_credites_error_message))
                                                .setCancelable(false)
                                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                                                    dialog.dismiss()
                                                }
                                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                                                    dialog.dismiss()
                                                    startActivity(
                                                        Intent(
                                                            context,
                                                            UserScreenActivity::class.java
                                                        )
                                                    )
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

                            showAlert(
                                context,
                                getString(R.string.empty_text_error)
                            )
                        }
                    }

                }


                Glide.with(context)
                    .load(imageItem.imageUrl)
                    .thumbnail(Glide.with(context).load(R.drawable.loader))
                    .fitCenter()
                    .into(selectedImageView)
                val builder = MaterialAlertDialogBuilder(context)
                builder.setCancelable(false)
                builder.setView(insalesUpdateProductImageLayout)

                val alert = builder.create()
                alert.show()

                cancelDialogBtn.setOnClickListener {
                    alert.dismiss()
                }

                updateImageBtn.setOnClickListener {

                    if (selectedImageBase64String.isNotEmpty()) {
                        alert.dismiss()
                        startLoading(context)

                        viewModel.callUpdateProductImage(
                            context,
                            shopName,
                            email,
                            password,
                            selectedImageBase64String,
                            imageItem.productId,
                            imageItem.position,
                            imageItem.id,
                            "${System.currentTimeMillis()}.jpg"
                        )
                        viewModel.getUpdateProductImageResponse()
                            .observe(this@SalesCustomersActivity, Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        selectedImageBase64String = ""
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            dismiss()
                                            showProducts()
                                        }, 6000)
                                    } else {
                                        dismiss()
                                        showAlert(context, response.get("message").asString)
                                    }
                                } else {
                                    dismiss()
                                    showProducts()
                                }
                            })
                    } else {
                        showAlert(context, getString(R.string.image_attach_error))
                    }
                }

            }

            override fun onItemAddImageClick(position: Int) {
                val pItem = productsList[position]
                val insalesUpdateProductImageLayout = LayoutInflater.from(context).inflate(
                    R.layout.insales_product_image_update_dialog, null
                )
                selectedImageView =
                    insalesUpdateProductImageLayout.findViewById(R.id.selected_insales_product_image_view)
                val cameraImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.camera_image_view)
                val imagesImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.images_image_view)
                val internetImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.internet_image_view)
                val cancelDialogBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_cancel_btn)
                val updateImageBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_update_btn)

                cameraImageView.setOnClickListener {
                    intentType = 1
                    if (RuntimePermissionHelper.checkCameraPermission(
                            context,
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
                            context,
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
                    val searchedImagesList = mutableListOf<String>()
                    val tempImageList = mutableListOf<String>()
                    val internetSearchLayout = LayoutInflater.from(context)
                        .inflate(R.layout.internet_image_search_dialog_layout, null)
                    val loader =
                        internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
                    val searchBoxView =
                        internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
                    val searchBtnView =
                        internetSearchLayout.findViewById<MaterialButton>(R.id.internet_image_search_btn)
                    val internetImageRecyclerView =
                        internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
                    val closeBtn =
                        internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
                    val builder = MaterialAlertDialogBuilder(context)
                    builder.setCancelable(false)
                    builder.setView(internetSearchLayout)
                    val iAlert = builder.create()
                    iAlert.show()

                    closeBtn.setOnClickListener {
                        iAlert.dismiss()
                    }

                    internetImageRecyclerView.layoutManager = StaggeredGridLayoutManager(
                        2,
                        LinearLayoutManager.VERTICAL
                    )//GridLayoutManager(context, 2)
                    internetImageRecyclerView.hasFixedSize()
                    val internetImageAdapter = InternetImageAdapter(
                        context,
                        searchedImagesList as java.util.ArrayList<String>
                    )
                    internetImageRecyclerView.adapter = internetImageAdapter
                    internetImageAdapter.setOnItemClickListener(object :
                        InternetImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedImage = searchedImagesList[position]

                        }

                        override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                            iAlert.dismiss()
                            selectedInternetImage = searchedImagesList[position]
                            Glide.with(context)
                                .load(selectedInternetImage)
                                .thumbnail(Glide.with(context).load(R.drawable.placeholder))
                                .fitCenter()
                                .into(selectedImageView)
                        }

                    })


                    searchBtnView.setOnClickListener {
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
                                            hideSoftKeyboard(
                                                context,
                                                searchBtnView
                                            )
                                            //Constants.hideKeyboar(requireActivity())
                                            val query = searchBoxView.text.toString().trim()
                                            runOnUiThread {
                                                loader.visibility = View.VISIBLE
                                            }

                                            searchInternetImages(
                                                context,
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
                                                                getUserCredits(
                                                                    context
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

                                                        showAlert(
                                                            context,
                                                            error.localizedMessage!!
                                                        )
                                                    }

                                                })
                                        } else {
                                            MaterialAlertDialogBuilder(context)
                                                .setMessage(getString(R.string.low_credites_error_message))
                                                .setCancelable(false)
                                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                                                    dialog.dismiss()
                                                }
                                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                                                    dialog.dismiss()
                                                    startActivity(
                                                        Intent(
                                                            context,
                                                            UserScreenActivity::class.java
                                                        )
                                                    )
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

                            showAlert(
                                context,
                                getString(R.string.empty_text_error)
                            )
                        }
                    }

                }

                Glide.with(context)
                    .load("")
                    .thumbnail(Glide.with(context).load(R.drawable.placeholder))
                    .fitCenter()
                    .into(selectedImageView)
                val builder = MaterialAlertDialogBuilder(context)
                builder.setCancelable(false)
                builder.setView(insalesUpdateProductImageLayout)

                val alert = builder.create()
                alert.show()

                cancelDialogBtn.setOnClickListener {
                    alert.dismiss()
                }

                updateImageBtn.setOnClickListener {

                    if (selectedImageBase64String.isNotEmpty() || selectedInternetImage.isNotEmpty()) {
                        alert.dismiss()
                        startLoading(context)

                        viewModel.callAddProductImage(
                            context,
                            shopName,
                            email,
                            password,
                            selectedImageBase64String,
                            pItem.id,
                            "${System.currentTimeMillis()}.jpg",
                            if (intentType != 3) {
                                ""
                            } else {
                                selectedInternetImage
                            }
                        )
                        viewModel.getAddProductImageResponse()
                            .observe(this@SalesCustomersActivity, Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        selectedImageBase64String = ""
                                        selectedInternetImage = ""
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            dismiss()
                                            showProducts()
                                        }, 6000)
                                    } else {
                                        dismiss()
                                        showAlert(context, response.get("message").asString)
                                    }
                                } else {
                                    dismiss()
                                    showAlert(context, getString(R.string.something_wrong_error))
                                }
                            })
                    } else {
                        showAlert(context, getString(R.string.image_attach_error))
                    }
                }
            }

            override fun onItemRemoveClick(position: Int,imagePosition:Int) {
                val imageItem = productsList[position].productImages!![imagePosition]


                MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.remove_text))
                    .setMessage(getString(R.string.image_remove_warning_message))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel_text)){dialog,which->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.remove_text)){dialog,which->
                        dialog.dismiss()
                        startLoading(context)
                        viewModel.callRemoveProductImage(
                            context,
                            shopName,
                            email,
                            password,
                            imageItem.productId,
                            imageItem.id
                        )
                        viewModel.getRemoveProductImageResponse()
                            .observe(this@SalesCustomersActivity, Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            dismiss()
                                            showProducts()
                                        }, 3000)
                                    } else {
                                        dismiss()
                                        showAlert(context, response.get("message").asString)
                                    }
                                } else {
                                    dismiss()
                                    showProducts()
                                }
                            })

                    }.create().show()

            }

        })
        startLoading(context,getString(R.string.please_wait_products_message))

        viewModel.callProducts(context, shopName, email, password)
        viewModel.getSalesProductsResponse().observe(this, Observer { response ->

            if (response != null) {
                if (response.get("status").asString == "200") {
                    if (menu != null){
                        menu!!.findItem(R.id.insales_logout).isVisible = true
                    }
                    val products = response.getAsJsonArray("products")
                    if (products.size() > 0) {
                        productsList.clear()
                        for (i in 0 until products.size()) {
                            val product = products.get(i).asJsonObject
                            val imagesArray = product.getAsJsonArray("images")
                            val imagesList = mutableListOf<ProductImages>()
                            if (imagesArray.size() > 0) {
                                for (j in 0 until imagesArray.size()) {
                                    val imageItem = imagesArray[j].asJsonObject
                                    imagesList.add(
                                        ProductImages(
                                            imageItem.get("id").asInt,
                                            imageItem.get("product_id").asInt,
                                            imageItem.get("url").asString,
                                            imageItem.get("position").asInt
                                        )
                                    )
                                }
                            }
                            productsList.add(
                                Product(
                                    product.get("id").asInt,
                                    product.get("title").asString,
                                    imagesList as ArrayList<ProductImages>
                                )
                            )
                        }
                        dismiss()
                        if (productsList.size > 0) {
                            adapter.notifyItemRangeChanged(0, productsList.size)
                        }
                    } else {
                        dismiss()
                    }
                } else {
                    dismiss()
                    showAlert(context, response.get("message").asString)
                }
            } else {
                dismiss()
            }
        })
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
                    currentPhotoPath = ImageManager.getRealPathFromUri(context, imageUri.data)
                    selectedImageBase64String =
                        ImageManager.convertImageToBase64(context, currentPhotoPath!!)
                    Log.d("TEST199", selectedImageBase64String)
                    Glide.with(context)
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(selectedImageView)
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
                selectedImageBase64String =
                    ImageManager.convertImageToBase64(context, currentPhotoPath!!)
                Log.d("TEST199", selectedImageBase64String)
                Glide.with(context)
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)
            }
        }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(context, bitmap).absolutePath
    }

    override fun onResume() {
        super.onResume()

        checkInsalesAccount()
    }

    private fun checkInsalesAccount(){
        val insalesStatus = appSettings.getString("INSALES_STATUS")

        if (insalesStatus!!.isNotEmpty() && insalesStatus == "logged") {
            insalesLoginWrapperLayout.visibility = View.GONE
            insalesDataWrapperLayout.visibility = View.VISIBLE
            if (menu != null){
                menu!!.findItem(R.id.insales_logout).isVisible = true
            }


            shopName = appSettings.getString("INSALES_SHOP_NAME") as String
            email = appSettings.getString("INSALES_EMAIL") as String
            password = appSettings.getString("INSALES_PASSWORD") as String

            if (productsList.size == 0) {
                showProducts()
            }

        } else {
            insalesDataWrapperLayout.visibility = View.GONE
            insalesLoginWrapperLayout.visibility = View.VISIBLE
            if (menu != null){
                menu!!.findItem(R.id.insales_logout).isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.insales_main_menu,menu)
        this.menu = menu
        return true
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.insales_login_btn -> {
                if (validation()) {
                    val shopName = insalesShopNameBox.text.toString().trim()
                    val email = insalesEmailBox.text.toString().trim()
                    val password = insalesPasswordBox.text.toString().trim()
                    inSalesLogin(shopName, email, password)
                }
            }
            else -> {

            }
        }
    }

    private fun validation(): Boolean {
        if (insalesShopNameBox.text.toString().trim().isEmpty()) {
            showAlert(context, getString(R.string.empty_text_error))
            return false
        } else if (insalesEmailBox.text.toString().trim().isEmpty()) {
            showAlert(context, getString(R.string.empty_text_error))
            return false
        } else if (!Pattern.compile(Constants.emailPattern)
                .matcher(insalesEmailBox.text.toString().trim())
                .matches()
        ) {
            showAlert(context, getString(R.string.email_valid_error))
            return false
        } else if (insalesPasswordBox.text.toString().trim().isEmpty()) {
            showAlert(context, getString(R.string.empty_text_error))
            return false
        }
        return true
    }

}