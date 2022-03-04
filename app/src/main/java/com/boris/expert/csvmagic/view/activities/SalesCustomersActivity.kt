package com.boris.expert.csvmagic.view.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
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
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.model.Category
import com.boris.expert.csvmagic.model.Product
import com.boris.expert.csvmagic.model.ProductImages
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.paperdb.Paper
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
    private lateinit var insalesSearchWrapperLayout: LinearLayout
    private lateinit var insalesShopNameBox: TextInputEditText
    private lateinit var insalesEmailBox: TextInputEditText
    private lateinit var insalesPasswordBox: TextInputEditText
    private lateinit var insalesLoginBtn: MaterialButton
    private var productsList = mutableListOf<Product>()
    private var originalProductsList = mutableListOf<Product>()
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
    private var menu: Menu? = null
    private lateinit var searchResetBtn: MaterialTextView
    private lateinit var searchBox: TextInputEditText
    private lateinit var searchImageBtn: ImageButton
    private var currentPage = 1
    private var currentTotalProducts = 0
    private lateinit var linearLayoutManager: WrapContentLinearLayoutManager
    private var dialogStatus = 0
    private var categoriesList = mutableListOf<Category>()
    private lateinit var fullDescriptionBox: TextInputEditText
    private lateinit var titleBox: TextInputEditText

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
        insalesSearchWrapperLayout = findViewById(R.id.insales_search_products_layout)
        insalesShopNameBox = findViewById(R.id.insales_login_shop_name_box)
        insalesEmailBox = findViewById(R.id.insales_login_email_box)
        insalesPasswordBox = findViewById(R.id.insales_login_password_box)
        insalesLoginBtn = findViewById(R.id.insales_login_btn)
        insalesLoginBtn.setOnClickListener(this)
        productsRecyclerView = findViewById(R.id.insales_products_recyclerview)
        searchResetBtn = findViewById(R.id.insales_products_search_reset_btn)
        searchResetBtn.setOnClickListener(this)
        searchBox = findViewById(R.id.insales_products_search_box)
        searchImageBtn = findViewById(R.id.insales_products_search_btn)
        searchImageBtn.setOnClickListener(this)


        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                search(query)
            }

        })

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
        } else if (item.itemId == R.id.insales_logout) {
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
                    originalProductsList.clear()
                    productsList.clear()
                    Paper.book().delete(Constants.cacheProducts)
                    startActivity(Intent(context, SalesCustomersActivity::class.java))

                }
                .create().show()
            true
        } else if (item.itemId == R.id.insales_data_filter) {
            if (categoriesList.size == 0) {
                viewModel.callCategories(context, shopName, email, password)
                viewModel.getCategoriesResponse().observe(this, Observer { response ->
                    if (response != null) {
                        if (response.get("status").asString == "200") {
                            val categories = response.get("categories").asJsonArray
                            if (categories.size() > 0) {
                                for (i in 0 until categories.size()) {
                                    val category = categories[i].asJsonObject
                                    categoriesList.add(
                                        Category(
                                            category.get("title").asString,
                                            category.get("id").asInt
                                        )
                                    )
                                }
                                //categoriesList.add(Category("Test Category",2767276))
                            }
                        }
                    }
                })
            }
            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            builder.setTitle(getString(R.string.sorting_heading_text))
            builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }

            val arrayAdapter =
                ArrayAdapter(
                    context,
                    android.R.layout.select_dialog_singlechoice,
                    getSortingList(context)
                )
            builder.setAdapter(arrayAdapter, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog!!.dismiss()
                    if (which == 2) {
                        displayCategoryFilterDialog(categoriesList)
                    } else {
                        sorting(which)
                    }

                }

            })
            val alert = builder.create()
            alert.show()
            true
        } else if (item.itemId == R.id.insales_data_sync) {
            currentPage = 1
            dialogStatus = 1
            fetchProducts(currentPage)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun displayCategoryFilterDialog(categoriesList: MutableList<Category>) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.filter_category_heading_text))
        builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
            dialog.dismiss()
        }

        val arrayAdapter =
            ArrayAdapter(context, android.R.layout.select_dialog_singlechoice, categoriesList)
        builder.setAdapter(arrayAdapter, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog!!.dismiss()
                val id = categoriesList[which].id
                searchByCategory(id)
            }

        })
        val alert = builder.create()
        alert.show()
    }

    private fun inSalesLogin(shopName: String, email: String, password: String) {

        startLoading(context, getString(R.string.please_wait_login_message))
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
                    menu!!.findItem(R.id.insales_data_filter).isVisible = true
                    menu!!.findItem(R.id.insales_data_sync).isVisible = true
                    showProducts()
                } else {
                    showAlert(context, response.get("message").asString)
                }
            }
        })
    }

    lateinit var selectedImageView: AppCompatImageView
    private fun showProducts() {
        linearLayoutManager = WrapContentLinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        productsRecyclerView.layoutManager = linearLayoutManager
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
                                                currentPhotoPath = ImageManager.getRealPathFromUri(
                                                    context,
                                                    Uri.parse(result)
                                                )!!
                                                Glide.with(context)
                                                    .load(currentPhotoPath)
                                                    .placeholder(R.drawable.placeholder)
                                                    .centerInside()
                                                    .into(selectedImageView)
                                                selectedImageBase64String =
                                                    ImageManager.convertImageToBase64(
                                                        context,
                                                        currentPhotoPath!!
                                                    )
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

            override fun onItemRemoveClick(position: Int, imagePosition: Int) {
                val imageItem = productsList[position].productImages!![imagePosition]


                MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.remove_text))
                    .setMessage(getString(R.string.image_remove_warning_message))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.remove_text)) { dialog, which ->
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

            override fun onItemEditImageClick(position: Int) {
                val pItem = productsList[position]
                val dialogLayout = LayoutInflater.from(context)
                    .inflate(R.layout.insales_product_detail_update_dialog_layout, null)
                val dialogHeading = dialogLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
                titleBox =
                    dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_title_input_field)
                val productShortDescriptionBox =
                    dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_short_desc_input_field)
                fullDescriptionBox =
                    dialogLayout.findViewById(R.id.insales_product_full_desc_input_field)
                val getDescriptionView =
                    dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view)

                titleBox.setText(pItem.title)
                productShortDescriptionBox.setText(pItem.shortDesc)
                fullDescriptionBox.setText(pItem.fullDesc)
                val dialogCancelBtn =
                    dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_cancel_btn)
                val dialogUpdateBtn =
                    dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_update_btn)

                val builder = MaterialAlertDialogBuilder(context)
                    .setView(dialogLayout)
                    .setCancelable(false)
                val alert = builder.create()
                alert.show()
                titleBox.setSelection(pItem.title.length)
                titleBox.requestFocus()
                Constants.openKeyboar(context)
                dialogCancelBtn.setOnClickListener {
                    Constants.hideKeyboar(context)
                    alert.dismiss()
                }

                getDescriptionView.setOnClickListener {
                    Constants.hideKeyboar(context)
                    userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

                    if (userCurrentCredits.toFloat() >= 1.0) {
                        launchActivity.launch(Intent(context, RainForestApiActivity::class.java))
                    } else {
                        MaterialAlertDialogBuilder(context)
                            .setMessage(getString(R.string.low_credites_error_message2))
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
                    //startActivity(Intent(context,RainForestApiActivity::class.java))

                }

                dialogUpdateBtn.setOnClickListener {
                    val titleText = titleBox.text.toString().trim()
                    val shortDesc = productShortDescriptionBox.text.toString().trim()
                    val fullDesc = fullDescriptionBox.text.toString().trim()

                    if (titleText.isNotEmpty()) {
                        Constants.hideKeyboar(context)
                        alert.dismiss()
//                        startLoading(
//                            context,
//                            getString(R.string.please_wait_product_update_message)
//                        )
                        pItem.title = titleText
                        pItem.shortDesc = shortDesc
                        pItem.fullDesc = fullDesc
                        Paper.book().delete(Constants.cacheProducts)
                        Paper.book().write(Constants.cacheProducts, originalProductsList)
                        adapter.notifyItemChanged(position)

                        viewModel.callUpdateProductDetail(
                            context,
                            shopName,
                            email,
                            password,
                            pItem.id,
                            titleText,
                            shortDesc,
                            fullDesc
                        )
                        viewModel.getUpdateProductDetailResponse()
                            .observe(this@SalesCustomersActivity, Observer { response ->
                                if (response != null) {
                                    if (response.get("status").asString == "200") {
//                                        Handler(Looper.myLooper()!!).postDelayed({
                                        dismiss()
//                                            //showProducts()
//                                        }, 3000)
                                        Toast.makeText(
                                            context,
                                            getString(R.string.product_updated_successfully),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        dismiss()
                                        showAlert(context, response.get("message").asString)
                                    }
                                } else {
                                    dismiss()
                                }
                            })
                    } else {
                        showAlert(context, getString(R.string.empty_text_error))
                    }
                }
            }

        })

        var pastVisiblesItems: Int
        var visibleItemCount: Int
        var totalItemCount: Int

        productsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.childCount
                    totalItemCount = linearLayoutManager.itemCount
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                             if (currentTotalProducts == 250){
//                                 currentPage +=1
//                                 fetchProducts(currentPage)
//                             }
//                            else{
//                                Toast.makeText(context,getString(R.string.fetch_all_products),Toast.LENGTH_SHORT).show()
//                            }
                    }

                }
            }
        })
        val cacheList: ArrayList<Product>? = Paper.book().read(Constants.cacheProducts)

        if (cacheList != null && cacheList.size > 0) {
            originalProductsList.addAll(cacheList)
            productsList.addAll(originalProductsList)
            adapter.notifyItemRangeChanged(0, productsList.size)
            Handler(Looper.myLooper()!!).postDelayed({
                if (menu != null) {
                    menu!!.findItem(R.id.insales_logout).isVisible = true
                    menu!!.findItem(R.id.insales_data_filter).isVisible = true
                    menu!!.findItem(R.id.insales_data_sync).isVisible = true
                }
            }, 3000)
        } else {
            dialogStatus = 1
            fetchProducts(currentPage)
        }

    }

    var launchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                if (data != null && data.hasExtra("TITLE")) {
                    val title = data.getStringExtra("TITLE") as String
                    if (title.isNotEmpty()) {
                        titleBox.setText(title)
                        titleBox.setSelection(titleBox.length())
                        //titleBox.requestFocus()
                    }
                }

                if (data != null && data.hasExtra("DESCRIPTION")) {
                    val description = data.getStringExtra("DESCRIPTION") as String
                    if (description.isNotEmpty()) {
                        fullDescriptionBox.setText(description)
                        fullDescriptionBox.setSelection(fullDescriptionBox.length())
                        fullDescriptionBox.requestFocus()
                    }
                }
            }
        }


    private fun fetchProducts(page: Int) {
        if (dialogStatus == 1) {
            startLoading(context, getString(R.string.please_wait_products_message))
        }
        viewModel.callProducts(context, shopName, email, password, page)
        viewModel.getSalesProductsResponse().observe(this, Observer { response ->

            if (response != null) {
                if (response.get("status").asString == "200") {
                    dialogStatus = 0
                    if (menu != null) {
                        menu!!.findItem(R.id.insales_logout).isVisible = true
                        menu!!.findItem(R.id.insales_data_filter).isVisible = true
                        menu!!.findItem(R.id.insales_data_sync).isVisible = true
                    }
                    val products = response.getAsJsonArray("products")
                    if (products.size() > 0) {
                        productsList.clear()
                        originalProductsList.clear()
                        Paper.book().delete(Constants.cacheProducts)
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
                            originalProductsList.add(
                                Product(
                                    product.get("id").asInt,
                                    product.get("category_id").asInt,
                                    product.get("title").asString,
                                    if (product.get("short_description").isJsonNull) {
                                        ""
                                    } else {
                                        product.get("short_description").asString
                                    },
                                    if (product.get("description").isJsonNull) {
                                        ""
                                    } else {
                                        product.get("description").asString
                                    },
                                    imagesList as ArrayList<ProductImages>
                                )
                            )

                            Paper.book().write(Constants.cacheProducts, originalProductsList)
                            currentTotalProducts = originalProductsList.size
                        }
                        dismiss()
                        val cacheList: ArrayList<Product>? =
                            Paper.book().read(Constants.cacheProducts)
                        if (cacheList != null && cacheList.size > 0) {
                            originalProductsList.clear()
                            originalProductsList.addAll(cacheList)
                            productsList.addAll(originalProductsList)
                            adapter.notifyItemRangeChanged(0, productsList.size)
                        }
//                            if (originalProductsList.size > 0) {
//                                productsList.addAll(originalProductsList)
//                                adapter.notifyItemRangeChanged(0, productsList.size)
//                            }
                        if (currentTotalProducts == 250) {
                            currentPage += 1
                            fetchProducts(currentPage)
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

    private fun checkInsalesAccount() {
        val insalesStatus = appSettings.getString("INSALES_STATUS")

        if (insalesStatus!!.isNotEmpty() && insalesStatus == "logged") {
            insalesLoginWrapperLayout.visibility = View.GONE
            insalesSearchWrapperLayout.visibility = View.VISIBLE
            insalesDataWrapperLayout.visibility = View.VISIBLE


            shopName = appSettings.getString("INSALES_SHOP_NAME") as String
            email = appSettings.getString("INSALES_EMAIL") as String
            password = appSettings.getString("INSALES_PASSWORD") as String

//            if (originalProductsList.size == 0) {
            showProducts()
//            }
//            else{
//                Handler(Looper.myLooper()!!).postDelayed({
//                    if (menu != null) {
//                        menu!!.findItem(R.id.insales_logout).isVisible = true
//                        menu!!.findItem(R.id.insales_data_filter).isVisible = true
//                        menu!!.findItem(R.id.insales_data_sync).isVisible = true
//                    }
//                },2000)
//
//            }

        } else {
            insalesDataWrapperLayout.visibility = View.GONE
            insalesSearchWrapperLayout.visibility = View.GONE
            insalesLoginWrapperLayout.visibility = View.VISIBLE

            if (menu != null) {
                menu!!.findItem(R.id.insales_logout).isVisible = false
                menu!!.findItem(R.id.insales_data_filter).isVisible = false
                menu!!.findItem(R.id.insales_data_sync).isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.insales_main_menu, menu)
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
            R.id.insales_products_search_reset_btn -> {
                hideSoftKeyboard(context, searchBox)
                if (searchBox.text.toString().trim().isNotEmpty()) {
                    searchBox.setText("")
                }
                if (productsList.isNotEmpty()) {
                    productsList.clear()
                }
                productsList.addAll(originalProductsList)
                adapter.notifyItemRangeChanged(0, productsList.size)

            }
            R.id.insales_products_search_btn -> {
                val query = searchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    Constants.hideKeyboar(context)
                    search(query)
                } else {
                    showAlert(context, getString(R.string.empty_text_error))
                }
            }
            else -> {

            }
        }
    }

    private fun searchByCategory(id: Int?) {
        val matchedProducts = mutableListOf<Product>()

        id?.let {
            productsList.forEach { item ->
                if (item.categoryId == id) {
                    matchedProducts.add(item)
                }
            }

            if (matchedProducts.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.category_products_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                productsList.clear()
                productsList.addAll(matchedProducts)
                adapter.notifyItemRangeChanged(0, productsList.size)
            }
        }
    }

    private fun search(text: String?) {
        val matchedProducts = mutableListOf<Product>()

        text?.let {
            productsList.forEach { item ->
                if (item.title.contains(text, true)) {
                    matchedProducts.add(item)
                }
            }

            if (matchedProducts.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.no_match_found_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                productsList.clear()
                productsList.addAll(matchedProducts)
                adapter.notifyItemRangeChanged(0, productsList.size)
            }
        }
    }

    private fun sorting(type: Int) {
        if (productsList.size > 0) {
            Collections.sort(productsList, object : Comparator<Product> {
                override fun compare(o1: Product?, o2: Product?): Int {
                    return if (type == 0) { // A-Z
                        o1!!.title.compareTo(o2!!.title, true)
                    } else if (type == 1) { // Z-A
                        o2!!.title.compareTo(o1!!.title, true)
                    } else {
                        -1
                    }
                }

            })
            adapter.notifyDataSetChanged()
        } else {
            showAlert(context, getString(R.string.empty_list_error_message))
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