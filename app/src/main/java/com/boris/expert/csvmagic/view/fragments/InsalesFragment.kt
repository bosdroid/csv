package com.boris.expert.csvmagic.view.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.InSalesProductsAdapter
import com.boris.expert.csvmagic.adapters.InternetImageAdapter
import com.boris.expert.csvmagic.adapters.KeywordsAdapter
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.GrammarCallback
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.model.Category
import com.boris.expert.csvmagic.model.KeywordObject
import com.boris.expert.csvmagic.model.Product
import com.boris.expert.csvmagic.model.ProductImages
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.boris.expert.csvmagic.view.activities.MainActivity
import com.boris.expert.csvmagic.view.activities.RainForestApiActivity
import com.boris.expert.csvmagic.view.activities.UserScreenActivity
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import io.paperdb.Paper
import net.expandable.ExpandableTextView
import org.apmem.tools.layouts.FlowLayout
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern


class InsalesFragment : Fragment(), View.OnClickListener {

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
    private var currentPhotoPath: String? = null
    private var selectedImageBase64String: String = ""
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
    private var originalCategoriesList = mutableListOf<Category>()
    private var categoriesList = mutableListOf<Category>()
    private lateinit var fullDescriptionBox: TextInputEditText
    private lateinit var titleBox: TextInputEditText
    private var titleTextViewList = mutableListOf<MaterialTextView>()
    private var shortDescTextViewList = mutableListOf<MaterialTextView>()
    private var fullDescTextViewList = mutableListOf<MaterialTextView>()
    private var keywordsList = mutableListOf<KeywordObject>()
    private lateinit var keywordsAdapter: KeywordsAdapter
    private lateinit var selectedImageView: AppCompatImageView
    private lateinit var fabAddProduct:FloatingActionButton
    private var categoryList = mutableListOf<Category>()
    private var defaultLayout = 0
    private var selectedCategoryId:Int = 0

    companion object {
        private lateinit var dynamicTitleTextViewWrapper: FlowLayout
        private lateinit var dynamicFullDescTextViewWrapper: FlowLayout
        private lateinit var dynamicKeywordsTextViewWrapper: FlowLayout
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(context)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(SalesCustomersViewModel()).createFor()
        )[SalesCustomersViewModel::class.java]
        keywordsList.add(KeywordObject("Keyword1", 1))
        keywordsList.add(KeywordObject("Keyword2", 1))
        keywordsList.add(KeywordObject("Keyword3", 1))
        keywordsList.add(KeywordObject("Keyword4", 1))
        keywordsList.add(KeywordObject("Keyword5", 1))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.insales_logout) {
            MaterialAlertDialogBuilder(requireActivity())
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
//                    startActivity(Intent(context, SalesCustomersActivity::class.java))
                    checkInsalesAccount()

                }
                .create().show()
            true
        } else if (item.itemId == R.id.insales_data_filter) {
            if (categoriesList.size == 0) {
                viewModel.callCategories(requireActivity(), shopName, email, password)
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

            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setCancelable(false)
            builder.setTitle(getString(R.string.sorting_heading_text))
            builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }

            val arrayAdapter =
                ArrayAdapter(
                    requireActivity(),
                    android.R.layout.select_dialog_item,
                    BaseActivity.getSortingList(requireActivity())
                )
            builder.setAdapter(arrayAdapter, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog!!.dismiss()
                    if (which == 0) {
                        resetProductList()
                    } else if (which == 3) {
                        displayCategoryFilterDialog(categoriesList)
                    } else if (which == 4) {
                        displayErrorItems()
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

    private fun getCategories(){
        viewModel.callCategories(requireActivity(), shopName, email, password)
        viewModel.getCategoriesResponse().observe(this, Observer { response ->
            if (response != null) {
                if (response.get("status").asString == "200") {
                    val categories = response.get("categories").asJsonArray
                    if (categories.size() > 0) {
                        for (i in 0 until categories.size()) {
                            val category = categories[i].asJsonObject
                            originalCategoriesList.add(
                                Category(
                                    category.get("title").asString,
                                    category.get("id").asInt
                                )
                            )
                        }
                        if (originalCategoriesList.size > 0) {
                            selectedCategoryId = originalCategoriesList[0].id
                        }
                    }
                }
            }
        })
    }

    private fun resetProductList() {
        productsList.clear()
        productsList.addAll(originalProductsList)
        adapter.notifyItemRangeChanged(0, productsList.size)
    }

    private fun displayErrorItems() {
        val matchedProducts = mutableListOf<Product>()
        productsList.forEach { item ->
            if (item.title.length < 10 || item.fullDesc.length < 10 || item.productImages!!.size == 0) {
                matchedProducts.add(item)
            }
        }

        if (matchedProducts.isEmpty()) {
            Toast.makeText(
                requireActivity(),
                getString(R.string.error_products_not_found),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            productsList.clear()
            productsList.addAll(matchedProducts)
            adapter.notifyDataSetChanged()
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
            BaseActivity.showAlert(requireActivity(), getString(R.string.empty_list_error_message))
        }
    }

    private fun displayCategoryFilterDialog(categoriesList: MutableList<Category>) {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.filter_category_heading_text))
        builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
            dialog.dismiss()
        }

        val arrayAdapter =
            ArrayAdapter(
                requireActivity(),
                android.R.layout.select_dialog_singlechoice,
                categoriesList
            )
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
                    requireActivity(),
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        //requireActivity().menuInflater.inflate(R.menu.insales_main_menu,menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_insales, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View) {
        insalesLoginWrapperLayout = view.findViewById(R.id.insales_login_wrapper_layout)
        insalesDataWrapperLayout = view.findViewById(R.id.insales_data_wrapper_layout)
        insalesSearchWrapperLayout = view.findViewById(R.id.insales_search_products_layout)
        insalesShopNameBox = view.findViewById(R.id.insales_login_shop_name_box)
        insalesEmailBox = view.findViewById(R.id.insales_login_email_box)
        insalesPasswordBox = view.findViewById(R.id.insales_login_password_box)
        insalesLoginBtn = view.findViewById(R.id.insales_login_btn)
        insalesLoginBtn.setOnClickListener(this)
        productsRecyclerView = view.findViewById(R.id.insales_products_recyclerview)
        searchResetBtn = view.findViewById(R.id.insales_products_search_reset_btn)
        searchResetBtn.setOnClickListener(this)
        searchBox = view.findViewById(R.id.insales_products_search_box)
        searchImageBtn = view.findViewById(R.id.insales_products_search_btn)
        searchImageBtn.setOnClickListener(this)
        fabAddProduct = view.findViewById(R.id.fab)
        fabAddProduct.setOnClickListener(this)


        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    resetProductList()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                search(query)
            }

        })
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
            fabAddProduct.visibility = View.VISIBLE


            shopName = appSettings.getString("INSALES_SHOP_NAME") as String
            email = appSettings.getString("INSALES_EMAIL") as String
            password = appSettings.getString("INSALES_PASSWORD") as String
            getCategories()
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
            fabAddProduct.visibility = View.GONE
            insalesLoginWrapperLayout.visibility = View.VISIBLE

            if (menu != null) {
                menu!!.findItem(R.id.insales_logout).isVisible = false
                menu!!.findItem(R.id.insales_data_filter).isVisible = false
                menu!!.findItem(R.id.insales_data_sync).isVisible = false
            }
        }
    }

    private fun showProducts() {
        linearLayoutManager = WrapContentLinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
        productsRecyclerView.layoutManager = linearLayoutManager
        productsRecyclerView.hasFixedSize()
        adapter = InSalesProductsAdapter(
            requireActivity(),
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
                    val builder = MaterialAlertDialogBuilder(requireActivity())
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
                        requireActivity(),
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
                                                currentPhotoPath = ImageManager.getRealPathFromUri(
                                                    requireActivity(),
                                                    Uri.parse(result)
                                                )!!
                                                Glide.with(requireActivity())
                                                    .load(currentPhotoPath)
                                                    .placeholder(R.drawable.placeholder)
                                                    .centerInside()
                                                    .into(selectedImageView)
                                                selectedImageBase64String =
                                                    ImageManager.convertImageToBase64(
                                                        requireActivity(),
                                                        currentPhotoPath!!
                                                    )
                                                iAlert.dismiss()
                                            } else {
                                                BaseActivity.showAlert(
                                                    requireActivity(),
                                                    getString(R.string.something_wrong_error)
                                                )
                                            }
                                        }

                                    })
                            } else {
                                if (loader.visibility == View.VISIBLE) {
                                    loader.visibility = View.INVISIBLE
                                }
                                BaseActivity.showAlert(
                                    requireActivity(),
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
                                                    startActivity(
                                                        Intent(
                                                            requireActivity(),
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

                            BaseActivity.showAlert(
                                requireActivity(),
                                getString(R.string.empty_text_error)
                            )
                        }
                    }

                }


                Glide.with(requireActivity())
                    .load(imageItem.imageUrl)
                    .thumbnail(Glide.with(requireActivity()).load(R.drawable.loader))
                    .fitCenter()
                    .into(selectedImageView)
                val builder = MaterialAlertDialogBuilder(requireActivity())
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
                        BaseActivity.startLoading(requireActivity())

                        viewModel.callUpdateProductImage(
                            requireActivity(),
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
                            .observe(requireActivity(), Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        selectedImageBase64String = ""
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            BaseActivity.dismiss()
                                            fetchProducts()//showProducts()
                                        }, 3000)
                                    } else {
                                        BaseActivity.dismiss()
                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            response.get("message").asString
                                        )
                                    }
                                } else {
                                    BaseActivity.dismiss()
                                    fetchProducts()//showProducts()
                                }
                            })
                    } else {
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.image_attach_error)
                        )
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
                    val builder = MaterialAlertDialogBuilder(requireActivity())
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
                        requireActivity(),
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
                            Glide.with(requireActivity())
                                .load(selectedInternetImage)
                                .thumbnail(
                                    Glide.with(requireActivity()).load(R.drawable.placeholder)
                                )
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
                                                    startActivity(
                                                        Intent(
                                                            requireActivity(),
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

                            BaseActivity.showAlert(
                                requireActivity(),
                                getString(R.string.empty_text_error)
                            )
                        }
                    }

                }

                Glide.with(requireActivity())
                    .load("")
                    .thumbnail(Glide.with(requireActivity()).load(R.drawable.placeholder))
                    .fitCenter()
                    .into(selectedImageView)
                val builder = MaterialAlertDialogBuilder(requireActivity())
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
                        BaseActivity.startLoading(requireActivity())

                        viewModel.callAddProductImage(
                            requireActivity(),
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
                            .observe(requireActivity(), Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        selectedImageBase64String = ""
                                        selectedInternetImage = ""
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            BaseActivity.dismiss()
                                            fetchProducts()//showProducts()
                                        }, 6000)
                                    } else {
                                        BaseActivity.dismiss()
                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            response.get("message").asString
                                        )
                                    }
                                } else {
                                    BaseActivity.dismiss()
                                    BaseActivity.showAlert(
                                        requireActivity(),
                                        getString(R.string.something_wrong_error)
                                    )
                                }
                            })
                    } else {
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.image_attach_error)
                        )
                    }
                }
            }

            override fun onItemRemoveClick(position: Int, imagePosition: Int) {
                val imageItem = productsList[position].productImages!![imagePosition]


                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.remove_text))
                    .setMessage(getString(R.string.image_remove_warning_message))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.remove_text)) { dialog, which ->
                        dialog.dismiss()
                        BaseActivity.startLoading(requireActivity())
                        viewModel.callRemoveProductImage(
                            requireActivity(),
                            shopName,
                            email,
                            password,
                            imageItem.productId,
                            imageItem.id
                        )
                        viewModel.getRemoveProductImageResponse()
                            .observe(requireActivity(), Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            BaseActivity.dismiss()
                                            fetchProducts()//showProducts()
                                        }, 3000)
                                    } else {
                                        BaseActivity.dismiss()
                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            response.get("message").asString
                                        )
                                    }
                                } else {
                                    BaseActivity.dismiss()
                                    fetchProducts()//showProducts()
                                }
                            })

                    }.create().show()

            }

            override fun onItemEditImageClick(position: Int) {
                val pItem = productsList[position]
                CustomDialog(pItem,position,adapter,viewModel).show(childFragmentManager,"dialog")

//                val dialogLayout = LayoutInflater.from(requireActivity())
//                    .inflate(R.layout.insales_product_detail_update_dialog_layout, null)
//                val dialogHeading = dialogLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
//                val swapLayoutBtn = dialogLayout.findViewById<MaterialCheckBox>(R.id.layout_swap)
//                val firstLinearLayout =
//                    dialogLayout.findViewById<LinearLayout>(R.id.first_linear_layout)
//                val secondLinearLayout =
//                    dialogLayout.findViewById<LinearLayout>(R.id.second_linear_layout)
//                dynamicTitleTextViewWrapper =
//                    dialogLayout.findViewById(R.id.dynamic_insales_title_textview_wrapper)
////                dynamicTitleTextViewWrapper.setOnDragListener(MyDragListener())
//                val dynamicShortDescTextViewWrapper =
//                    dialogLayout.findViewById<FlowLayout>(R.id.dynamic_insales_short_description_textview_wrapper)
//                dynamicFullDescTextViewWrapper =
//                    dialogLayout.findViewById(R.id.dynamic_insales_full_description_textview_wrapper)
////                dynamicTitleTextViewWrapper.setOnDragListener(MyDragListener())
//                val keywordsRecyclerView =
//                    dialogLayout.findViewById<RecyclerView>(R.id.keywords_recyclerview)
//                keywordsRecyclerView.layoutManager =
//                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//                keywordsRecyclerView.hasFixedSize()
//                keywordsAdapter = KeywordsAdapter(
//                    requireActivity(),
//                    keywordsList as ArrayList<KeywordObject>
//                )
//                keywordsRecyclerView.adapter = keywordsAdapter
//
//                keywordsAdapter.setOnItemClickListener(object :
//                    KeywordsAdapter.OnItemClickListener {
//                    override fun onItemClick(position: Int) {
//                        val item = keywordsList[position]
//                        val builder = MaterialAlertDialogBuilder(requireActivity())
//                        val options = arrayOf("+add in title", "+add in description")
//                        var isTitleChecked = false
//                        var isDescriptionChecked = false
//                        val checkedItems = booleanArrayOf(false, false)
//                        builder.setMultiChoiceItems(
//                            options,
//                            checkedItems
//                        ) { dialog, which, isCheck ->
//                            when (which) {
//                                0 -> {
//                                    isTitleChecked = isCheck
//                                }
//                                1 -> {
//                                    isDescriptionChecked = isCheck
//                                }
//                                else -> {
//
//                                }
//                            }
//                        }
//                        builder.setPositiveButton(requireActivity().resources.getString(R.string.ok_text)) { dialog, which ->
//
//                            if (isTitleChecked) {
//                                val textView = getMaterialTextView(item.keyword)
//                                dynamicTitleTextViewWrapper.addView(textView, 0)
//                                dynamicTitleTextViewWrapper.invalidate()
//                            }
//
//                            if (isDescriptionChecked) {
//                                val textView = getMaterialTextView(item.keyword)
//                                dynamicFullDescTextViewWrapper.addView(textView, 0)
//                                dynamicFullDescTextViewWrapper.invalidate()
//                            }
//                        }
//                        builder.setNegativeButton(
//                            requireActivity().resources.getString(R.string.cancel_text),
//                            null
//                        )
//                        val alert = builder.create()
//                        alert.show()
//                    }
//
//                    override fun onItemAddTitleClick(position: Int) {
//                        val item = keywordsList[position]
//                        val params = FlowLayout.LayoutParams(
//                            FlowLayout.LayoutParams.WRAP_CONTENT,
//                            FlowLayout.LayoutParams.WRAP_CONTENT
//                        )
//                        params.setMargins(5, 5, 5, 5)
//                        val textView = MaterialTextView(requireActivity())
//                        textView.layoutParams = params
//                        textView.text = item.keyword
//                        textView.tag = "title"
//                        textView.setTextColor(
//                            ContextCompat.getColor(
//                                requireActivity(),
//                                R.color.white
//                            )
//                        )
//                        textView.setBackgroundColor(
//                            ContextCompat.getColor(
//                                requireActivity(),
//                                R.color.primary_positive_color
//                            )
//                        )
//                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                        //titleTextViewList.add(textView)
//                        //textView.setOnClickListener(requireActivity())
//                        textView.setOnTouchListener(ChoiceTouchListener())
//                        textView.setOnDragListener(ChoiceDragListener())
//                        dynamicTitleTextViewWrapper.addView(textView, 0)
//                        //dynamicTitleTextViewWrapper.invalidate()
//
//                    }
//
//                    override fun onItemAddDescriptionClick(position: Int) {
//                        val item = keywordsList[position]
//                        val params = FlowLayout.LayoutParams(
//                            FlowLayout.LayoutParams.WRAP_CONTENT,
//                            FlowLayout.LayoutParams.WRAP_CONTENT
//                        )
//                        params.setMargins(5, 5, 5, 5)
//                        val textView = MaterialTextView(requireActivity())
//                        textView.layoutParams = params
//                        textView.text = item.keyword
//                        textView.tag = "title"
//                        textView.setTextColor(
//                            ContextCompat.getColor(
//                                requireActivity(),
//                                R.color.white
//                            )
//                        )
//                        textView.setBackgroundColor(
//                            ContextCompat.getColor(
//                                requireActivity(),
//                                R.color.primary_positive_color
//                            )
//                        )
//                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                        //titleTextViewList.add(textView)
//                        //textView.setOnClickListener(requireActivity())
//                        textView.setOnTouchListener(ChoiceTouchListener())
//                        textView.setOnDragListener(ChoiceDragListener())
//                        dynamicFullDescTextViewWrapper.addView(textView, 0)
//                        //dynamicTitleTextViewWrapper.invalidate()
//                    }
//
//                })
//
//                secondLinearLayout.visibility = View.VISIBLE
//                titleBox =
//                    dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_title_input_field)
//                val productShortDescriptionBox =
//                    dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_short_desc_input_field)
//                fullDescriptionBox =
//                    dialogLayout.findViewById(R.id.insales_product_full_desc_input_field)
//                val getDescriptionView =
//                    dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view)
//                val getDescriptionView1 =
//                    dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view1)
//
//                titleBox.setText(pItem.title)
//                productShortDescriptionBox.setText(pItem.shortDesc)
//                fullDescriptionBox.setText(pItem.fullDesc)
//                val dialogCancelBtn =
//                    dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_cancel_btn)
//                val dialogUpdateBtn =
//                    dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_update_btn)
//
//                titleTextViewList.clear()
//                shortDescTextViewList.clear()
//                fullDescTextViewList.clear()
//                dynamicTitleTextViewWrapper.removeAllViews()
//                dynamicShortDescTextViewWrapper.removeAllViews()
//                dynamicFullDescTextViewWrapper.removeAllViews()
//
//                val titleTextList = pItem.title.trim().split(" ")
//                val shortDescTextList = pItem.shortDesc.trim().split(" ")
//                val fullDescTextList = pItem.fullDesc.trim().split(" ")
//
//                for (i in 0 until titleTextList.size) {
//                    val params = FlowLayout.LayoutParams(
//                        FlowLayout.LayoutParams.WRAP_CONTENT,
//                        FlowLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(5, 5, 5, 5)
//                    val textView = MaterialTextView(requireActivity())
//                    textView.layoutParams = params
//                    textView.text = titleTextList[i].trim()
//                    textView.tag = "title"
//                    textView.id = i
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    titleTextViewList.add(textView)
//                    textView.setOnClickListener(this@InsalesFragment)
////                    textView.setOnTouchListener(ChoiceTouchListener())
////                    textView.setOnDragListener(ChoiceDragListener())
//                    dynamicTitleTextViewWrapper.addView(textView)
//                }
//
//                for (i in 0 until shortDescTextList.size) {
//                    val params = FlowLayout.LayoutParams(
//                        FlowLayout.LayoutParams.WRAP_CONTENT,
//                        FlowLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(5, 5, 5, 5)
//                    val textView = MaterialTextView(requireActivity())
//                    textView.layoutParams = params
//                    textView.text = shortDescTextList[i].trim()
//                    textView.tag = "title"
//                    textView.id = i
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    shortDescTextViewList.add(textView)
//                    textView.setOnClickListener(this@InsalesFragment)
////                    textView.setOnTouchListener(ChoiceTouchListener())
////                    textView.setOnDragListener(ChoiceDragListener())
//                    dynamicShortDescTextViewWrapper.addView(textView)
//                }
////
//                for (i in 0 until fullDescTextList.size) {
//                    val params = FlowLayout.LayoutParams(
//                        FlowLayout.LayoutParams.WRAP_CONTENT,
//                        FlowLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(5, 5, 5, 5)
//                    val textView = MaterialTextView(requireActivity())
//                    textView.layoutParams = params
//                    textView.text = fullDescTextList[i].trim()
//                    textView.tag = "title"
//                    textView.id = i
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    fullDescTextViewList.add(textView)
//                    textView.setOnClickListener(this@InsalesFragment)
////                    textView.setOnTouchListener(ChoiceTouchListener())
////                    textView.setOnDragListener(ChoiceDragListener())
//                    dynamicFullDescTextViewWrapper.addView(textView)
//                }
//
//
//
//
//
//                swapLayoutBtn.setOnCheckedChangeListener { buttonView, isChecked ->
//                    if (isChecked) {
//                        secondLinearLayout.visibility = View.GONE
//                        firstLinearLayout.visibility = View.VISIBLE
//                        defaultLayout = 1
//
//                        titleBox.setSelection(pItem.title.length)
//                        titleBox.requestFocus()
//                        Constants.openKeyboar(requireActivity())
//                    } else {
//                        Constants.hideKeyboar(requireActivity())
//                        BaseActivity.startLoading(requireActivity())
//                        firstLinearLayout.visibility = View.GONE
//                        secondLinearLayout.visibility = View.VISIBLE
//                        defaultLayout = 0
////                        val titleTextList = titleBox.text.toString().trim().split(" ")
////                        val shortDescTextList =
////                            productShortDescriptionBox.text.toString().trim().split(
////                                " "
////                            )
////                        val fullDescTextList = fullDescriptionBox.text.toString().trim().split(" ")
////
////                        for (i in 0 until titleTextList.size) {
////                            val params = FlowLayout.LayoutParams(
////                                FlowLayout.LayoutParams.WRAP_CONTENT,
////                                FlowLayout.LayoutParams.WRAP_CONTENT
////                            )
////                            params.setMargins(5, 5, 5, 5)
////                            val textView = MaterialTextView(context)
////                            textView.layoutParams = params
////                            textView.text = titleTextList[i].trim()
////                            textView.tag = "title"
////                            textView.id = i
////                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
////                            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
////                            titleTextViewList.add(textView)
////                            textView.setOnClickListener(this@SalesCustomersActivity)
////                            dynamicTitleTextViewWrapper.addView(textView)
////                        }
////
////                        for (i in 0 until shortDescTextList.size) {
////                            val params = FlowLayout.LayoutParams(
////                                FlowLayout.LayoutParams.WRAP_CONTENT,
////                                FlowLayout.LayoutParams.WRAP_CONTENT
////                            )
////                            params.setMargins(5, 5, 5, 5)
////                            val textView = MaterialTextView(context)
////                            textView.layoutParams = params
////                            textView.text = shortDescTextList[i].trim()
////                            textView.tag = "title"
////                            textView.id = i
////                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
////                            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
////                            shortDescTextViewList.add(textView)
////                            textView.setOnClickListener(this@SalesCustomersActivity)
////                            dynamicShortDescTextViewWrapper.addView(textView)
////                        }
////
////                        for (i in 0 until fullDescTextList.size) {
////                            val params = FlowLayout.LayoutParams(
////                                FlowLayout.LayoutParams.WRAP_CONTENT,
////                                FlowLayout.LayoutParams.WRAP_CONTENT
////                            )
////                            params.setMargins(5, 5, 5, 5)
////                            val textView = MaterialTextView(context)
////                            textView.layoutParams = params
////                            textView.text = fullDescTextList[i].trim()
////                            textView.tag = "title"
////                            textView.id = i
////                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
////                            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
////                            fullDescTextViewList.add(textView)
////                            textView.setOnClickListener(this@SalesCustomersActivity)
////                            dynamicFullDescTextViewWrapper.addView(textView)
////                        }
//                        BaseActivity.dismiss()
//                    }
//                }
//
////                swapLayoutBtn.setOnClickListener {
////                    titleTextViewList.clear()
////                    shortDescTextViewList.clear()
////                    fullDescTextViewList.clear()
////                    dynamicTitleTextViewWrapper.removeAllViews()
////                    dynamicShortDescTextViewWrapper.removeAllViews()
////                    dynamicFullDescTextViewWrapper.removeAllViews()
////
////
////                    if (defaultLayout == 0){
////
////                    }
////                    else{
////
////                    }
////                }
//
//                val builder = MaterialAlertDialogBuilder(requireActivity())
//                    .setView(dialogLayout)
//                    .setCancelable(false)
//                val alert = builder.create()
//                alert.show()
//                titleBox.setSelection(pItem.title.length)
////                titleBox.requestFocus()
////                Constants.openKeyboar(context)
//                dialogCancelBtn.setOnClickListener {
//                    BaseActivity.hideSoftKeyboard(requireActivity(), dialogCancelBtn)
////                    Constants.hideKeyboar(requireActivity())
//                    alert.dismiss()
//                }
//
//                getDescriptionView.setOnClickListener {
//                    Constants.hideKeyboar(requireActivity())
//                    userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//
//                    if (userCurrentCredits.toFloat() >= 1.0) {
//                        launchActivity.launch(
//                            Intent(
//                                requireActivity(),
//                                RainForestApiActivity::class.java
//                            )
//                        )
//                    } else {
//                        MaterialAlertDialogBuilder(requireActivity())
//                            .setMessage(getString(R.string.low_credites_error_message2))
//                            .setCancelable(false)
//                            .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                                dialog.dismiss()
//                            }
//                            .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                                dialog.dismiss()
//                                startActivity(Intent(context, UserScreenActivity::class.java))
//                            }
//                            .create().show()
//                    }
//
//                }
//
//                getDescriptionView1.setOnClickListener {
//                    Constants.hideKeyboar(requireActivity())
//                    userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//
//                    if (userCurrentCredits.toFloat() >= 1.0) {
//                        launchActivity.launch(
//                            Intent(
//                                requireActivity(),
//                                RainForestApiActivity::class.java
//                            )
//                        )
//                    } else {
//                        MaterialAlertDialogBuilder(requireActivity())
//                            .setMessage(getString(R.string.low_credites_error_message2))
//                            .setCancelable(false)
//                            .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                                dialog.dismiss()
//                            }
//                            .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                                dialog.dismiss()
//                                startActivity(
//                                    Intent(
//                                        requireActivity(),
//                                        UserScreenActivity::class.java
//                                    )
//                                )
//                            }
//                            .create().show()
//                    }
//
//                }
//
//                dialogUpdateBtn.setOnClickListener {
//                    val titleText = titleBox.text.toString().trim()
//                    val shortDesc = productShortDescriptionBox.text.toString().trim()
//                    val fullDesc = fullDescriptionBox.text.toString().trim()
////
//                    if (defaultLayout == 0) {
//                        var stringBuilder = StringBuilder()
//
//                        for (i in 0 until (dynamicTitleTextViewWrapper as ViewGroup).childCount) {
//                            val nextChild = (dynamicTitleTextViewWrapper as ViewGroup).getChildAt(i)
//                            val text = (nextChild as MaterialTextView).text.toString()
//                            stringBuilder.append(text)
//                            stringBuilder.append(" ")
//                        }
//
////                        for (i in 0 until titleTextViewList.size) {
////                            val item = titleTextViewList[i]
////                            stringBuilder.append(item.text.toString())
////                            stringBuilder.append(" ")
////                        }
//
//                        pItem.title = stringBuilder.toString().trim()
//                        stringBuilder = StringBuilder()
////                        for (i in 0 until shortDescTextViewList.size) {
////                            val item = shortDescTextViewList[i]
////                            stringBuilder.append(item.text.toString())
////                            stringBuilder.append(" ")
////                        }
//
//                        for (i in 0 until (dynamicShortDescTextViewWrapper as ViewGroup).childCount) {
//                            val nextChild =
//                                (dynamicShortDescTextViewWrapper as ViewGroup).getChildAt(i)
//                            val text = (nextChild as MaterialTextView).text.toString()
//                            stringBuilder.append(text)
//                            stringBuilder.append(" ")
//                        }
//
//                        pItem.shortDesc = stringBuilder.toString().trim()
//
//                        stringBuilder = StringBuilder()
////                        for (i in 0 until fullDescTextViewList.size) {
////                            val item = fullDescTextViewList[i]
////                            stringBuilder.append(item.text.toString())
////                            stringBuilder.append(" ")
////                        }
//                        for (i in 0 until (dynamicFullDescTextViewWrapper as ViewGroup).childCount) {
//                            val nextChild =
//                                (dynamicFullDescTextViewWrapper as ViewGroup).getChildAt(i)
//                            val text = (nextChild as MaterialTextView).text.toString()
//                            stringBuilder.append(text)
//                            stringBuilder.append(" ")
//                        }
//                        pItem.fullDesc = stringBuilder.toString().trim()
//                    } else {
//                        BaseActivity.hideSoftKeyboard(requireActivity(), dialogUpdateBtn)
//                        pItem.title = titleText
//                        pItem.shortDesc = shortDesc
//                        pItem.fullDesc = fullDesc
//                    }
//                    defaultLayout = 0
//                    if (titleText.isNotEmpty()) {
//                        alert.dismiss()
//                        BaseActivity.startLoading(
//                            requireActivity(),
//                            getString(R.string.please_wait_product_update_message)
//                        )
//
//                        Paper.book().delete(Constants.cacheProducts)
//                        Paper.book().write(Constants.cacheProducts, originalProductsList)
//                        adapter.notifyItemChanged(position)
//
//                        viewModel.callUpdateProductDetail(
//                            requireActivity(),
//                            shopName,
//                            email,
//                            password,
//                            pItem.id,
//                            pItem.title,
//                            pItem.shortDesc,
//                            pItem.fullDesc
//                        )
//                        viewModel.getUpdateProductDetailResponse()
//                            .observe(requireActivity(), Observer { response ->
//                                if (response != null) {
//                                    if (response.get("status").asString == "200") {
////                                        Handler(Looper.myLooper()!!).postDelayed({
//                                        BaseActivity.dismiss()
//                                        fetchProducts()
////                                            showProducts()
////                                        }, 3000)
//                                        Toast.makeText(
//                                            requireActivity(),
//                                            getString(R.string.product_updated_successfully),
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    } else {
//                                        BaseActivity.dismiss()
//                                        BaseActivity.showAlert(
//                                            requireActivity(),
//                                            response.get("message").asString
//                                        )
//                                    }
//                                } else {
//                                    BaseActivity.dismiss()
//                                }
//                            })
//                    } else {
//                        BaseActivity.showAlert(
//                            requireActivity(),
//                            getString(R.string.empty_text_error)
//                        )
//                    }
//                }
            }

            override fun onItemGrammarCheckClick(
                position: Int,
                grammarCheckBtn: AppCompatImageView,
                title: ExpandableTextView,
                description: ExpandableTextView,
                grammarStatusView: MaterialTextView
            ) {
                val item = productsList[position]
                BaseActivity.startLoading(requireActivity())
                GrammarCheck.check(
                    requireActivity(),
                    item.title,
                    title,
                    1,
                    grammarStatusView,
                    object : GrammarCallback {
                        override fun onSuccess(response: SpannableStringBuilder?, errors: Boolean) {
                            GrammarCheck.check(
                                requireActivity(),
                                item.fullDesc,
                                description,
                                0,
                                grammarStatusView,
                                object : GrammarCallback {
                                    override fun onSuccess(
                                        response: SpannableStringBuilder?,
                                        errors: Boolean
                                    ) {
                                        BaseActivity.dismiss()
                                        if (errors) {
                                            grammarStatusView.setTextColor(Color.RED)
                                            grammarStatusView.setText("Errors Found")
                                            grammarCheckBtn.setImageResource(R.drawable.red_cross)
                                        } else {
                                            grammarStatusView.setTextColor(Color.GREEN)
                                            grammarStatusView.setText("No Errors")
                                            grammarCheckBtn.setImageResource(R.drawable.green_check_48)
                                        }
                                    }

                                })
                        }

                    })

            }

        })

//        var pastVisiblesItems: Int
//        var visibleItemCount: Int
//        var totalItemCount: Int

//        productsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if (dy > 0) { //check for scroll down
//                    visibleItemCount = linearLayoutManager.childCount
//                    totalItemCount = linearLayoutManager.itemCount
//                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition()
//
//                    if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
////                             if (currentTotalProducts == 250){
////                                 currentPage +=1
////                                 fetchProducts(currentPage)
////                             }
////                            else{
////                                Toast.makeText(context,getString(R.string.fetch_all_products),Toast.LENGTH_SHORT).show()
////                            }
//                    }
//
//                }
//            }
//        })
        val cacheList: ArrayList<Product>? = Paper.book().read(Constants.cacheProducts)

        if (cacheList != null && cacheList.size > 0) {
            originalProductsList.clear()
            productsList.clear()
            originalProductsList.addAll(cacheList)
            productsList.addAll(originalProductsList)
            adapter.notifyItemRangeChanged(0, productsList.size)
            Handler(Looper.myLooper()!!).postDelayed({
                if (menu != null) {
                    menu!!.findItem(R.id.insales_logout).isVisible = true
                    menu!!.findItem(R.id.insales_data_filter).isVisible = true
                    menu!!.findItem(R.id.insales_data_sync).isVisible = true
                }
            }, 1500)
        } else {
            dialogStatus = 1
            fetchProducts(currentPage)
        }

    }

    private fun getMaterialTextView(text: String):MaterialTextView{
        val params = FlowLayout.LayoutParams(
            FlowLayout.LayoutParams.WRAP_CONTENT,
            FlowLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 5, 5, 5)
        val textView = MaterialTextView(requireActivity())
        textView.layoutParams = params
        textView.text = text
        textView.tag = "title"
        textView.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        textView.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primary_positive_color
            )
        )
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        //titleTextViewList.add(textView)
        //textView.setOnClickListener(requireActivity())
        textView.setOnTouchListener(ChoiceTouchListener())
        textView.setOnDragListener(ChoiceDragListener())
        return textView
    }

    private fun fetchProducts() {
        currentPage = 1
        dialogStatus = 1
        fetchProducts(currentPage)
    }

    var launchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (defaultLayout == 1) {
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
                            val stringBuilder = StringBuilder()
                            stringBuilder.append(fullDescriptionBox.text.toString())
                            stringBuilder.append(description)
                            fullDescriptionBox.setText(stringBuilder.toString())
                            fullDescriptionBox.setSelection(fullDescriptionBox.length())
                            fullDescriptionBox.requestFocus()
                        }
                    }
                } else {

                    if (data != null && data.hasExtra("TITLE")) {
                        val title = data.getStringExtra("TITLE") as String
                        if (title.isNotEmpty()) {
                            titleTextViewList.clear()
                            dynamicTitleTextViewWrapper.removeAllViews()
                            val titleTextList = title.trim().split(" ")

                            for (i in 0 until titleTextList.size) {
                                val params = FlowLayout.LayoutParams(
                                    FlowLayout.LayoutParams.WRAP_CONTENT,
                                    FlowLayout.LayoutParams.WRAP_CONTENT
                                )
                                params.setMargins(5, 5, 5, 5)
                                val textView = MaterialTextView(requireActivity())
                                textView.layoutParams = params
                                textView.text = titleTextList[i].trim()
                                textView.tag = "title"
                                textView.id = i
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.black
                                    )
                                )
                                titleTextViewList.add(textView)
                                textView.setOnClickListener(this@InsalesFragment)
//                                textView.setOnTouchListener(ChoiceTouchListener())
//                                textView.setOnDragListener(ChoiceDragListener())
                                dynamicTitleTextViewWrapper.addView(textView)
                            }

                            //titleBox.setText(title)
                            //titleBox.setSelection(titleBox.length())
                            //titleBox.requestFocus()
                        }
                    }

                    if (data != null && data.hasExtra("DESCRIPTION")) {
                        val description = data.getStringExtra("DESCRIPTION") as String
                        if (description.isNotEmpty()) {
//                            fullDescTextViewList.clear()
//                            dynamicFullDescTextViewWrapper.removeAllViews()
                            val fullDescTextList = description.trim().split(" ")

                            for (i in 0 until fullDescTextList.size) {
                                val params = FlowLayout.LayoutParams(
                                    FlowLayout.LayoutParams.WRAP_CONTENT,
                                    FlowLayout.LayoutParams.WRAP_CONTENT
                                )
                                params.setMargins(5, 5, 5, 5)
                                val textView = MaterialTextView(requireActivity())
                                textView.layoutParams = params
                                textView.text = fullDescTextList[i].trim()
                                textView.tag = "title"
                                textView.id = i
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.black
                                    )
                                )
                                fullDescTextViewList.add(textView)
                                textView.setOnClickListener(this@InsalesFragment)
//                                textView.setOnTouchListener(ChoiceTouchListener())
//                                textView.setOnDragListener(ChoiceDragListener())
                                dynamicFullDescTextViewWrapper.addView(textView)
                            }

                            //fullDescriptionBox.setText(description)
//                        fullDescriptionBox.setSelection(fullDescriptionBox.length())
//                        fullDescriptionBox.requestFocus()
                        }
                    }
                }
            }
        }

    private fun fetchProducts(page: Int) {
        if (dialogStatus == 1) {
            BaseActivity.startLoading(
                requireActivity(),
                getString(R.string.please_wait_products_message)
            )
        }
        viewModel.callProducts(requireActivity(), shopName, email, password, page)
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
                        BaseActivity.dismiss()
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
                        BaseActivity.dismiss()
                    }
                } else {
                    BaseActivity.dismiss()
                    BaseActivity.showAlert(requireActivity(), response.get("message").asString)
                }
            } else {
                BaseActivity.dismiss()
            }
        })
    }

    private fun inSalesLogin(shopName: String, email: String, password: String) {

        BaseActivity.startLoading(requireActivity(), getString(R.string.please_wait_login_message))
        viewModel.callSalesAccount(requireActivity(), shopName, email, password)
        viewModel.getSalesAccountResponse().observe(this, Observer { response ->
            BaseActivity.dismiss()
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
                    fabAddProduct.visibility = View.VISIBLE
//                    menu!!.findItem(R.id.insales_logout).isVisible = true
//                    menu!!.findItem(R.id.insales_data_filter).isVisible = true
//                    menu!!.findItem(R.id.insales_data_sync).isVisible = true
                    showProducts()
                } else {
                    BaseActivity.showAlert(requireActivity(), response.get("message").asString)
                }
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
                    currentPhotoPath = ImageManager.getRealPathFromUri(
                        requireActivity(),
                        imageUri.data
                    )
                    selectedImageBase64String =
                        ImageManager.convertImageToBase64(requireActivity(), currentPhotoPath!!)
                    Log.d("TEST199", selectedImageBase64String)
                    Glide.with(requireActivity())
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
                    ImageManager.convertImageToBase64(requireActivity(), currentPhotoPath!!)
                Log.d("TEST199", selectedImageBase64String)
                Glide.with(requireActivity())
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)
            }
        }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
    }

    override fun onClick(v: View?) {
        val view = v!!
        when (view.id) {
            R.id.insales_login_btn -> {
                if (validation()) {
                    val shopName = insalesShopNameBox.text.toString().trim()
                    val email = insalesEmailBox.text.toString().trim()
                    val password = insalesPasswordBox.text.toString().trim()
                    inSalesLogin(shopName, email, password)
                }
            }
            R.id.insales_products_search_reset_btn -> {
                BaseActivity.hideSoftKeyboard(requireActivity(), searchBox)
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
                    Constants.hideKeyboar(requireActivity())
                    search(query)
                } else {
                    BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
                }
            }
            R.id.fab -> {
                addProduct()
            }
            else -> {
//                val position = view.id
//                val textView = view as MaterialTextView
//                view.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireActivity(),
//                        R.color.primary_positive_color
//                    )
//                )
//                view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
//                val balloon = Balloon.Builder(requireActivity())
//                    .setLayout(R.layout.ballon_layout_design)
//                    .setArrowSize(10)
//                    .setArrowOrientation(ArrowOrientation.TOP)
//                    .setArrowPosition(0.5f)
//                    .setWidthRatio(0.55f)
//                    .setCornerRadius(4f)
//                    .setBackgroundColor(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.light_gray
//                        )
//                    )
//                    .setBalloonAnimation(BalloonAnimation.ELASTIC)
//                    .setLifecycleOwner(this)
//                    .build()
//                val editTextBox = balloon.getContentView().findViewById<TextInputEditText>(R.id.balloon_edit_text)
//                editTextBox.setText(textView.text.toString().trim())
//                val closeBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_close_btn)
//                val applyBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_apply_btn)
//                balloon.showAlignTop(textView)
//                editTextBox.requestFocus()
//                Constants.openKeyboar(requireActivity())
//                closeBtn.setOnClickListener {
//                    Constants.hideKeyboar(requireActivity())
//                    balloon.dismiss()
//                    view.setBackgroundColor(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.white
//                        )
//                    )
//                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                }
//                applyBtn.setOnClickListener {
//                    Constants.hideKeyboar(requireActivity())
//                    balloon.dismiss()
//                    //val tempText = textView.replace(mWord,editTextBox.text.toString().trim())
//                    textView.text = editTextBox.text.toString().trim()
//                    view.setBackgroundColor(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.white
//                        )
//                    )
//                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//
//                }
            }
        }
    }


    private fun addProduct(){
        val addProductLayout = LayoutInflater.from(requireActivity()).inflate(
            R.layout.insales_add_product_dialog,
            null
        )
        val categoriesSpinner = addProductLayout.findViewById<AppCompatSpinner>(R.id.ap_cate_spinner)
        val apTitleView = addProductLayout.findViewById<TextInputEditText>(R.id.ap_title)
        val apDescriptionView = addProductLayout.findViewById<TextInputEditText>(R.id.ap_description)
        val apQuantityView = addProductLayout.findViewById<TextInputEditText>(R.id.ap_quantity)
        val apPriceView = addProductLayout.findViewById<TextInputEditText>(R.id.ap_price)
        val apSubmitBtn = addProductLayout.findViewById<MaterialButton>(R.id.ap_dialog_submit_btn)
        val apCancelBtn = addProductLayout.findViewById<MaterialButton>(R.id.ap_dialog_cancel_btn)


        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(addProductLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        val cateSpinnerAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            originalCategoriesList
        )
        cateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = cateSpinnerAdapter

        if (selectedCategoryId != 0){
            categoriesSpinner!!.setSelection(0)
        }

        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                    val selectedItem = originalCategoriesList[position]
                    selectedCategoryId = selectedItem.id

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        apCancelBtn.setOnClickListener {
            alert.dismiss()
        }

        apSubmitBtn.setOnClickListener {

            if (addProductValidation(categoriesSpinner, apTitleView, apQuantityView, apPriceView)){
                alert.dismiss()
                BaseActivity.startLoading(requireActivity())
               viewModel.callAddProduct(
                   requireActivity(),
                   shopName,
                   email,
                   password,
                   selectedCategoryId,
                   apTitleView.text.toString().trim(),
                   apDescriptionView.text.toString().trim(),
                   apQuantityView.text.toString().trim(),
                   apPriceView.text.toString().trim()
               )
                viewModel.getAddProductResponse()
                    .observe(requireActivity(), Observer { response ->
                        if (response != null) {
                            if (response.get("status").asString == "200") {
                                Handler(Looper.myLooper()!!).postDelayed({
                                    BaseActivity.dismiss()
                                    fetchProducts()//showProducts()
                                }, 3000)
                            } else {
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

    private fun addProductValidation(
        categoriesSpinner: AppCompatSpinner?,
        apTitleView: TextInputEditText?,
        apQuantityView: TextInputEditText?,
        apPriceView: TextInputEditText?
    ): Boolean {
        if (selectedCategoryId == 0){
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.add_product_cate_error)
            )
            return false
        }
        else if (apTitleView!!.text.toString().isEmpty()){
            apTitleView.error = requireActivity().resources.getString(R.string.empty_text_error)
            return false
        }
        else if (apQuantityView!!.text.toString().isEmpty()){
            apQuantityView.error = requireActivity().resources.getString(R.string.empty_text_error)
            return false
        }
        else if (apPriceView!!.text.toString().isEmpty()){
            apPriceView.error = requireActivity().resources.getString(R.string.empty_text_error)
            return false
        }
        return true
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
                    requireActivity(),
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

    private fun validation(): Boolean {
        if (insalesShopNameBox.text.toString().trim().isEmpty()) {
            BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
            return false
        } else if (insalesEmailBox.text.toString().trim().isEmpty()) {
            BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
            return false
        } else if (!Pattern.compile(Constants.emailPattern)
                .matcher(insalesEmailBox.text.toString().trim())
                .matches()
        ) {
            BaseActivity.showAlert(requireActivity(), getString(R.string.email_valid_error))
            return false
        } else if (insalesPasswordBox.text.toString().trim().isEmpty()) {
            BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
            return false
        }
        return true
    }

    internal class MyDragListener : View.OnDragListener {

        override fun onDrag(v: View, event: DragEvent): Boolean {
            val action = event.action
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                }
//                    v.setBackgroundDrawable(enterShape)
                DragEvent.ACTION_DRAG_EXITED -> {
                }
//                    v.setBackgroundDrawable(normalShape)
                DragEvent.ACTION_DROP -> {
                    // Dropped, reassign View to ViewGroup
                    val t: MaterialTextView
                    val view = event.localState as View
                    t = view.findViewById(R.id.keyword_item_name_view)

                    val temp = t.text.toString()
                    val tempTextView = MaterialTextView(view.context)
                    tempTextView.text = temp
//                    val r = temp
//                    textview.add(r)
//                    temp = all(textview)
//                    val container = v as MaterialTextView

//                    container.text = temp
                    dynamicTitleTextViewWrapper.addView(tempTextView)
                    dynamicTitleTextViewWrapper.invalidate()
                    //view.setVisibility(View.VISIBLE);
//                    wordList.remove(String(r))
//                    list.setAdapter(null)
//                    a = adapter(wordList, this@MainActivity)
//                    list.setAdapter(a)
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                } //v.setBackgroundDrawable(normalShape)
                else -> {
                }
            }
            return true
        }
    }

    private fun updateProductDetail(){

    }


    class CustomDialog(private val pItem:Product,private val position:Int,private val insalesAdapter:InSalesProductsAdapter,private val viewModel:SalesCustomersViewModel) : DialogFragment(), View.OnClickListener{

        private var insalesFragment:InsalesFragment?=null

        override fun onAttach(context: Context) {
            super.onAttach(context)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialogStyle
            )
            insalesFragment = InsalesFragment()
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v =  inflater.inflate(R.layout.insales_product_detail_update_dialog_layout, container)

            initViews(v)

            return v
        }

        private fun initViews(dialogLayout: View) {
            val dialogHeading = dialogLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
            val swapLayoutBtn = dialogLayout.findViewById<MaterialCheckBox>(R.id.layout_swap)
            val firstLinearLayout =
                dialogLayout.findViewById<LinearLayout>(R.id.first_linear_layout)
            val secondLinearLayout =
                dialogLayout.findViewById<LinearLayout>(R.id.second_linear_layout)
            dynamicTitleTextViewWrapper =
                dialogLayout.findViewById(R.id.dynamic_insales_title_textview_wrapper)
//                dynamicTitleTextViewWrapper.setOnDragListener(MyDragListener())
            val dynamicShortDescTextViewWrapper =
                dialogLayout.findViewById<FlowLayout>(R.id.dynamic_insales_short_description_textview_wrapper)
            dynamicFullDescTextViewWrapper =
                dialogLayout.findViewById(R.id.dynamic_insales_full_description_textview_wrapper)

            secondLinearLayout.visibility = View.VISIBLE
            insalesFragment!!.titleBox =
                dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_title_input_field)
            val productShortDescriptionBox =
                dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_short_desc_input_field)
            insalesFragment!!.fullDescriptionBox =
                dialogLayout.findViewById(R.id.insales_product_full_desc_input_field)
            val getDescriptionView =
                dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view)
            val getDescriptionView1 =
                dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view1)

            insalesFragment!!.titleBox.setText(pItem.title)
            productShortDescriptionBox.setText(pItem.shortDesc)
            insalesFragment!!.fullDescriptionBox.setText(pItem.fullDesc)
            val dialogCancelBtn =
                dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_cancel_btn)
            val dialogUpdateBtn =
                dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_update_btn)

            insalesFragment!!.titleTextViewList.clear()
            insalesFragment!!.shortDescTextViewList.clear()
            insalesFragment!!.fullDescTextViewList.clear()
            dynamicTitleTextViewWrapper.removeAllViews()
            dynamicShortDescTextViewWrapper.removeAllViews()
            dynamicFullDescTextViewWrapper.removeAllViews()

            val titleTextList = pItem.title.trim().split(" ")
            val shortDescTextList = pItem.shortDesc.trim().split(" ")
            val fullDescTextList = pItem.fullDesc.trim().split(" ")

            for (i in 0 until titleTextList.size) {
                val params = FlowLayout.LayoutParams(
                    FlowLayout.LayoutParams.WRAP_CONTENT,
                    FlowLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(5, 5, 5, 5)
                val textView = MaterialTextView(requireActivity())
                textView.layoutParams = params
                textView.text = titleTextList[i].trim()
                textView.tag = "title"
                textView.id = i
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                insalesFragment!!.titleTextViewList.add(textView)
                textView.setOnClickListener(this)
                dynamicTitleTextViewWrapper.addView(textView)
            }

            for (i in 0 until shortDescTextList.size) {
                val params = FlowLayout.LayoutParams(
                    FlowLayout.LayoutParams.WRAP_CONTENT,
                    FlowLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(5, 5, 5, 5)
                val textView = MaterialTextView(requireActivity())
                textView.layoutParams = params
                textView.text = shortDescTextList[i].trim()
                textView.tag = "title"
                textView.id = i
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                insalesFragment!!.shortDescTextViewList.add(textView)
                textView.setOnClickListener(this)
                dynamicShortDescTextViewWrapper.addView(textView)
            }
//
            for (i in 0 until fullDescTextList.size) {
                val params = FlowLayout.LayoutParams(
                    FlowLayout.LayoutParams.WRAP_CONTENT,
                    FlowLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(5, 5, 5, 5)
                val textView = MaterialTextView(requireActivity())
                textView.layoutParams = params
                textView.text = fullDescTextList[i].trim()
                textView.tag = "title"
                textView.id = i
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                insalesFragment!!.fullDescTextViewList.add(textView)
                textView.setOnClickListener(this)
                dynamicFullDescTextViewWrapper.addView(textView)
            }

            swapLayoutBtn.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    secondLinearLayout.visibility = View.GONE
                    firstLinearLayout.visibility = View.VISIBLE
                    insalesFragment!!.defaultLayout = 1

                    insalesFragment!!.titleBox.setSelection(pItem.title.length)
                    insalesFragment!!.titleBox.requestFocus()
                    Constants.openKeyboar(requireContext())
                } else {
                    Constants.hideKeyboar(requireContext())
                    BaseActivity.startLoading(requireContext())
                    firstLinearLayout.visibility = View.GONE
                    secondLinearLayout.visibility = View.VISIBLE
                    insalesFragment!!.defaultLayout = 0
                    BaseActivity.dismiss()
                }
            }

            insalesFragment!!.titleBox.setSelection(pItem.title.length)
//                titleBox.requestFocus()
//                Constants.openKeyboar(context)
            dialogCancelBtn.setOnClickListener {
                BaseActivity.hideSoftKeyboard(requireContext(), dialogCancelBtn)
//                    Constants.hideKeyboar(requireActivity())
                dismiss()
            }

//            getDescriptionView.setOnClickListener {
//                Constants.hideKeyboar(requireActivity())
//                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//
//                if (userCurrentCredits.toFloat() >= 1.0) {
//                    launchActivity.launch(
//                        Intent(
//                            requireActivity(),
//                            RainForestApiActivity::class.java
//                        )
//                    )
//                } else {
//                    MaterialAlertDialogBuilder(requireActivity())
//                        .setMessage(getString(R.string.low_credites_error_message2))
//                        .setCancelable(false)
//                        .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                            dialog.dismiss()
//                        }
//                        .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                            dialog.dismiss()
//                            startActivity(Intent(context, UserScreenActivity::class.java))
//                        }
//                        .create().show()
//                }
//
//            }
//
//            getDescriptionView1.setOnClickListener {
//                Constants.hideKeyboar(requireActivity())
//                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//
//                if (userCurrentCredits.toFloat() >= 1.0) {
//                    launchActivity.launch(
//                        Intent(
//                            requireActivity(),
//                            RainForestApiActivity::class.java
//                        )
//                    )
//                } else {
//                    MaterialAlertDialogBuilder(requireActivity())
//                        .setMessage(getString(R.string.low_credites_error_message2))
//                        .setCancelable(false)
//                        .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                            dialog.dismiss()
//                        }
//                        .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                            dialog.dismiss()
//                            startActivity(
//                                Intent(
//                                    requireActivity(),
//                                    UserScreenActivity::class.java
//                                )
//                            )
//                        }
//                        .create().show()
//                }
//
//            }

            dialogUpdateBtn.setOnClickListener {
                val titleText = insalesFragment!!.titleBox.text.toString().trim()
                val shortDesc = productShortDescriptionBox.text.toString().trim()
                val fullDesc = insalesFragment!!.fullDescriptionBox.text.toString().trim()
//
                if (insalesFragment!!.defaultLayout == 0) {
                    var stringBuilder = StringBuilder()

                    for (i in 0 until (dynamicTitleTextViewWrapper as ViewGroup).childCount) {
                        val nextChild = (dynamicTitleTextViewWrapper as ViewGroup).getChildAt(i)
                        val text = (nextChild as MaterialTextView).text.toString()
                        stringBuilder.append(text)
                        stringBuilder.append(" ")
                    }

//                        for (i in 0 until titleTextViewList.size) {
//                            val item = titleTextViewList[i]
//                            stringBuilder.append(item.text.toString())
//                            stringBuilder.append(" ")
//                        }

                    pItem.title = stringBuilder.toString().trim()
                    stringBuilder = StringBuilder()
//                        for (i in 0 until shortDescTextViewList.size) {
//                            val item = shortDescTextViewList[i]
//                            stringBuilder.append(item.text.toString())
//                            stringBuilder.append(" ")
//                        }

                    for (i in 0 until (dynamicShortDescTextViewWrapper as ViewGroup).childCount) {
                        val nextChild =
                            (dynamicShortDescTextViewWrapper as ViewGroup).getChildAt(i)
                        val text = (nextChild as MaterialTextView).text.toString()
                        stringBuilder.append(text)
                        stringBuilder.append(" ")
                    }

                    pItem.shortDesc = stringBuilder.toString().trim()

                    stringBuilder = StringBuilder()
//                        for (i in 0 until fullDescTextViewList.size) {
//                            val item = fullDescTextViewList[i]
//                            stringBuilder.append(item.text.toString())
//                            stringBuilder.append(" ")
//                        }
                    for (i in 0 until (dynamicFullDescTextViewWrapper as ViewGroup).childCount) {
                        val nextChild =
                            (dynamicFullDescTextViewWrapper as ViewGroup).getChildAt(i)
                        val text = (nextChild as MaterialTextView).text.toString()
                        stringBuilder.append(text)
                        stringBuilder.append(" ")
                    }
                    pItem.fullDesc = stringBuilder.toString().trim()
                } else {
                    BaseActivity.hideSoftKeyboard(requireContext(), dialogUpdateBtn)
                    pItem.title = titleText
                    pItem.shortDesc = shortDesc
                    pItem.fullDesc = fullDesc
                }
                insalesFragment!!.defaultLayout = 0
                if (titleText.isNotEmpty()) {
                    BaseActivity.startLoading(
                        requireActivity(),
                        getString(R.string.please_wait_product_update_message)
                    )

                    Paper.book().delete(Constants.cacheProducts)
                    Paper.book().write(Constants.cacheProducts, insalesFragment!!.originalProductsList)
                    insalesAdapter.notifyItemChanged(position)

                    viewModel.callUpdateProductDetail(
                        requireContext(),
                        insalesFragment!!.shopName,
                        insalesFragment!!.email,
                        insalesFragment!!.password,
                        pItem.id,
                        pItem.title,
                        pItem.shortDesc,
                        pItem.fullDesc
                    )
                    viewModel.getUpdateProductDetailResponse()
                        .observe(requireActivity(), Observer { response ->
                            if (response != null) {
                                if (response.get("status").asString == "200") {
//                                        Handler(Looper.myLooper()!!).postDelayed({
                                    BaseActivity.dismiss()
                                    dismiss()
                                    insalesFragment!!.fetchProducts()
//                                            showProducts()
//                                        }, 3000)
//                                    Toast.makeText(
//                                        requireContext(),
//                                        getString(R.string.product_updated_successfully),
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                } else {
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
                } else {
                    BaseActivity.showAlert(
                        requireActivity(),
                        getString(R.string.empty_text_error)
                    )
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            return dialog
        }

        override fun onClick(v: View?) {
            val view = v!!
            when (view.id) {
                else -> {
                    val position = view.id
                    val textView = view as MaterialTextView
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.primary_positive_color
                        )
                    )
                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                    val balloon = Balloon.Builder(requireActivity())
                        .setLayout(R.layout.ballon_layout_design)
                        .setArrowSize(10)
                        .setArrowOrientation(ArrowOrientation.TOP)
                        .setArrowPosition(0.5f)
                        .setWidthRatio(0.55f)
                        .setCornerRadius(4f)
                        .setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.light_gray
                            )
                        )
                        .setBalloonAnimation(BalloonAnimation.ELASTIC)
                        .setLifecycleOwner(this)
                        .build()
                    val editTextBox = balloon.getContentView().findViewById<TextInputEditText>(R.id.balloon_edit_text)
                    editTextBox.setText(textView.text.toString().trim())
                    val closeBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_close_btn)
                    val applyBtn = balloon.getContentView().findViewById<AppCompatButton>(R.id.balloon_apply_btn)
                    balloon.showAlignTop(textView)
                    editTextBox.requestFocus()
                    Constants.openKeyboar(requireActivity())
                    closeBtn.setOnClickListener {
                        Constants.hideKeyboar(requireActivity())
                        balloon.dismiss()
                        view.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.white
                            )
                        )
                        view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    }
                    applyBtn.setOnClickListener {
                        Constants.hideKeyboar(requireActivity())
                        balloon.dismiss()
                        //val tempText = textView.replace(mWord,editTextBox.text.toString().trim())
                        textView.text = editTextBox.text.toString().trim()
                        view.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.white
                            )
                        )
                        view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))

                    }
                }
            }
        }
    }

}