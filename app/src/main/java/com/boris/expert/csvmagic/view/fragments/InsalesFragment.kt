package com.boris.expert.csvmagic.view.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.*
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.interfaces.GrammarCallback
import com.boris.expert.csvmagic.interfaces.ResponseListener
import com.boris.expert.csvmagic.model.*
import com.boris.expert.csvmagic.utils.*
import com.boris.expert.csvmagic.view.activities.*
import com.boris.expert.csvmagic.viewmodel.AddProductViewModel
import com.boris.expert.csvmagic.viewmodel.SalesCustomersViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.paperdb.Paper
import net.expandable.ExpandableTextView
import org.apmem.tools.layouts.FlowLayout
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class InsalesFragment : Fragment(), View.OnClickListener {

    private lateinit var internetSearchLayout: View
    private lateinit var internetImageRecyclerView: RecyclerView
    private lateinit var internetImageDoneBtn: MaterialButton
    private var barcodeSearchHint = "default"
    private lateinit var appSettings: AppSettings
    private lateinit var viewModel: SalesCustomersViewModel
    private lateinit var insalesLoginWrapperLayout: CardView
    private lateinit var insalesDataWrapperLayout: LinearLayout
    private lateinit var insalesSearchWrapperLayout: CardView
    private lateinit var insalesShopNameBox: TextInputEditText
    private lateinit var insalesEmailBox: TextInputEditText
    private lateinit var insalesPasswordBox: TextInputEditText
    private lateinit var insalesLoginBtn: MaterialButton
    private var productsList = mutableListOf<Product>()
    private var originalProductsList = mutableListOf<Product>()
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var productAdapter: InSalesProductsAdapter1
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
    private lateinit var fabAddProduct: FloatingActionButton
    private var categoryList = mutableListOf<Category>()
    private var selectedCategoryId: Int = 0
    private var apTitleView: TextInputEditText? = null
    private var apDescriptionView: TextInputEditText? = null
    private lateinit var internetImageAdapter: InternetImageAdapter
    private lateinit var searchBtnView: ImageButton
    private lateinit var searchBoxView: TextInputEditText
    private lateinit var loader: ProgressBar
    private lateinit var voiceSearchIcon: AppCompatImageView
    private var voiceLanguageCode = "en"
    var searchedImagesList = mutableListOf<String>()
    private lateinit var voiceSearchView: AppCompatImageView
    private lateinit var barcodeSearchFragmentInsales: AppCompatImageView
    private var voiceSearchHint = "default"
    private lateinit var imagesRecyclerView: RecyclerView
    private var barcodeImageList = mutableListOf<String>()
    var multiImagesList = mutableListOf<String>()
    private lateinit var adapter: BarcodeImageAdapter


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
            if (originalCategoriesList.size == 0) {
                viewModel.callCategories(requireActivity(), shopName, email, password)
                viewModel.getCategoriesResponse().observe(this, Observer { response ->
                    if (response != null) {
                        if (response.get("status").asString == "200") {
                            val categories = response.get("categories").asJsonArray
                            if (categories.size() > 0) {
                                if (categoriesList.isNotEmpty()) {
                                    categoriesList.clear()
                                }
                                for (i in 0 until categories.size()) {
                                    val category = categories[i].asJsonObject
                                    originalCategoriesList.add(
                                        Category(
                                            category.get("title").asString,
                                            category.get("id").asInt
                                        )
                                    )
                                }
                            }
                        }
                    }
                })
            } else {
                if (categoriesList.isNotEmpty()) {
                    categoriesList.clear()
                }
                categoriesList.addAll(originalCategoriesList)
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


    private fun resetProductList() {
        productsList.clear()
        productsList.addAll(originalProductsList)
        productAdapter.submitList(productsList)
        //productAdapter.notifyItemRangeChanged(0, productsList.size)
        productsRecyclerView.smoothScrollToPosition(0)
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
            productAdapter.notifyDataSetChanged()

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
            productAdapter.notifyDataSetChanged()

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
                //productAdapter.notifyItemRangeChanged(0, productsList.size)
                productAdapter.submitList(productsList)
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
        voiceSearchView = view.findViewById(R.id.voice_search_fragment_insales)
        barcodeSearchFragmentInsales = view.findViewById(R.id.barcode_img_fragment_insales)

        voiceSearchView.setOnClickListener {
            voiceSearchHint = "voice_mode"
            voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
            val voiceLayout = LayoutInflater.from(context).inflate(
                R.layout.voice_language_setting_layout,
                null
            )
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
                            if (parent!!.selectedItem.toString().toLowerCase(
                                    Locale.ENGLISH
                                ).contains("english")
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
                Constants.listUpdateFlag = 1
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

        barcodeSearchFragmentInsales.setOnClickListener {
            barcodeSearchHint = "default"
            Constants.listUpdateFlag = 1
            val intent = Intent(requireActivity(), BarcodeReaderActivity::class.java)
            barcodeImageResultLauncher.launch(intent)
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    resetProductList()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        searchBox.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                val query = searchBox.text.toString()
                if (query.isNotEmpty()) {
                    BaseActivity.hideSoftKeyboard(requireActivity(), searchBox)
                    requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                    search(query, "default")
                }

                return false
            }

        })
    }


    override fun onResume() {
        super.onResume()
        internetSearchLayout = LayoutInflater.from(context)
            .inflate(R.layout.internet_image_search_dialog_layout, null)
        checkInsalesAccount()
    }

    private fun checkInsalesAccount() {
        val insalesStatus = appSettings.getString("INSALES_STATUS")

        if (insalesStatus!!.isNotEmpty() && insalesStatus == "logged") {

            LocalBroadcastManager.getInstance(
                requireActivity()
            ).registerReceiver(broadcastReceiver, IntentFilter("update-products"))

            insalesLoginWrapperLayout.visibility = View.GONE
            insalesSearchWrapperLayout.visibility = View.VISIBLE
            insalesDataWrapperLayout.visibility = View.VISIBLE
            fabAddProduct.visibility = View.VISIBLE


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
            fabAddProduct.visibility = View.GONE
            insalesLoginWrapperLayout.visibility = View.VISIBLE

            if (menu != null) {
                menu!!.findItem(R.id.insales_logout).isVisible = false
                menu!!.findItem(R.id.insales_data_filter).isVisible = false
                menu!!.findItem(R.id.insales_data_sync).isVisible = false
            }
        }
    }

    private var characters = 0
    private var grammarPrice = 0F
    private var unitCharacterPrice = 0F
    private var howMuchChargeCredits = 0F
    private fun showProducts() {

        linearLayoutManager = WrapContentLinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
        productsRecyclerView.layoutManager = linearLayoutManager
        productsRecyclerView.hasFixedSize()
        productAdapter = InSalesProductsAdapter1(
            requireActivity()
        )
        productsRecyclerView.isNestedScrollingEnabled = false
        productsRecyclerView.adapter = productAdapter
        productAdapter.setOnItemClickListener(object : InSalesProductsAdapter1.OnItemClickListener {
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
                    val tempImageList = mutableListOf<String>()
                    searchedImagesList.clear()
                    loader =
                        internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
                    searchBoxView =
                        internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
                    searchBtnView =
                        internetSearchLayout.findViewById<ImageButton>(R.id.internet_image_search_btn)
                    internetImageRecyclerView =
                        internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
                    val closeBtn =
                        internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
                    voiceSearchIcon = internetSearchLayout.findViewById(
                        R.id
                            .voice_search_internet_images
                    )
                    val barcodeImage = internetSearchLayout.findViewById<AppCompatImageView>(
                        R.id
                            .barcode_img_search_internet_images
                    )
                    internetImageDoneBtn =
                        internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setCancelable(false)
                    builder.setView(internetSearchLayout)
                    val iAlert = builder.create()
                    iAlert.show()

                    internetImageDoneBtn.setOnClickListener {
                        iAlert.dismiss()
                    }

                    barcodeImage.setOnClickListener {
                        barcodeSearchHint = "image"
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
                        searchedImagesList as java.util.ArrayList<String>
                    )
                    internetImageRecyclerView.adapter = internetImageAdapter
                    internetImageAdapter.setOnItemClickListener(object :
                        InternetImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedImage = searchedImagesList[position]
                            FullImageFragment(selectedImage).show(
                                childFragmentManager,
                                "full-image-dialog"
                            )
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
                    voiceSearchIcon.setOnClickListener {
                        voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                        val voiceLayout = LayoutInflater.from(context).inflate(
                            R.layout.voice_language_setting_layout,
                            null
                        )
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
                                        if (parent!!.selectedItem.toString().toLowerCase(
                                                Locale.ENGLISH
                                            ).contains("english")
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
//                        var creditChargePrice: Float = 0F
//                        if (searchBoxView.text.toString().trim().isNotEmpty()) {
//
//
//                            val firebaseDatabase = FirebaseDatabase.getInstance().reference
//                            firebaseDatabase.child("SearchImagesLimit")
//                                .addListenerForSingleValueEvent(object :
//                                    ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        val creditPrice = snapshot.child("credits")
//                                            .getValue(Int::class.java) as Int
//                                        val images = snapshot.child("images")
//                                            .getValue(Int::class.java) as Int
//                                        creditChargePrice = creditPrice.toFloat() / images
//
//                                        userCurrentCredits =
//                                            appSettings.getString(Constants.userCreditsValue) as String
//
//                                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
//                                            BaseActivity.hideSoftKeyboard(
//                                                requireActivity(),
//                                                searchBtnView
//                                            )
//                                            //Constants.hideKeyboar(requireActivity())
//                                            val query = searchBoxView.text.toString().trim()
//                                            requireActivity().runOnUiThread {
//                                                loader.visibility = View.VISIBLE
//                                            }
//
//                                            BaseActivity.searchInternetImages(
//                                                requireActivity(),
//                                                query,
//                                                object : APICallback {
//                                                    override fun onSuccess(response: JSONObject) {
//                                                        if (loader.visibility == View.VISIBLE) {
//                                                            loader.visibility =
//                                                                View.INVISIBLE
//                                                        }
//
//                                                        val items =
//                                                            response.getJSONArray("items")
//                                                        if (items.length() > 0) {
//                                                            searchedImagesList.clear()
//                                                            for (i in 0 until items.length()) {
//                                                                val item =
//                                                                    items.getJSONObject(
//                                                                        i
//                                                                    )
//                                                                if (item.has("link")) {
//                                                                    searchedImagesList.add(
//                                                                        item.getString(
//                                                                            "link"
//                                                                        )
//                                                                    )
//                                                                }
//                                                            }
//                                                            internetImageAdapter.notifyItemRangeChanged(
//                                                                0,
//                                                                searchedImagesList.size
//                                                            )
//
//                                                        }
//                                                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                                                        val hashMap = HashMap<String, Any>()
//                                                        val remaining =
//                                                            userCurrentCredits.toFloat() - creditChargePrice
//                                                        Log.d("TEST199", "$remaining")
//                                                        hashMap["credits"] =
//                                                            remaining.toString()
//                                                        firebaseDatabase.child(Constants.firebaseUserCredits)
//                                                            .child(Constants.firebaseUserId)
//                                                            .updateChildren(hashMap)
//                                                            .addOnSuccessListener {
//                                                                BaseActivity.getUserCredits(
//                                                                    requireActivity()
//                                                                )
//                                                            }
//                                                            .addOnFailureListener {
//
//                                                            }
//                                                    }
//
//                                                    override fun onError(error: VolleyError) {
//                                                        if (loader.visibility == View.VISIBLE) {
//                                                            loader.visibility =
//                                                                View.INVISIBLE
//                                                        }
//
//                                                        BaseActivity.showAlert(
//                                                            requireActivity(),
//                                                            error.localizedMessage!!
//                                                        )
//                                                    }
//
//                                                })
//                                        } else {
//                                            MaterialAlertDialogBuilder(requireActivity())
//                                                .setMessage(getString(R.string.low_credites_error_message))
//                                                .setCancelable(false)
//                                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                                                    dialog.dismiss()
//                                                }
//                                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                                                    dialog.dismiss()
//                                                    startActivity(
//                                                        Intent(
//                                                            requireActivity(),
//                                                            UserScreenActivity::class.java
//                                                        )
//                                                    )
//                                                }
//                                                .create().show()
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//
//                                    }
//
//                                })
//
//
//                        } else {
//                            if (loader.visibility == View.VISIBLE) {
//                                loader.visibility = View.INVISIBLE
//                            }
//
//                            BaseActivity.showAlert(
//                                requireActivity(),
//                                getString(R.string.empty_text_error)
//                            )
//                        }
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList as java.util.ArrayList<String>,
                            internetImageAdapter
                        )
                    }
                    searchBoxView.setOnEditorActionListener(object :
                        TextView.OnEditorActionListener {
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
                                    searchedImagesList as java.util.ArrayList<String>,
                                    internetImageAdapter
                                )
                            }
                            return false
                        }

                    })
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
                multiImagesList.clear()
                barcodeImageList.clear()
                val insalesUpdateProductImageLayout = LayoutInflater.from(context).inflate(
                    R.layout.insales_product_image_update_dialog, null
                )
                selectedImageView =
                    insalesUpdateProductImageLayout.findViewById(R.id.selected_insales_product_image_view)
                imagesRecyclerView =
                    insalesUpdateProductImageLayout.findViewById(R.id.insales_product_images_recyclerview)
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

                imagesRecyclerView.layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL,
                    false
                )
                imagesRecyclerView.hasFixedSize()
                adapter = BarcodeImageAdapter(
                    requireContext(),
                    barcodeImageList as java.util.ArrayList<String>
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
//                    val searchedImagesList = mutableListOf<String>()
                    val tempImageList = mutableListOf<String>()
//                    val internetSearchLayout = LayoutInflater.from(context)
//                        .inflate(R.layout.internet_image_search_dialog_layout, null)
                    searchedImagesList.clear()
                    loader =
                        internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
                    searchBoxView =
                        internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
                    searchBtnView =
                        internetSearchLayout.findViewById<ImageButton>(R.id.internet_image_search_btn)
                    internetImageRecyclerView =
                        internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
                    val closeBtn =
                        internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
                    voiceSearchIcon =
                        internetSearchLayout.findViewById(R.id.voice_search_internet_images)
                    val barcodeImage = internetSearchLayout.findViewById<AppCompatImageView>(
                        R.id
                            .barcode_img_search_internet_images
                    )
                    internetImageDoneBtn =
                        internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setCancelable(false)
                    builder.setView(internetSearchLayout)
                    val iAlert = builder.create()
                    iAlert.show()

                    internetImageDoneBtn.setOnClickListener {
                        iAlert.dismiss()
                    }

                    barcodeImage.setOnClickListener {
                        barcodeSearchHint = "image"
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
                        searchedImagesList as java.util.ArrayList<String>
                    )
                    internetImageRecyclerView.adapter = internetImageAdapter
                    internetImageAdapter.setOnItemClickListener(object :
                        InternetImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedImage = searchedImagesList[position]

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
                        }

                    })

                    voiceSearchIcon.setOnClickListener {
                        voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                        val voiceLayout = LayoutInflater.from(context).inflate(
                            R.layout.voice_language_setting_layout,
                            null
                        )
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
                                        if (parent!!.selectedItem.toString().toLowerCase(
                                                Locale.ENGLISH
                                            ).contains("english")
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
//                        var creditChargePrice: Float = 0F
//                        if (searchBoxView.text.toString().trim().isNotEmpty()) {
//
//
//                            val firebaseDatabase = FirebaseDatabase.getInstance().reference
//                            firebaseDatabase.child("SearchImagesLimit")
//                                .addListenerForSingleValueEvent(object :
//                                    ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        val creditPrice = snapshot.child("credits")
//                                            .getValue(Int::class.java) as Int
//                                        val images = snapshot.child("images")
//                                            .getValue(Int::class.java) as Int
//                                        creditChargePrice = creditPrice.toFloat() / images
//
//                                        userCurrentCredits =
//                                            appSettings.getString(Constants.userCreditsValue) as String
//
//                                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
//                                            BaseActivity.hideSoftKeyboard(
//                                                requireActivity(),
//                                                searchBtnView
//                                            )
//                                            //Constants.hideKeyboar(requireActivity())
//                                            val query = searchBoxView.text.toString().trim()
//                                            requireActivity().runOnUiThread {
//                                                loader.visibility = View.VISIBLE
//                                            }
//
//                                            BaseActivity.searchInternetImages(
//                                                requireActivity(),
//                                                query,
//                                                object : APICallback {
//                                                    override fun onSuccess(response: JSONObject) {
//                                                        if (loader.visibility == View.VISIBLE) {
//                                                            loader.visibility =
//                                                                View.INVISIBLE
//                                                        }
//
//                                                        val items =
//                                                            response.getJSONArray("items")
//                                                        if (items.length() > 0) {
//                                                            searchedImagesList.clear()
//                                                            for (i in 0 until items.length()) {
//                                                                val item =
//                                                                    items.getJSONObject(
//                                                                        i
//                                                                    )
//                                                                if (item.has("link")) {
//                                                                    searchedImagesList.add(
//                                                                        item.getString(
//                                                                            "link"
//                                                                        )
//                                                                    )
//                                                                }
//                                                            }
//                                                            internetImageAdapter.notifyItemRangeChanged(
//                                                                0,
//                                                                searchedImagesList.size
//                                                            )
//
//                                                        }
//                                                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                                                        val hashMap = HashMap<String, Any>()
//                                                        val remaining =
//                                                            userCurrentCredits.toFloat() - creditChargePrice
//                                                        Log.d("TEST199", "$remaining")
//                                                        hashMap["credits"] =
//                                                            remaining.toString()
//                                                        firebaseDatabase.child(Constants.firebaseUserCredits)
//                                                            .child(Constants.firebaseUserId)
//                                                            .updateChildren(hashMap)
//                                                            .addOnSuccessListener {
//                                                                BaseActivity.getUserCredits(
//                                                                    requireActivity()
//                                                                )
//                                                            }
//                                                            .addOnFailureListener {
//
//                                                            }
//                                                    }
//
//                                                    override fun onError(error: VolleyError) {
//                                                        if (loader.visibility == View.VISIBLE) {
//                                                            loader.visibility =
//                                                                View.INVISIBLE
//                                                        }
//
//                                                        BaseActivity.showAlert(
//                                                            requireActivity(),
//                                                            error.localizedMessage!!
//                                                        )
//                                                    }
//
//                                                })
//                                        } else {
//                                            MaterialAlertDialogBuilder(requireActivity())
//                                                .setMessage(getString(R.string.low_credites_error_message))
//                                                .setCancelable(false)
//                                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                                                    dialog.dismiss()
//                                                }
//                                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                                                    dialog.dismiss()
//                                                    startActivity(
//                                                        Intent(
//                                                            requireActivity(),
//                                                            UserScreenActivity::class.java
//                                                        )
//                                                    )
//                                                }
//                                                .create().show()
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//
//                                    }
//
//                                })
//
//
//                        } else {
//                            if (loader.visibility == View.VISIBLE) {
//                                loader.visibility = View.INVISIBLE
//                            }
//
//                            BaseActivity.showAlert(
//                                requireActivity(),
//                                getString(R.string.empty_text_error)
//                            )
//                        }
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList as java.util.ArrayList<String>,
                            internetImageAdapter
                        )
                    }
                    searchBoxView.setOnEditorActionListener(object :
                        TextView.OnEditorActionListener {
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
                                    searchedImagesList as java.util.ArrayList<String>,
                                    internetImageAdapter
                                )
                            }
                            return false
                        }

                    })
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

                    if (multiImagesList.isNotEmpty()) {
                        alert.dismiss()
                        BaseActivity.startLoading(requireActivity())

                        uploadImages(
                            pItem.id,
                            multiImagesList,
                            object : ResponseListener {
                                override fun onSuccess(result: String) {
                                    if (result.contains("success")) {
                                        Handler(Looper.myLooper()!!).postDelayed(
                                            {
                                                BaseActivity.dismiss()
                                                fetchProducts()//showProducts()
                                            },
                                            2000
                                        )
                                    }
                                }

                            })


//                        viewModel.callAddProductImage(
//                            requireActivity(),
//                            shopName,
//                            email,
//                            password,
//                            selectedImageBase64String,
//                            pItem.id,
//                            "${System.currentTimeMillis()}.jpg",
//                            if (intentType != 3) {
//                                ""
//                            } else {
//                                selectedInternetImage
//                            }
//                        )
//                        viewModel.getAddProductImageResponse()
//                            .observe(requireActivity(), Observer { response ->
//
//                                if (response != null) {
//                                    if (response.get("status").asString == "200") {
//                                        selectedImageBase64String = ""
//                                        selectedInternetImage = ""
//                                        Handler(Looper.myLooper()!!).postDelayed({
//                                            BaseActivity.dismiss()
//                                            fetchProducts()//showProducts()
//                                        }, 6000)
//                                    } else {
//                                        BaseActivity.dismiss()
//                                        BaseActivity.showAlert(
//                                            requireActivity(),
//                                            response.get("message").asString
//                                        )
//                                    }
//                                } else {
//                                    BaseActivity.dismiss()
//                                    BaseActivity.showAlert(
//                                        requireActivity(),
//                                        getString(R.string.something_wrong_error)
//                                    )
//                                }
//                            })
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
                CustomDialog(
                    shopName,
                    email,
                    password,
                    pItem,
                    position,
                    productAdapter,
                    viewModel,
                    object : ResponseListener {
                        override fun onSuccess(result: String) {
                            fetchProducts()
                        }

                    }).show(
                    childFragmentManager,
                    "dialog"
                )
            }

            override fun onItemGrammarCheckClick(
                position: Int,
                grammarCheckBtn: AppCompatImageView,
                title: ExpandableTextView,
                description: ExpandableTextView,
                grammarStatusView: MaterialTextView
            ) {
                val item = productsList[position]
                characters = appSettings.getInt("GRAMMAR_CHARACTERS_LIMIT")
                grammarPrice = appSettings.getString("GRAMMAR_CHARACTERS_PRICE")!!.toFloat()
                unitCharacterPrice = grammarPrice / characters
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

                val totalCharacters = item.title.length + item.fullDesc.length
                val totalCreditPrice = unitCharacterPrice * totalCharacters
                howMuchChargeCredits = totalCreditPrice

                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= totalCreditPrice) {
                    BaseActivity.startLoading(requireActivity())
                    GrammarCheck.check(
                        requireActivity(),
                        item.title,
                        title,
                        1,
                        grammarStatusView,
                        object : GrammarCallback {
                            override fun onSuccess(
                                response: SpannableStringBuilder?,
                                errors: Boolean
                            ) {
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
                                            chargeCreditsPrice()
                                            if (errors) {
                                                grammarStatusView.setTextColor(Color.RED)
                                                grammarStatusView.text =
                                                    requireActivity().resources.getString(
                                                        R.string.error_found_text
                                                    )
                                                //grammarCheckBtn.setImageResource(R.drawable.red_cross)
                                                grammarCheckBtn.setColorFilter(
                                                    ContextCompat.getColor(
                                                        requireActivity(),
                                                        R.color.red
                                                    ),
                                                    android.graphics.PorterDuff.Mode.MULTIPLY
                                                )
                                            } else {
                                                grammarStatusView.setTextColor(Color.GREEN)
                                                grammarStatusView.text =
                                                    requireActivity().resources.getString(
                                                        R.string.no_erros_text
                                                    )
                                                // grammarCheckBtn.setImageResource(R.drawable.green_check_48)
                                                grammarCheckBtn.setColorFilter(
                                                    ContextCompat.getColor(
                                                        requireActivity(),
                                                        R.color.green
                                                    ),
                                                    android.graphics.PorterDuff.Mode.MULTIPLY
                                                )
                                            }
                                        }

                                    })
                            }

                        })
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage(requireActivity().getString(R.string.low_credites_error_message))
                        .setCancelable(false)
                        .setNegativeButton(requireActivity().getString(R.string.no_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(requireActivity().getString(R.string.buy_credits)) { dialog, which ->
                            dialog.dismiss()
                            requireActivity().startActivity(
                                Intent(
                                    requireActivity(),
                                    UserScreenActivity::class.java
                                )
                            )
                        }
                        .create().show()
                }

            }

            override fun onItemGetDescriptionClick(position: Int) {
                val pItem = productsList[position]
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

                if (userCurrentCredits.toFloat() >= 1.0) {
                    Constants.pItemPosition = position
                    Constants.pItem = pItem

                    launchActivity.launch(
                        Intent(
                            requireActivity(),
                            RainForestApiActivity::class.java
                        )
                    )
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
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
            }

            override fun onItemCameraIconClick(
                position: Int,
                title: ExpandableTextView,
                description: ExpandableTextView
            ) {
                val item = productsList[position]
                Constants.pItemPosition = position
                Constants.pItem = item
                Constants.pTitle = title
                Constants.pDescription = description
                //BaseActivity.showAlert(requireActivity(),item.title)
                if (RuntimePermissionHelper.checkCameraPermission(
                        requireActivity(), Constants.CAMERA_PERMISSION
                    )
                ) {
                    // BaseActivity.hideSoftKeyboard(requireActivity())
                    pickImageFromCamera()
                }
            }

            override fun onItemImageIconClick(
                position: Int,
                title: ExpandableTextView,
                description: ExpandableTextView
            ) {
                val item = productsList[position]
                Constants.pItemPosition = position
                Constants.pItem = item
                Constants.pTitle = title
                Constants.pDescription = description
                //BaseActivity.showAlert(requireActivity(),item.fullDesc)
                if (RuntimePermissionHelper.checkCameraPermission(
                        requireActivity(),
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    //BaseActivity.hideSoftKeyboard(requireActivity())
                    pickImageFromGallery()
                }
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
        if (Constants.listUpdateFlag == 0) {
            if (cacheList != null && cacheList.size > 0) {
                originalProductsList.clear()
                productsList.clear()
                originalProductsList.addAll(cacheList)
                originalProductsList.sortByDescending { it.id }
                productsList.addAll(originalProductsList)
                //productAdapter.notifyItemRangeChanged(0, productsList.size)
                productAdapter.submitList(productsList)
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
        Constants.listUpdateFlag = 0
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
                        if (response.get("status").asString == "200") {
                            selectedImageBase64String = ""
                            selectedInternetImage = ""

                            if (index == listImages.size - 1) {
                                index = 0
                                responseListener.onSuccess("success")
                            } else {
                                index++
                                uploadImages(productId, listImages, responseListener)
                            }
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
    }


    private var barcodeImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.hasExtra("SCANNED_BARCODE_VALUE")) {
                    val barcodeId = result.data!!.getStringExtra("SCANNED_BARCODE_VALUE") as String
                    if (barcodeId.isNotEmpty()) {
                        if (barcodeSearchHint == "default") {
                            search(barcodeId, "sku")
                        } else {
                            searchBoxView.setText(barcodeId)
                            Constants.hideKeyboar(requireActivity())
                            startSearch(
                                searchBoxView,
                                searchBtnView,
                                loader,
                                searchedImagesList as java.util.ArrayList<String>,
                                internetImageAdapter
                            )
                        }

                    }
                }


            }
            else{
                Constants.listUpdateFlag = 0
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

                if (voiceSearchHint == "default") {
                    searchBoxView.setText(spokenText)
                    Constants.hideKeyboar(requireActivity())
                    startSearch(
                        searchBoxView, searchBtnView, loader,
                        searchedImagesList as java.util.ArrayList<String>, internetImageAdapter
                    )
                } else {
                    searchBox.setText(spokenText)
                    search(spokenText, "default")
                    voiceSearchHint = "default"
                }
            }
        }

    private fun startSearch(
        searchBoxView: TextInputEditText,
        searchBtnView: ImageButton,
        loader: ProgressBar,
        searchedImagesList: java.util.ArrayList<String>,
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


    private fun pickImageFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher1.launch(
            Intent.createChooser(
                pickPhoto, getString(R.string.image_to_text_mode)
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

    private fun pickImageFromCamera() {
        val takePictureIntent = Intent(context, OcrActivity::class.java)
        cameraResultLauncher1.launch(takePictureIntent)

    }

    private var cameraResultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->


//                // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val text = result.data!!.getStringExtra("SCAN_TEXT")
                if (text!!.isNotEmpty()) {
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    val options = arrayOf(
                        requireActivity().resources.getString(R.string.plus_add_title_text),
                        requireActivity().resources.getString(R.string.plus_add_description_text)
                    )
                    var isTitleChecked = false
                    var isDescriptionChecked = false
                    val checkedItems = booleanArrayOf(false, false)
                    builder.setMultiChoiceItems(
                        options,
                        checkedItems
                    ) { dialog, which, isCheck ->
                        when (which) {
                            0 -> {
                                isTitleChecked = isCheck
                                if (isTitleChecked) {
                                    if (Constants.pTitle != null) {
                                        val stringBuilder = StringBuilder()
                                        Constants.pTitle!!.isExpanded = true
                                        stringBuilder.append(Constants.pTitle!!.text.toString())
                                        stringBuilder.append(" $text")
                                        productsList[Constants.pItemPosition!!].title =
                                            stringBuilder.toString()
                                        Constants.pItem!!.title = stringBuilder.toString()
//                                        if (Constants.pItemPosition != null){
//                                            adapter.notifyItemChanged(Constants.pItemPosition!!)
//                                        }
                                    }
                                }

                            }
                            1 -> {
                                isDescriptionChecked = isCheck
                                if (isDescriptionChecked) {
                                    if (Constants.pDescription != null) {
                                        val stringBuilder = StringBuilder()
                                        Constants.pDescription!!.isExpanded = true
                                        stringBuilder.append(Constants.pDescription!!.text.toString())
                                        stringBuilder.append(" $text")
                                        productsList[Constants.pItemPosition!!].fullDesc =
                                            stringBuilder.toString()
                                        Constants.pItem!!.fullDesc = stringBuilder.toString()
//                                        if (Constants.pItemPosition != null){
//                                            adapter.notifyItemChanged(Constants.pItemPosition!!)
//                                        }
                                    }
                                }
                            }
                            else -> {

                            }
                        }
                    }
                    builder.setPositiveButton(requireActivity().resources.getString(R.string.ok_text)) { dialog, which ->


                        dialog.dismiss()
                        if (isTitleChecked || isDescriptionChecked) {
                            //updateInsalesProductDetail(Constants.pItem!!)
                            CustomDialog(
                                shopName,
                                email,
                                password,
                                productsList[Constants.pItemPosition!!],
                                Constants.pItemPosition!!,
                                productAdapter,
                                viewModel,
                                object : ResponseListener {
                                    override fun onSuccess(result: String) {
                                        fetchProducts()
                                    }

                                }).show(childFragmentManager, "dialog")
                        }

                    }
                    builder.setNegativeButton(
                        requireActivity().resources.getString(R.string.cancel_text),
                        null
                    )
                    val alert = builder.create()
                    alert.show()
                }
//                updateInputBox.setText(text)
//                updateInputBox.setSelection(updateInputBox.text.toString().length)
//                val data: Intent? = result.data
//                val bitmap = data!!.extras!!.get("data") as Bitmap
//                val file = ImageManager.readWriteImage(context,bitmap)
//                cropImage(Uri.fromFile(file))
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imgUri = result.uri
            try {

                if (imgUri != null && Constants.hint == "default") {
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    val options = arrayOf(
                        requireActivity().resources.getString(R.string.plus_add_title_text),
                        requireActivity().resources.getString(R.string.plus_add_description_text)
                    )
                    var isTitleChecked = false
                    var isDescriptionChecked = false
                    val checkedItems = booleanArrayOf(false, false)
                    builder.setMultiChoiceItems(
                        options,
                        checkedItems
                    ) { dialog, which, isCheck ->
                        when (which) {
                            0 -> {
                                isTitleChecked = isCheck
                                if (isTitleChecked) {
                                    if (Constants.pTitle != null) {
                                        TextRecogniser.runTextRecognition(requireActivity(),
                                            Constants.pTitle!!, imgUri, object : ResponseListener {
                                                override fun onSuccess(result: String) {
                                                    productsList[Constants.pItemPosition!!].title =
                                                        result
                                                    Constants.pItem!!.title = result
//                                                 if (Constants.pItemPosition != null){
//                                                     adapter.notifyItemChanged(Constants.pItemPosition!!)
//                                                 }
                                                }

                                            })
                                    }
                                }
                            }
                            1 -> {
                                isDescriptionChecked = isCheck
                                if (isDescriptionChecked) {
                                    if (Constants.pDescription != null) {
                                        TextRecogniser.runTextRecognition(
                                            requireActivity(),
                                            Constants.pDescription!!,
                                            imgUri,
                                            object : ResponseListener {
                                                override fun onSuccess(result: String) {
                                                    productsList[Constants.pItemPosition!!].fullDesc =
                                                        result
                                                    Constants.pItem!!.fullDesc = result
//                                                if (Constants.pItemPosition != null){
//                                                    adapter.notifyItemChanged(Constants.pItemPosition!!)
//                                                }
                                                }

                                            })
                                    }
                                }
                            }
                            else -> {

                            }
                        }
                    }
                    builder.setPositiveButton(requireActivity().resources.getString(R.string.ok_text)) { dialog, which ->

                        dialog.dismiss()
                        if (isTitleChecked || isDescriptionChecked) {

                            CustomDialog(
                                shopName,
                                email,
                                password,
                                productsList[Constants.pItemPosition!!],
                                Constants.pItemPosition!!,
                                productAdapter,
                                viewModel,
                                object : ResponseListener {
                                    override fun onSuccess(result: String) {
                                        fetchProducts()
                                    }

                                }).show(childFragmentManager, "dialog")
                        }
                    }
                    builder.setNegativeButton(
                        requireActivity().resources.getString(R.string.cancel_text),
                        null
                    )
                    val alert = builder.create()
                    alert.show()
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                    for (fragment in childFragmentManager.fragments) {
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun chargeCreditsPrice() {
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        val remaining = userCurrentCredits.toFloat() - howMuchChargeCredits
        userCurrentCredits = remaining.toString()
        hashMap["credits"] = userCurrentCredits
        firebaseDatabase.child(Constants.firebaseUserCredits)
            .child(Constants.firebaseUserId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                howMuchChargeCredits = 0F
                BaseActivity.getUserCredits(
                    requireActivity()
                )
            }
            .addOnFailureListener {

            }
    }

    private fun fetchProducts() {
        currentPage = 1
        dialogStatus = 1
        fetchProducts(currentPage)
    }

    var launchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && Constants.pItem != null) {
                val data: Intent? = result.data

                if (data != null && data.hasExtra("TITLE")) {
                    val title = data.getStringExtra("TITLE") as String
                    if (title.isNotEmpty()) {
                        val currentPItemTitle = Constants.pItem!!.title
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemTitle)
                        stringBuilder.append(title)
                        Constants.pItem!!.title = stringBuilder.toString()
                    }
                }

                if (data != null && data.hasExtra("DESCRIPTION")) {
                    val description = data.getStringExtra("DESCRIPTION") as String
                    if (description.isNotEmpty()) {

                        val currentPItemDescription = Constants.pItem!!.fullDesc
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemDescription)
                        stringBuilder.append(description)
                        Constants.pItem!!.fullDesc = stringBuilder.toString()

                    }
                }
                CustomDialog(
                    shopName,
                    email,
                    password,
                    Constants.pItem!!,
                    Constants.pItemPosition!!,
                    productAdapter,
                    viewModel, object : ResponseListener {
                        override fun onSuccess(result: String) {
                            fetchProducts()
                        }

                    }
                ).show(childFragmentManager, "dialog")
                productAdapter.notifyItemChanged(Constants.pItemPosition!!)

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
                            val variants = product.getAsJsonArray("variants")
                            val variantsItem = variants[0].asJsonObject
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
                                    if (variantsItem.get("sku").isJsonNull) {
                                        ""
                                    } else {
                                        variantsItem.get("sku").asString
                                    },
                                    imagesList as ArrayList<ProductImages>
                                )
                            )
                            originalProductsList.sortByDescending { it.id }
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
                            //productAdapter.notifyItemRangeChanged(0, productsList.size)
                            productAdapter.submitList(productsList)
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
//                selectedImageBase64String =
//                    ImageManager.convertImageToBase64(requireActivity(), currentPhotoPath!!)
//                Log.d("TEST199", selectedImageBase64String)
                Glide.with(requireActivity())
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)
                barcodeImageList.add(currentPhotoPath!!)
                multiImagesList.add(currentPhotoPath!!)
                adapter.notifyDataSetChanged()
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
                searchBox.clearFocus()
                requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                if (searchBox.text.toString().trim().isNotEmpty()) {
                    searchBox.setText("")
                }
                if (productsList.isNotEmpty()) {
                    productsList.clear()
                }
                productsList.addAll(originalProductsList)
                //productAdapter.notifyItemRangeChanged(0, productsList.size)
                productAdapter.submitList(productsList)

            }
            R.id.insales_products_search_btn -> {
                val query = searchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    BaseActivity.hideSoftKeyboard(requireActivity(), searchBox)
                    searchBox.clearFocus()
                    requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                    search(query, "default")
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

//    private fun updateInsalesProductDetail(pItem: Product){
//        viewModel.callUpdateProductDetail(
//            requireContext(),
//            shopName,
//            email,
//            password,
//            pItem.id,
//            pItem.title,
//            pItem.shortDesc,
//            pItem.fullDesc
//        )
//        viewModel.getUpdateProductDetailResponse()
//            .observe(requireActivity(), Observer { response ->
//                if (response != null) {
//                    if (response.get("status").asString == "200") {
//                        BaseActivity.dismiss()
//                        Constants.pItem = null
//                        Constants.pItemPosition = null
//                        Constants.pTitle = null
//                        Constants.pDescription = null
//                        Toast.makeText(
//                            requireContext(),
//                            getString(R.string.product_updated_successfully),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        fetchProducts()
//
//                    } else {
//                        BaseActivity.dismiss()
//                        BaseActivity.showAlert(
//                            requireActivity(),
//                            response.get("message").asString
//                        )
//                    }
//                } else {
//                    BaseActivity.dismiss()
//                }
//            })
//    }


    private fun addProduct() {

//        appSettings.remove("AP_PRODUCT_CATEGORY")
//        appSettings.remove("AP_PRODUCT_TITLE")
//        appSettings.remove("AP_PRODUCT_DESCRIPTION")
//        appSettings.remove("AP_PRODUCT_QUANTITY")
//        appSettings.remove("AP_PRODUCT_PRICE")

        AddProductCustomDialog(
            originalCategoriesList,
            shopName,
            email,
            password,
            viewModel,
            object : ResponseListener {
                override fun onSuccess(result: String) {
                    fetchProducts()
                }

            }).show(childFragmentManager, "add-dialog")

    }


    private fun search(text: String?, type: String) {

        val matchedProducts = mutableListOf<Product>()


        text?.let {

            if (type == "default") {
                productsList.forEach { item ->
                    if (item.title.contains(text, true)) {
                        matchedProducts.add(item)
                    }
                }
            } else {
                productsList.forEach { item ->
                    if (item.sku.contains(text, true)) {
                        matchedProducts.add(item)
                    }
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
                //productAdapter.notifyItemRangeChanged(0, productsList.size)
                productAdapter.submitList(productsList)

            }
        }
    }


    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == "update-products") {
                fetchProducts()
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


    class CustomDialog(
        private val shopName: String,
        private val email: String,
        private val password: String,
        private val pItem: Product,
        private val position: Int,
        private val insalesAdapter: InSalesProductsAdapter1,
        private val viewModel: SalesCustomersViewModel,
        private val listener: ResponseListener
    ) : DialogFragment(), View.OnClickListener {

        private var insalesFragment: InsalesFragment? = null
        private var defaultLayout = 0

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
            val v =
                inflater.inflate(
                    R.layout.insales_product_detail_update_dialog_layout,
                    container
                )

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

            val titleClearBrush =
                dialogLayout.findViewById<AppCompatImageView>(R.id.title_clear_brush_view)
            val shortDescClearBrush =
                dialogLayout.findViewById<AppCompatImageView>(R.id.short_desc_clear_brush_view)
            val fullDescClearBrush =
                dialogLayout.findViewById<AppCompatImageView>(R.id.full_desc_clear_brush_view)

            if (pItem.title.length > 10) {
                insalesFragment!!.titleBox.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                dynamicTitleTextViewWrapper.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            } else {
                //holder.productTitle.text = context.getString(R.string.product_title_error)
                insalesFragment!!.titleBox.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.light_red
                    )
                )
                dynamicTitleTextViewWrapper.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.light_red
                    )
                )
            }

            if (pItem.fullDesc.length > 10) {
                insalesFragment!!.fullDescriptionBox.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                dynamicFullDescTextViewWrapper.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            } else {
                //holder.productDescription.text = context.getString(R.string.product_description_error)
                insalesFragment!!.fullDescriptionBox.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.light_red
                    )
                )
                dynamicFullDescTextViewWrapper.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.light_red
                    )
                )
            }

            titleClearBrush.setOnClickListener {
                dynamicTitleTextViewWrapper.removeAllViews()
                insalesFragment!!.titleTextViewList.clear()
                insalesFragment!!.titleBox.setText("")
                secondLinearLayout.visibility = View.GONE
                firstLinearLayout.visibility = View.VISIBLE

                defaultLayout = 1
                swapLayoutBtn.isChecked = true
                insalesFragment!!.titleBox.requestFocus()
                Constants.openKeyboar(requireContext())
            }

            shortDescClearBrush.setOnClickListener {
                dynamicShortDescTextViewWrapper.removeAllViews()
                insalesFragment!!.shortDescTextViewList.clear()
                productShortDescriptionBox.setText("")
                secondLinearLayout.visibility = View.GONE
                firstLinearLayout.visibility = View.VISIBLE

                defaultLayout = 1
                swapLayoutBtn.isChecked = true
                productShortDescriptionBox.requestFocus()
                Constants.openKeyboar(requireContext())
            }

            fullDescClearBrush.setOnClickListener {
                dynamicFullDescTextViewWrapper.removeAllViews()
                insalesFragment!!.fullDescTextViewList.clear()
                insalesFragment!!.fullDescriptionBox.setText("")
                secondLinearLayout.visibility = View.GONE
                firstLinearLayout.visibility = View.VISIBLE

                defaultLayout = 1
                swapLayoutBtn.isChecked = true
                insalesFragment!!.fullDescriptionBox.requestFocus()
                Constants.openKeyboar(requireContext())
            }

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

                    defaultLayout = 1
                    insalesFragment!!.fullDescriptionBox.setText(pItem.fullDesc)
//                    if (insalesFragment!!.titleBox.text.toString().isNotEmpty()) {
                    insalesFragment!!.titleBox.setText(pItem.title)
                        insalesFragment!!.titleBox.setSelection(pItem.title.length)
//                    }
                    Constants.openKeyboar(requireContext())
                    insalesFragment!!.titleBox.requestFocus()
                } else {
                    Constants.hideKeyboar(requireContext())
                    //BaseActivity.startLoading(requireContext())
                    firstLinearLayout.visibility = View.GONE
                    secondLinearLayout.visibility = View.VISIBLE
                    defaultLayout = 0
                    //BaseActivity.dismiss()
                }
            }

            insalesFragment!!.titleBox.setSelection(pItem.title.length)
            insalesFragment!!.titleBox.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    pItem.title = insalesFragment!!.titleBox.text.toString()
                }

            })
            insalesFragment!!.fullDescriptionBox.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    pItem.fullDesc = insalesFragment!!.fullDescriptionBox.text.toString()
                }

            })

            dialogCancelBtn.setOnClickListener {
                BaseActivity.hideSoftKeyboard(requireContext(), dialogCancelBtn)

                dismiss()
            }

            dialogUpdateBtn.setOnClickListener {
                val titleText = insalesFragment!!.titleBox.text.toString().trim()
                val shortDesc = productShortDescriptionBox.text.toString().trim()
                val fullDesc = insalesFragment!!.fullDescriptionBox.text.toString().trim()

                if (defaultLayout == 0) {
                    var stringBuilder = StringBuilder()

                    for (i in 0 until (dynamicTitleTextViewWrapper as ViewGroup).childCount) {
                        val nextChild = (dynamicTitleTextViewWrapper as ViewGroup).getChildAt(i)
                        val text = (nextChild as MaterialTextView).text.toString()
                        stringBuilder.append(text)
                        stringBuilder.append(" ")
                    }

                    pItem.title = stringBuilder.toString().trim()
                    stringBuilder = StringBuilder()

                    for (i in 0 until (dynamicShortDescTextViewWrapper as ViewGroup).childCount) {
                        val nextChild =
                            (dynamicShortDescTextViewWrapper as ViewGroup).getChildAt(i)
                        val text = (nextChild as MaterialTextView).text.toString()
                        stringBuilder.append(text)
                        stringBuilder.append(" ")
                    }

                    pItem.shortDesc = stringBuilder.toString().trim()

                    stringBuilder = StringBuilder()

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
                defaultLayout = 0
                if (titleText.isNotEmpty()) {
                    BaseActivity.startLoading(
                        requireActivity(),
                        getString(R.string.please_wait_product_update_message)
                    )

                    Paper.book().delete(Constants.cacheProducts)
                    Paper.book()
                        .write(Constants.cacheProducts, insalesFragment!!.originalProductsList)
                    insalesAdapter.notifyItemChanged(position)

                    viewModel.callUpdateProductDetail(
                        requireContext(),
                        shopName,
                        email,
                        password,
                        pItem.id,
                        pItem.title,
                        pItem.shortDesc,
                        pItem.fullDesc
                    )
                    viewModel.getUpdateProductDetailResponse()
                        .observe(requireActivity(), Observer { response ->
                            if (response != null) {
                                if (response.get("status").asString == "200") {
                                    BaseActivity.dismiss()
                                    Constants.pItem = null
                                    Constants.pItemPosition = null
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.product_updated_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismiss()
                                    listener.onSuccess("")
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


                    val editTextBox = balloon.getContentView()
                        .findViewById<TextInputEditText>(R.id.balloon_edit_text)
                    editTextBox.setText(textView.text.toString().trim())
                    val clearTextView = balloon.getContentView()
                        .findViewById<AppCompatImageView>(R.id.balloon_brush_clear_view)
                    val closeBtn = balloon.getContentView()
                        .findViewById<AppCompatButton>(R.id.balloon_close_btn)
                    val applyBtn = balloon.getContentView()
                        .findViewById<AppCompatButton>(R.id.balloon_apply_btn)
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

                    clearTextView.setOnClickListener {
                        editTextBox.setText("")
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


    class AddProductCustomDialog(
        private val originalCategoriesList: MutableList<Category>,
        private val shopName: String,
        private val email: String,
        private val password: String,
        private val viewModel: SalesCustomersViewModel,
        private val listener: ResponseListener
    ) : DialogFragment()
    {
        private lateinit var internetImageRecyclerView:RecyclerView
        private lateinit var internetImageDoneBtn: MaterialButton
        private lateinit var apTitleActiveListNameView: MaterialTextView
        private lateinit var apDescriptionActiveListNameView: MaterialTextView
        private lateinit var apQuantityActiveListNameView: MaterialTextView
        private lateinit var apPriceActiveListNameView: MaterialTextView
        private var insalesFragment: InsalesFragment? = null
        private var selectedCategoryId = 0
        private var selectedInternetImage = ""
        private var userCurrentCredits = ""
        private lateinit var appSettings: AppSettings
        private lateinit var selectedImageView: AppCompatImageView
        private var currentPhotoPath: String? = null
        private var selectedImageBase64String: String = ""
        private var intentType = 0
        private lateinit var categoriesSpinner: AppCompatSpinner
        private lateinit var apTitleView: TextInputEditText
        private lateinit var apDescriptionView: TextInputEditText
        private var finalTitleText = ""
        private var finalDescriptionText = ""
        private var finalQuantityText = ""
        private var finalPriceText = ""

        //private var CIVType = ""
        private lateinit var quickModeCheckBox: MaterialCheckBox
        private lateinit var apViewPager: MyViewPager
        private lateinit var apFirstLayout: LinearLayout
        private lateinit var apSecondLayout: LinearLayout
        private lateinit var apNextPreviousButtons: LinearLayout
        private lateinit var apPreviousBtn: MaterialTextView
        private lateinit var apNextBtn: MaterialTextView
        private lateinit var apBackArrowBtn: AppCompatImageView
        private lateinit var internetImageAdapter: InternetImageAdapter
        private lateinit var searchBtnView: ImageButton
        private lateinit var searchBoxView: TextInputEditText
        private lateinit var loader: ProgressBar
        private lateinit var voiceSearchIcon: AppCompatImageView
        private var voiceLanguageCode = "en"
        val searchedImagesList = mutableListOf<String>()
        private lateinit var addProdcutViewModel: AddProductViewModel
        private lateinit var testDataBtn: MaterialTextView
        private lateinit var getTitleBtn: MaterialTextView
        private lateinit var imagesRecyclerView: RecyclerView
        private var barcodeImageList = mutableListOf<String>()
        var multiImagesList = mutableListOf<String>()
        private lateinit var imagesAdapter: BarcodeImageAdapter

        var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == "dialog-dismiss") {
                    val intentV = Intent("update-products")
                    LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intentV)
                    dismiss()
                } else if (intent.action != null && intent.action == "move-next") {
                    apViewPager.currentItem = 1
                }
            }
        }


        override fun onResume() {
            super.onResume()
            val intentFilter = IntentFilter()
            intentFilter.addAction("dialog-dismiss")
            intentFilter.addAction("move-next")
            LocalBroadcastManager.getInstance(
                requireActivity()
            ).registerReceiver(broadcastReceiver, intentFilter)
        }

        override fun onPause() {
            super.onPause()
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
                broadcastReceiver
            );
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialogStyle
            )
            appSettings = AppSettings(requireActivity())
            insalesFragment = InsalesFragment()
            tableGenerator = TableGenerator(requireActivity())
            addProdcutViewModel = ViewModelProvider(this)[AddProductViewModel::class.java]
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val v =
                inflater.inflate(R.layout.insales_add_product_dialog, container)

            initViews(v)

            return v
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            return dialog
        }

        private fun initViews(view: View) {
            categoriesSpinner =
                view.findViewById(R.id.ap_cate_spinner)
            apTitleView = view.findViewById(R.id.ap_title)
            val apTestDataView = view.findViewById<MaterialTextView>(R.id.test_data_button)
            apDescriptionView =
                view.findViewById(R.id.ap_description)
            testDataBtn = view.findViewById(R.id.test_data_button1)
            quickModeCheckBox = view.findViewById(R.id.ap_quick_product_mode)
            apViewPager = view.findViewById(R.id.ap_viewpager)
            apFirstLayout = view.findViewById(R.id.ap_first_layout)
            apSecondLayout = view.findViewById(R.id.ap_second_layout)
            apPreviousBtn = view.findViewById(R.id.ap_previous_btn)
            apNextBtn = view.findViewById(R.id.ap_next_btn)
            apBackArrowBtn = view.findViewById(R.id.ap_back_arrow)
            apNextPreviousButtons = view.findViewById(R.id.ap_next_previous_buttons)
            getTitleBtn = view.findViewById(R.id.get_title_text_view)
            imagesRecyclerView = view.findViewById(R.id.ap_images_recyclerview)
            imagesRecyclerView.layoutManager = LinearLayoutManager(
                requireActivity(), RecyclerView.HORIZONTAL,
                false
            )
            imagesRecyclerView.hasFixedSize()
            imagesAdapter = BarcodeImageAdapter(
                requireContext(),
                barcodeImageList as java.util.ArrayList<String>
            )
            imagesRecyclerView.adapter = imagesAdapter
            imagesAdapter.setOnItemClickListener(object :
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
                        imagesAdapter.notifyItemRemoved(position)
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

            getTitleBtn.setOnClickListener {
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

                if (userCurrentCredits.toFloat() >= 1.0) {

                    launchActivity1.launch(
                        Intent(
                            requireActivity(),
                            RainForestApiActivity::class.java
                        )
                    )
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
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
            }

            val apTitleViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_title_wrapper)
            val apTitleDefaultInputWrapper =
                view.findViewById<TextInputLayout>(R.id.ap_title_non_changeable_default_text_input_wrapper)
            val apDescriptionViewWrapper =
                view.findViewById<TextInputLayout>(R.id.ap_description_wrapper)
            val apDescriptionDefaultInputWrapper =
                view.findViewById<TextInputLayout>(R.id.ap_description_non_changeable_default_text_input_wrapper)
            val apQuantityViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_quantity_wrapper)
            val apQuantityDefaultInputWrapper =
                view.findViewById<TextInputLayout>(R.id.ap_quantity_non_changeable_default_text_input_wrapper)
            val apPriceViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_price_wrapper)
            val apPriceDefaultInputWrapper =
                view.findViewById<TextInputLayout>(R.id.ap_price_non_changeable_default_text_input_wrapper)


            val apAddDescriptionView =
                view.findViewById<MaterialTextView>(R.id.ap_add_description_text_view)
            val apQuantityView = view.findViewById<TextInputEditText>(R.id.ap_quantity)
            val apPriceView = view.findViewById<TextInputEditText>(R.id.ap_price)
            val apSubmitBtn = view.findViewById<MaterialButton>(R.id.ap_dialog_submit_btn)
            val apCancelBtn = view.findViewById<MaterialButton>(R.id.ap_dialog_cancel_btn)

            val apTitleSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_title_options_spinner)
            val apDescriptionSpinner =
                view.findViewById<AppCompatSpinner>(R.id.ap_description_options_spinner)
            val apQuantitySpinner =
                view.findViewById<AppCompatSpinner>(R.id.ap_quantity_options_spinner)
            val apPriceSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_price_options_spinner)

            val apTitleListBtn =
                view.findViewById<MaterialButton>(R.id.ap_title_list_with_fields_btn)
            val apDescriptionListBtn =
                view.findViewById<MaterialButton>(R.id.ap_description_list_with_fields_btn)
            val apQuantityListBtn =
                view.findViewById<MaterialButton>(R.id.ap_quantity_list_with_fields_btn)
            val apPriceListBtn =
                view.findViewById<MaterialButton>(R.id.ap_price_list_with_fields_btn)

            val apTitleDefaultInputBox =
                view.findViewById<TextInputEditText>(R.id.ap_title_non_changeable_default_text_input)
            val apDescriptionDefaultInputBox =
                view.findViewById<TextInputEditText>(R.id.ap_description_non_changeable_default_text_input)
            val apQuantityDefaultInputBox =
                view.findViewById<TextInputEditText>(R.id.ap_quantity_non_changeable_default_text_input)
            val apPriceDefaultInputBox =
                view.findViewById<TextInputEditText>(R.id.ap_price_non_changeable_default_text_input)


            val apTitleListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_title_list_spinner)
            val apDescriptionListSpinner =
                view.findViewById<AppCompatSpinner>(R.id.ap_description_list_spinner)
            val apQuantityListSpinner =
                view.findViewById<AppCompatSpinner>(R.id.ap_quantity_list_spinner)
            val apPriceListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_price_list_spinner)

            apTitleActiveListNameView =
                view.findViewById<MaterialTextView>(R.id.ap_title_active_list_name)
            apDescriptionActiveListNameView =
                view.findViewById<MaterialTextView>(R.id.ap_description_active_list_name)
            apQuantityActiveListNameView =
                view.findViewById<MaterialTextView>(R.id.ap_quantity_active_list_name)
            apPriceActiveListNameView =
                view.findViewById<MaterialTextView>(R.id.ap_price_active_list_name)

            val apTitleCameraRecView = view.findViewById<LinearLayout>(R.id.ap_title_camera_layout)
            val apTitleImageRecView = view.findViewById<LinearLayout>(R.id.ap_title_images_layout)
            val apTitleVoiceRecView = view.findViewById<LinearLayout>(R.id.ap_title_voice_layout)
            val quickModeStatus = appSettings.getInt("QUICK_MODE_STATUS")
            if (quickModeStatus == 1) {
                quickModeCheckBox.isChecked = true
                apFirstLayout.visibility = View.GONE
                apSecondLayout.visibility = View.VISIBLE
                apNextPreviousButtons.visibility = View.VISIBLE
            } else {
                apSecondLayout.visibility = View.GONE
                apNextPreviousButtons.visibility = View.GONE
                apFirstLayout.visibility = View.VISIBLE
            }
            quickModeCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                if (isChecked) {

                    if (userCurrentCredits.toFloat() > 0) {
                        appSettings.putInt("QUICK_MODE_STATUS", 1)
                        quickModeCheckBox.isChecked = true
                        apFirstLayout.visibility = View.GONE
                        apSecondLayout.visibility = View.VISIBLE
                        apNextPreviousButtons.visibility = View.VISIBLE
                    } else {
                        quickModeCheckBox.isChecked = false
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

                } else {
                    appSettings.putInt("QUICK_MODE_STATUS", 0)
                    apSecondLayout.visibility = View.GONE
                    apNextPreviousButtons.visibility = View.GONE
                    apFirstLayout.visibility = View.VISIBLE
                }
            }



            apBackArrowBtn.setOnClickListener {
                dismiss()
            }
            apViewPager.offscreenPageLimit = 7
            val fragmentAdapter = ViewPagerAdapter(childFragmentManager)
            fragmentAdapter.addFragment(ApScannerFragment(), "ap_scanner_fr")
            fragmentAdapter.addFragment(ApCategoryInputFragment(), "ap_category_fr")
            fragmentAdapter.addFragment(ApTitleInputFragment(), "ap_title_fr")
            fragmentAdapter.addFragment(ApDescriptionInputFragment(), "ap_description_fr")
            fragmentAdapter.addFragment(ApQuantityInputFragment(), "ap_quantity_fr")
            fragmentAdapter.addFragment(ApPriceInputFragment(), "ap_price_fr")
            fragmentAdapter.addFragment(ApImageUploadFragment(), "ap_image_fr")
            apViewPager.adapter = fragmentAdapter

            testDataBtn.setOnClickListener {
                val currentFragment =
                    childFragmentManager.getFragments().get(apViewPager.getCurrentItem())
                if (currentFragment is ApTitleInputFragment) {
                    currentFragment.updateTestData("Test Title")
                } else if (currentFragment is ApDescriptionInputFragment) {
                    currentFragment.updateTestData("Test Description")
                } else if (currentFragment is ApQuantityInputFragment) {
                    currentFragment.updateTestData("1")
                } else if (currentFragment is ApPriceInputFragment) {
                    currentFragment.updateTestData("1")
                }
            }

            apViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {

                    if (position == 2 || position == 3) {
                        getTitleBtn.visibility = View.VISIBLE
                    } else {
                        getTitleBtn.visibility = View.GONE
                    }

                    if (position == 0 || position == 1 || position == 6) {
                        testDataBtn.visibility = View.GONE
                    } else {
                        testDataBtn.visibility = View.VISIBLE
                    }
                    if (position == 0) {
                        apPreviousBtn.visibility = View.INVISIBLE
                    } else {
                        apPreviousBtn.visibility = View.VISIBLE
                    }
                    if (position < apViewPager.adapter!!.count - 1) {
                        apNextBtn.visibility = View.VISIBLE
                    } else {
                        apNextBtn.visibility = View.INVISIBLE
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })

            apPreviousBtn.setOnClickListener {
                apViewPager.setCurrentItem(apViewPager.currentItem - 1, true)
            }

            apNextBtn.setOnClickListener {
                apViewPager.setCurrentItem(apViewPager.currentItem + 1, true)
            }


            apTitleCameraRecView.setOnClickListener {
                Constants.CIVType = "ap_title"
                if (RuntimePermissionHelper.checkCameraPermission(
                        requireActivity(), Constants.CAMERA_PERMISSION
                    )
                ) {
                    BaseActivity.hideSoftKeyboard(requireActivity(), apTitleCameraRecView)
                    pickImageFromCamera()
                }
            }
            apTitleImageRecView.setOnClickListener {
                Constants.CIVType = "ap_title"
                Constants.hint = "ap"
                if (RuntimePermissionHelper.checkCameraPermission(
                        requireActivity(),
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    BaseActivity.hideSoftKeyboard(requireActivity(), apTitleImageRecView)
                    pickImageFromGallery()
                }
            }
            apTitleVoiceRecView.setOnClickListener {
                Constants.CIVType = "ap_title"
                voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                val voiceLayout = LayoutInflater.from(context).inflate(
                    R.layout.voice_language_setting_layout,
                    null
                )
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
                            voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(
                                    Locale.ENGLISH
                                ).contains("english")
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

            val apDescriptionCameraRecView =
                view.findViewById<LinearLayout>(R.id.ap_description_camera_layout)
            val apDescriptionImageRecView =
                view.findViewById<LinearLayout>(R.id.ap_description_images_layout)
            val apDescriptionVoiceRecView =
                view.findViewById<LinearLayout>(R.id.ap_description_voice_layout)

            apDescriptionCameraRecView.setOnClickListener {
                Constants.CIVType = "ap_description"

                if (RuntimePermissionHelper.checkCameraPermission(
                        requireActivity(), Constants.CAMERA_PERMISSION
                    )
                ) {
                    BaseActivity.hideSoftKeyboard(requireActivity(), apDescriptionCameraRecView)
                    pickImageFromCamera()
                }
            }
            apDescriptionImageRecView.setOnClickListener {
                Constants.CIVType = "ap_description"
                Constants.hint = "ap"
                if (RuntimePermissionHelper.checkCameraPermission(
                        requireActivity(),
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    BaseActivity.hideSoftKeyboard(requireActivity(), apDescriptionImageRecView)
                    pickImageFromGallery()
                }
            }
            apDescriptionVoiceRecView.setOnClickListener {
                Constants.CIVType = "ap_description"
                voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                val voiceLayout = LayoutInflater.from(context).inflate(
                    R.layout.voice_language_setting_layout,
                    null
                )
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
                            voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(
                                    Locale.ENGLISH
                                ).contains("english")
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


            val apTitleSpinnerSelectedPosition =
                appSettings.getInt("AP_TITLE_SPINNER_SELECTED_POSITION")
            val apTitleDefaultValue = appSettings.getString("AP_TITLE_DEFAULT_VALUE")
            val apTitleListId = appSettings.getInt("AP_TITLE_LIST_ID")
            val apTitleActiveListName = appSettings.getString("AP_TITLE_LIST_NAME")
            if (apTitleActiveListName!!.isEmpty()) {
                apTitleActiveListNameView.text = "Active List: None"
            } else {
                apTitleActiveListNameView.text = "Active List: $apTitleActiveListName"
            }
            apTitleSpinner.setSelection(apTitleSpinnerSelectedPosition)


            apTitleListBtn.setOnClickListener {
                openListWithFieldsDialog("ap_title")
            }

            if (apTitleSpinnerSelectedPosition == 1) {
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.VISIBLE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleDefaultInputBox.setText(apTitleDefaultValue)
                apTitleView.setText(apTitleDefaultValue)
            } else if (apTitleSpinnerSelectedPosition == 2) {
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleListBtn.visibility = View.VISIBLE
                apTitleActiveListNameView.visibility = View.VISIBLE
                apTitleViewWrapper.visibility = View.GONE
                apTitleListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apTitleListId)
                val listValues = listOptions.split(",")
                val apTitleSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apTitleListSpinner.adapter = apTitleSpinnerAdapter

                apTitleListSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }

            } else if (apTitleSpinnerSelectedPosition == 3) {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleVoiceRecView.visibility = View.VISIBLE
            } else if (apTitleSpinnerSelectedPosition == 4) {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleVoiceRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleCameraRecView.visibility = View.VISIBLE
            } else if (apTitleSpinnerSelectedPosition == 5) {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleImageRecView.visibility = View.VISIBLE
            } else {
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
            }



            apTitleDefaultInputBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    apTitleView.setText(s.toString())
                    appSettings.putString("AP_TITLE_DEFAULT_VALUE", s.toString())
                }

            })

            apTitleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    appSettings.putInt("AP_TITLE_SPINNER_SELECTED_POSITION", position)

                    if (position == 1) {
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.VISIBLE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        if (apTitleDefaultValue!!.isNotEmpty()) {
                            apTitleDefaultInputBox.setText(apTitleDefaultValue)
                            apTitleView.setText(apTitleDefaultValue)
                        } else {
                            apTitleView.setText(appSettings.getString("AP_TITLE_VALUE"))
                            apTitleView.setSelection(apTitleView.text.toString().length)
                        }
                    } else if (position == 2) {
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleListBtn.visibility = View.VISIBLE
                        apTitleActiveListNameView.visibility = View.VISIBLE
                        apTitleViewWrapper.visibility = View.GONE
                        apTitleView.setText(appSettings.getString("AP_TITLE_VALUE"))
                        apTitleView.setSelection(apTitleView.text.toString().length)
                        apTitleListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apTitleListId)
                        val listValues = listOptions.split(",")
                        val apTitleSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apTitleListSpinner.adapter = apTitleSpinnerAdapter

                        apTitleListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }

                    } else if (position == 3) {
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleView.setText(appSettings.getString("AP_TITLE_VALUE"))
                        apTitleView.setSelection(apTitleView.text.toString().length)
                        apTitleVoiceRecView.visibility = View.VISIBLE
                    } else if (position == 4) {
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleView.setText(appSettings.getString("AP_TITLE_VALUE"))
                        apTitleView.setSelection(apTitleView.text.toString().length)
                        apTitleCameraRecView.visibility = View.VISIBLE
                    } else if (position == 5) {
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleView.setText(appSettings.getString("AP_TITLE_VALUE"))
                        apTitleView.setSelection(apTitleView.text.toString().length)
                        apTitleImageRecView.visibility = View.VISIBLE
                    } else {
                        apTitleVoiceRecView.visibility = View.GONE
                        apTitleCameraRecView.visibility = View.GONE
                        apTitleImageRecView.visibility = View.GONE
                        apTitleListBtn.visibility = View.GONE
                        apTitleActiveListNameView.visibility = View.GONE
                        apTitleDefaultInputWrapper.visibility = View.GONE
                        apTitleListSpinner.visibility = View.GONE
                        apTitleViewWrapper.visibility = View.VISIBLE
                        apTitleView.setText(appSettings.getString("AP_TITLE_VALUE"))
                        apTitleView.setSelection(apTitleView.text.toString().length)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

            apTitleView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    appSettings.putString("AP_TITLE_VALUE", s.toString())
                }

            })

            val apDescriptionSpinnerSelectedPosition =
                appSettings.getInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION")
            val apDescriptionDefaultValue = appSettings.getString("AP_DESCRIPTION_DEFAULT_VALUE")
            val apDescriptionListId = appSettings.getInt("AP_DESCRIPTION_LIST_ID")
            val apDescriptionActiveListName = appSettings.getString("AP_DESCRIPTION_LIST_NAME")
            if (apDescriptionActiveListName!!.isEmpty()) {
                apDescriptionActiveListNameView.text = "Active List: None"
            } else {
                apDescriptionActiveListNameView.text = "Active List: $apDescriptionActiveListName"
            }
            apDescriptionSpinner.setSelection(apDescriptionSpinnerSelectedPosition)
            apDescriptionListBtn.setOnClickListener {
                openListWithFieldsDialog("ap_description")
            }
            if (apDescriptionSpinnerSelectedPosition == 1) {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.VISIBLE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionDefaultInputBox.setText(apDescriptionDefaultValue)
                apDescriptionView.setText(apDescriptionDefaultValue)
            } else if (apDescriptionSpinnerSelectedPosition == 2) {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionListBtn.visibility = View.VISIBLE
                apDescriptionActiveListNameView.visibility = View.VISIBLE
                apDescriptionViewWrapper.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apDescriptionListId)
                val listValues = listOptions.split(",")
                val apDescriptionSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                apDescriptionListSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }

            } else if (apDescriptionSpinnerSelectedPosition == 3) {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionVoiceRecView.visibility = View.VISIBLE
            } else if (apDescriptionSpinnerSelectedPosition == 4) {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionCameraRecView.visibility = View.VISIBLE
            } else if (apDescriptionSpinnerSelectedPosition == 5) {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionImageRecView.visibility = View.VISIBLE
            } else {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
            }

            apDescriptionDefaultInputBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    apDescriptionView.setText(s.toString())
                    appSettings.putString("AP_DESCRIPTION_DEFAULT_VALUE", s.toString())
                }

            })

            apDescriptionSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        appSettings.putInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION", position)
                        if (position == 1) {
                            apDescriptionVoiceRecView.visibility = View.GONE
                            apDescriptionCameraRecView.visibility = View.GONE
                            apDescriptionImageRecView.visibility = View.GONE
                            apDescriptionListBtn.visibility = View.GONE
                            apDescriptionActiveListNameView.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.VISIBLE
                            apDescriptionViewWrapper.visibility = View.VISIBLE
                            if (apDescriptionDefaultValue!!.isNotEmpty()) {
                                apDescriptionDefaultInputBox.setText(apDescriptionDefaultValue)
                                apDescriptionView.setText(apDescriptionDefaultValue)
                            } else {
                                apDescriptionView.setText(appSettings.getString("AP_DESCRIPTION_VALUE"))
                                apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                            }
                        } else if (position == 2) {
                            apDescriptionVoiceRecView.visibility = View.GONE
                            apDescriptionCameraRecView.visibility = View.GONE
                            apDescriptionImageRecView.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.GONE
                            apDescriptionListBtn.visibility = View.VISIBLE
                            apDescriptionActiveListNameView.visibility = View.VISIBLE
                            apDescriptionViewWrapper.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.VISIBLE
                            apDescriptionView.setText(appSettings.getString("AP_DESCRIPTION_VALUE"))
                            apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                            val listOptions: String =
                                tableGenerator.getListValues(apDescriptionListId)
                            val listValues = listOptions.split(",")
                            val apDescriptionSpinnerAdapter = ArrayAdapter(
                                requireActivity(),
                                android.R.layout.simple_spinner_item,
                                listValues
                            )
                            apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                            apDescriptionListSpinner.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {

                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {

                                    }

                                }

                        } else if (position == 3) {
                            apDescriptionListBtn.visibility = View.GONE
                            apDescriptionActiveListNameView.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.GONE
                            apDescriptionCameraRecView.visibility = View.GONE
                            apDescriptionImageRecView.visibility = View.GONE
                            apDescriptionViewWrapper.visibility = View.VISIBLE
                            apDescriptionView.setText(appSettings.getString("AP_DESCRIPTION_VALUE"))
                            apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                            apDescriptionVoiceRecView.visibility = View.VISIBLE
                        } else if (position == 4) {
                            apDescriptionListBtn.visibility = View.GONE
                            apDescriptionActiveListNameView.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.GONE
                            apDescriptionVoiceRecView.visibility = View.GONE
                            apDescriptionImageRecView.visibility = View.GONE
                            apDescriptionViewWrapper.visibility = View.VISIBLE
                            apDescriptionView.setText(appSettings.getString("AP_DESCRIPTION_VALUE"))
                            apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                            apDescriptionCameraRecView.visibility = View.VISIBLE
                        } else if (position == 5) {
                            apDescriptionListBtn.visibility = View.GONE
                            apDescriptionActiveListNameView.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.GONE
                            apDescriptionVoiceRecView.visibility = View.GONE
                            apDescriptionCameraRecView.visibility = View.GONE
                            apDescriptionViewWrapper.visibility = View.VISIBLE
                            apDescriptionView.setText(appSettings.getString("AP_DESCRIPTION_VALUE"))
                            apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                            apDescriptionImageRecView.visibility = View.VISIBLE
                        } else {
                            apDescriptionVoiceRecView.visibility = View.GONE
                            apDescriptionCameraRecView.visibility = View.GONE
                            apDescriptionImageRecView.visibility = View.GONE
                            apDescriptionListBtn.visibility = View.GONE
                            apDescriptionActiveListNameView.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.GONE
                            apDescriptionViewWrapper.visibility = View.VISIBLE
                            apDescriptionView.setText(appSettings.getString("AP_DESCRIPTION_VALUE"))
                            apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }

            apDescriptionView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    appSettings.putString("AP_DESCRIPTION_VALUE", s.toString())
                }

            })

            val apQuantitySpinnerSelectedPosition =
                appSettings.getInt("AP_QUANTITY_SPINNER_SELECTED_POSITION")
            val apQuantityDefaultValue = appSettings.getString("AP_QUANTITY_DEFAULT_VALUE")
            val apQuantityListId = appSettings.getInt("AP_QUANTITY_LIST_ID")
            val apQuantityActiveListName = appSettings.getString("AP_QUANTITY_LIST_NAME")
            if (apQuantityActiveListName!!.isEmpty()) {
                apQuantityActiveListNameView.text = "Active List: None"
            } else {
                apQuantityActiveListNameView.text = "Active List: $apQuantityActiveListName"
            }
            apQuantitySpinner.setSelection(apQuantitySpinnerSelectedPosition)
            apQuantityListBtn.setOnClickListener {
                openListWithFieldsDialog("ap_quantity")
            }
            if (apQuantitySpinnerSelectedPosition == 1) {
                apQuantityListSpinner.visibility = View.GONE
                apQuantityListBtn.visibility = View.GONE
                apQuantityActiveListNameView.visibility = View.GONE
                apQuantityDefaultInputWrapper.visibility = View.VISIBLE
                apQuantityViewWrapper.visibility = View.VISIBLE
                apQuantityDefaultInputBox.setText(apQuantityDefaultValue)
                apQuantityView.setText(apQuantityDefaultValue)
            } else if (apQuantitySpinnerSelectedPosition == 2) {
                apQuantityDefaultInputWrapper.visibility = View.GONE
                apQuantityListBtn.visibility = View.VISIBLE
                apQuantityActiveListNameView.visibility = View.VISIBLE
                apQuantityViewWrapper.visibility = View.GONE
                apQuantityListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apQuantityListId)
                val listValues = listOptions.split(",")
                val apQuantitySpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                apQuantityListSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
            } else {
                apQuantityViewWrapper.visibility = View.VISIBLE
                apQuantityListBtn.visibility = View.GONE
                apQuantityActiveListNameView.visibility = View.GONE
                apQuantityDefaultInputWrapper.visibility = View.GONE
                apQuantityListSpinner.visibility = View.GONE
            }

            apQuantityDefaultInputBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    apQuantityView.setText(s.toString())
                    appSettings.putString("AP_QUANTITY_DEFAULT_VALUE", s.toString())
                }

            })

            apQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    appSettings.putInt("AP_QUANTITY_SPINNER_SELECTED_POSITION", position)
                    if (position == 1) {
                        apQuantityListSpinner.visibility = View.GONE
                        apQuantityListBtn.visibility = View.GONE
                        apQuantityDefaultInputWrapper.visibility = View.VISIBLE
                        apQuantityViewWrapper.visibility = View.VISIBLE
                        if (apQuantityDefaultValue!!.isNotEmpty()) {
                            apQuantityDefaultInputBox.setText(apQuantityDefaultValue)
                            apQuantityView.setText(apQuantityDefaultValue)
                        } else {
                            apQuantityView.setText(appSettings.getString("AP_QUANTITY_VALUE"))
                            apQuantityView.setSelection(apQuantityView.text.toString().length)
                        }
                    } else if (position == 2) {
                        apQuantityDefaultInputWrapper.visibility = View.GONE
                        apQuantityListBtn.visibility = View.VISIBLE
                        apQuantityViewWrapper.visibility = View.GONE
                        apQuantityView.setText(appSettings.getString("AP_QUANTITY_VALUE"))
                        apQuantityView.setSelection(apQuantityView.text.toString().length)
                        apQuantityListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apQuantityListId)
                        val listValues = listOptions.split(",")
                        val apQuantitySpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                        apQuantityListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }
                    } else {
                        apQuantityViewWrapper.visibility = View.VISIBLE
                        apQuantityView.setText(appSettings.getString("AP_QUANTITY_VALUE"))
                        apQuantityView.setSelection(apQuantityView.text.toString().length)
                        apQuantityListBtn.visibility = View.GONE
                        apQuantityDefaultInputWrapper.visibility = View.GONE
                        apQuantityListSpinner.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

            apQuantityView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    appSettings.putString("AP_QUANTITY_VALUE", s.toString())
                }

            })

            val apPriceSpinnerSelectedPosition =
                appSettings.getInt("AP_PRICE_SPINNER_SELECTED_POSITION")
            val apPriceDefaultValue = appSettings.getString("AP_PRICE_DEFAULT_VALUE")
            val apPriceListId = appSettings.getInt("AP_PRICE_LIST_ID")
            val apPriceActiveListName = appSettings.getString("AP_PRICE_LIST_NAME")
            if (apPriceActiveListName!!.isEmpty()) {
                apPriceActiveListNameView.text = "Active List: None"
            } else {
                apPriceActiveListNameView.text = "Active List: $apPriceActiveListName"
            }
            apPriceSpinner.setSelection(apPriceSpinnerSelectedPosition)
            apPriceListBtn.setOnClickListener {
                openListWithFieldsDialog("ap_price")
            }
            if (apPriceSpinnerSelectedPosition == 1) {
                apPriceListSpinner.visibility = View.GONE
                apPriceListBtn.visibility = View.GONE
                apPriceActiveListNameView.visibility = View.GONE
                apPriceDefaultInputWrapper.visibility = View.VISIBLE
                apPriceViewWrapper.visibility = View.VISIBLE
                apPriceDefaultInputBox.setText(apPriceDefaultValue)
                apPriceView.setText(apPriceDefaultValue)
            } else if (apPriceSpinnerSelectedPosition == 2) {
                apPriceDefaultInputWrapper.visibility = View.GONE
                apPriceListBtn.visibility = View.VISIBLE
                apPriceActiveListNameView.visibility = View.VISIBLE
                apPriceViewWrapper.visibility = View.GONE
                apPriceListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apPriceListId)
                val listValues = listOptions.split(",")
                val apPriceSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apPriceListSpinner.adapter = apPriceSpinnerAdapter

                apPriceListSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
            } else {
                apPriceViewWrapper.visibility = View.VISIBLE
                apPriceListBtn.visibility = View.GONE
                apPriceActiveListNameView.visibility = View.GONE
                apPriceDefaultInputWrapper.visibility = View.GONE
                apPriceListSpinner.visibility = View.GONE
            }

            apPriceDefaultInputBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    apPriceView.setText(s.toString())
                    appSettings.putString("AP_PRICE_DEFAULT_VALUE", s.toString())
                }

            })

            apPriceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    appSettings.putInt("AP_PRICE_SPINNER_SELECTED_POSITION", position)
                    if (position == 1) {
                        apPriceListSpinner.visibility = View.GONE
                        apPriceListBtn.visibility = View.GONE
                        apPriceDefaultInputWrapper.visibility = View.VISIBLE
                        apPriceViewWrapper.visibility = View.VISIBLE
                        if (apPriceDefaultValue!!.isNotEmpty()) {
                            apPriceDefaultInputBox.setText(apPriceDefaultValue)
                            apPriceView.setText(apPriceDefaultValue)
                        } else {
                            apPriceView.setText(appSettings.getString("AP_PRICE_VALUE"))
                            apPriceView.setSelection(apPriceView.text.toString().length)
                        }
                    } else if (position == 2) {
                        apPriceDefaultInputWrapper.visibility = View.GONE
                        apPriceListBtn.visibility = View.VISIBLE
                        apPriceViewWrapper.visibility = View.GONE
                        apPriceView.setText(appSettings.getString("AP_PRICE_VALUE"))
                        apPriceView.setSelection(apPriceView.text.toString().length)
                        apPriceListSpinner.visibility = View.VISIBLE
                        val listOptions: String = tableGenerator.getListValues(apPriceListId)
                        val listValues = listOptions.split(",")
                        val apPriceSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apPriceListSpinner.adapter = apPriceSpinnerAdapter

                        apPriceListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }
                    } else {
                        apPriceViewWrapper.visibility = View.VISIBLE
                        apPriceView.setText(appSettings.getString("AP_PRICE_VALUE"))
                        apPriceView.setSelection(apPriceView.text.toString().length)
                        apPriceListBtn.visibility = View.GONE
                        apPriceDefaultInputWrapper.visibility = View.GONE
                        apPriceListSpinner.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

            apPriceView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    appSettings.putString("AP_PRICE_VALUE", s.toString())
                }

            })


            selectedImageView =
                view.findViewById(R.id.selected_insales_add_product_image_view)
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
                searchedImagesList.clear()
                val internetSearchLayout = LayoutInflater.from(context)
                    .inflate(R.layout.internet_image_search_dialog_layout, null)
                loader =
                    internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
                searchBoxView =
                    internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
                searchBtnView =
                    internetSearchLayout.findViewById<ImageButton>(R.id.internet_image_search_btn)
                internetImageRecyclerView =
                    internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
                val closeBtn =
                    internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
                voiceSearchIcon =
                    internetSearchLayout.findViewById(R.id.voice_search_internet_images)
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
                    searchedImagesList as java.util.ArrayList<String>
                )
                internetImageRecyclerView.adapter = internetImageAdapter
                internetImageAdapter.setOnItemClickListener(object :
                    InternetImageAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val selectedImage = searchedImagesList[position]
                        FullImageFragment(selectedImage).show(
                            childFragmentManager,
                            "full-image-dialog"
                        )
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
                        imagesAdapter.notifyDataSetChanged()
                    }

                })

                voiceSearchIcon.setOnClickListener {
                    voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                    val voiceLayout = LayoutInflater.from(context).inflate(
                        R.layout.voice_language_setting_layout,
                        null
                    )
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
                                    if (parent!!.selectedItem.toString().toLowerCase(
                                            Locale.ENGLISH
                                        ).contains("english")
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
                        voiceResultLauncher1.launch(intent)
                    }
                }

                searchBoxView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                    override fun onEditorAction(
                        v: TextView?,
                        actionId: Int,
                        event: KeyEvent?
                    ): Boolean {
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList,
                            internetImageAdapter
                        )
                        return false
                    }

                })

                searchBtnView.setOnClickListener {
//                    var creditChargePrice: Float = 0F
//                    if (searchBoxView.text.toString().trim().isNotEmpty()) {
//
//
//                        val firebaseDatabase = FirebaseDatabase.getInstance().reference
//                        firebaseDatabase.child("SearchImagesLimit")
//                                .addListenerForSingleValueEvent(object :
//                                    ValueEventListener {
//                                    override fun onDataChange(snapshot: DataSnapshot) {
//                                        val creditPrice = snapshot.child("credits")
//                                            .getValue(Int::class.java) as Int
//                                        val images = snapshot.child("images")
//                                            .getValue(Int::class.java) as Int
//                                        creditChargePrice = creditPrice.toFloat() / images
//
//                                        userCurrentCredits =
//                                            appSettings.getString(Constants.userCreditsValue) as String
//
//                                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
//                                            BaseActivity.hideSoftKeyboard(
//                                                requireActivity(),
//                                                searchBtnView
//                                            )
//                                            //Constants.hideKeyboar(requireActivity())
//                                            val query = searchBoxView.text.toString().trim()
//                                            requireActivity().runOnUiThread {
//                                                loader.visibility = View.VISIBLE
//                                            }
//
//                                            BaseActivity.searchInternetImages(
//                                                requireActivity(),
//                                                query,
//                                                object : APICallback {
//                                                    override fun onSuccess(response: JSONObject) {
//                                                        if (loader.visibility == View.VISIBLE) {
//                                                            loader.visibility =
//                                                                View.INVISIBLE
//                                                        }
//
//                                                        val items =
//                                                            response.getJSONArray("items")
//                                                        if (items.length() > 0) {
//                                                            searchedImagesList.clear()
//                                                            for (i in 0 until items.length()) {
//                                                                val item =
//                                                                    items.getJSONObject(i)
//                                                                if (item.has("link")) {
//                                                                    searchedImagesList.add(
//                                                                        item.getString(
//                                                                            "link"
//                                                                        )
//                                                                    )
//                                                                }
//                                                            }
//                                                            internetImageAdapter.notifyItemRangeChanged(
//                                                                0,
//                                                                searchedImagesList.size
//                                                            )
//
//                                                        }
//                                                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//                                                        val hashMap = HashMap<String, Any>()
//                                                        val remaining =
//                                                            userCurrentCredits.toFloat() - creditChargePrice
//                                                        Log.d("TEST199", "$remaining")
//                                                        hashMap["credits"] =
//                                                            remaining.toString()
//                                                        firebaseDatabase.child(Constants.firebaseUserCredits)
//                                                            .child(Constants.firebaseUserId)
//                                                            .updateChildren(hashMap)
//                                                            .addOnSuccessListener {
//                                                                BaseActivity.getUserCredits(
//                                                                    requireActivity()
//                                                                )
//                                                            }
//                                                            .addOnFailureListener {
//
//                                                            }
//                                                    }
//
//                                                    override fun onError(error: VolleyError) {
//                                                        if (loader.visibility == View.VISIBLE) {
//                                                            loader.visibility =
//                                                                View.INVISIBLE
//                                                        }
//
//                                                        BaseActivity.showAlert(
//                                                            requireActivity(),
//                                                            error.localizedMessage!!
//                                                        )
//                                                    }
//
//                                                })
//                                        } else {
//                                            MaterialAlertDialogBuilder(requireActivity())
//                                                .setMessage(getString(R.string.low_credites_error_message))
//                                                .setCancelable(false)
//                                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
//                                                    dialog.dismiss()
//                                                }
//                                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
//                                                    dialog.dismiss()
//                                                    startActivity(
//                                                        Intent(
//                                                            requireActivity(),
//                                                            UserScreenActivity::class.java
//                                                        )
//                                                    )
//                                                }
//                                                .create().show()
//                                        }
//                                    }
//
//                                    override fun onCancelled(error: DatabaseError) {
//
//                                    }
//
//                                })
//
//
//                    } else {
//                        if (loader.visibility == View.VISIBLE) {
//                            loader.visibility = View.INVISIBLE
//                        }
//
//                        BaseActivity.showAlert(
//                            requireActivity(),
//                            getString(R.string.empty_text_error)
//                        )
//                    }
                    startSearch(
                        searchBoxView,
                        searchBtnView,
                        loader,
                        searchedImagesList,
                        internetImageAdapter
                    )
                }

            }

            apAddDescriptionView.setOnClickListener {
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

                if (userCurrentCredits.toFloat() >= 1.0) {

                    launchActivity.launch(
                        Intent(
                            requireActivity(),
                            RainForestApiActivity::class.java
                        )
                    )
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
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
            }

            val cateSpinnerAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                originalCategoriesList
            )
            cateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoriesSpinner.adapter = cateSpinnerAdapter

            categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            getCategories(cateSpinnerAdapter)

            apTestDataView.setOnClickListener {
                apTitleView.setText(requireActivity().resources.getString(R.string.test_text))
                apDescriptionView.setText(requireActivity().resources.getString(R.string.test_text))
                apPriceView.setText("1")
                apQuantityView.setText("1")
            }

            apCancelBtn.setOnClickListener {
                dismiss()
            }

            apSubmitBtn.setOnClickListener {

                if (addProductValidation(
                        categoriesSpinner,
                        apTitleView,
                        apTitleSpinnerSelectedPosition,
                        apQuantityView,
                        apQuantitySpinnerSelectedPosition,
                        apPriceView,
                        apPriceSpinnerSelectedPosition
                    )
                ) {
                    BaseActivity.startLoading(requireActivity())
                    finalTitleText = if (apTitleSpinnerSelectedPosition == 1) {
                        apTitleDefaultInputBox.text.toString().toString()
                    } else if (apTitleSpinnerSelectedPosition == 2) {
                        apTitleListSpinner.selectedItem.toString().trim()
                    } else {
                        apTitleView.text.toString().trim()
                    }

                    finalDescriptionText = if (apDescriptionSpinnerSelectedPosition == 1) {
                        apDescriptionDefaultInputBox.text.toString().toString()
                    } else if (apDescriptionSpinnerSelectedPosition == 2) {
                        apDescriptionListSpinner.selectedItem.toString().trim()
                    } else {
                        apDescriptionView.text.toString().trim()
                    }

                    finalQuantityText = if (apQuantitySpinnerSelectedPosition == 1) {
                        apQuantityDefaultInputBox.text.toString().toString()
                    } else if (apQuantitySpinnerSelectedPosition == 2) {
                        apQuantityListSpinner.selectedItem.toString().trim()
                    } else {
                        apQuantityView.text.toString().trim()
                    }

                    finalPriceText = if (apPriceSpinnerSelectedPosition == 1) {
                        apPriceDefaultInputBox.text.toString().toString()
                    } else if (apPriceSpinnerSelectedPosition == 2) {
                        apPriceListSpinner.selectedItem.toString().trim()
                    } else {
                        apPriceView.text.toString().trim()
                    }


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
                        ""
                    )
                    viewModel.getAddProductResponse()
                        .observe(requireActivity(), Observer { response ->
                            if (response != null) {
                                if (response.get("status").asString == "200") {
                                    val details = response.getAsJsonObject("details")
                                    val productId = details.get("id").asInt

                                    if (multiImagesList.isNotEmpty()) {
                                        BaseActivity.dismiss()
                                        BaseActivity.startLoading(requireActivity())
                                        uploadImages(
                                            productId,
                                            multiImagesList,
                                            object : ResponseListener {
                                                override fun onSuccess(result: String) {
                                                    if (result.contains("success")) {
                                                        Handler(Looper.myLooper()!!).postDelayed(
                                                            {
                                                                BaseActivity.dismiss()
                                                                dismiss()
                                                                listener.onSuccess("")
                                                            },
                                                            2000
                                                        )
                                                    }
                                                }

                                            })
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
//                                                                    dismiss()
//                                                                    listener.onSuccess("")
//                                                                },
//                                                                6000
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
                                            BaseActivity.dismiss()
                                            dismiss()
                                            listener.onSuccess("")
                                        }, 3000)
                                    }

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
                            if (response.get("status").asString == "200") {
                                selectedImageBase64String = ""
                                selectedInternetImage = ""

                                if (index == listImages.size - 1) {
                                    index = 0
                                    responseListener.onSuccess("success")
                                } else {
                                    index++
                                    uploadImages(productId, listImages, responseListener)
                                }
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
                    val text = result.data!!.getStringExtra("SCAN_TEXT")
                    if (Constants.CIVType == "ap_title") {
                        val currentPItemTitle = apTitleView.text.toString().trim()
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemTitle)
                        stringBuilder.append(text)
                        apTitleView.setText(stringBuilder.toString())
                    } else {
                        val currentPItemTitle = apDescriptionView.text.toString().trim()
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemTitle)
                        stringBuilder.append(text)
                        apDescriptionView.setText(stringBuilder.toString())
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
                                results!!.get(0)
                            }
                    if (Constants.CIVType == "ap_title") {
                        val currentPItemTitle = apTitleView.text.toString().trim()
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemTitle)
                        stringBuilder.append(spokenText)
                        apTitleView.setText(stringBuilder.toString())
                    } else {
                        val currentPItemTitle = apDescriptionView.text.toString().trim()
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemTitle)
                        stringBuilder.append(spokenText)
                        apDescriptionView.setText(stringBuilder.toString())
                    }
                }
            }

        fun pickImageFromCamera() {
            val takePictureIntent = Intent(context, OcrActivity::class.java)
            cameraResultLauncher1.launch(takePictureIntent)
        }

        private fun cropImage(imageUri: Uri) {

            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(requireActivity())
        }

        private var voiceResultLauncher1 =
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
                        searchedImagesList as java.util.ArrayList<String>, internetImageAdapter
                    )
                }
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
                                searchedImagesList as java.util.ArrayList<String>,
                                internetImageAdapter
                            )
                        }
                    }


                }
            }

        private fun startSearch(
            searchBoxView: TextInputEditText,
            searchBtnView: ImageButton,
            loader: ProgressBar,
            searchedImagesList: java.util.ArrayList<String>,
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

                BaseActivity.showAlert(
                    requireActivity(),
                    getString(R.string.empty_text_error)
                )
            }
        }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

            if (!quickModeCheckBox.isChecked && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                val imgUri = result.uri
                try {
                    TextRecogniser.runTextRecognition(
                        requireActivity(), if (Constants.CIVType == "ap_title") {
                            apTitleView
                        } else {
                            apDescriptionView
                        }, imgUri
                    )
                    Constants.hint = "default"
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
                for (fragment in childFragmentManager.fragments) {
                    fragment.onActivityResult(requestCode, resultCode, data)
                }
            }

            //super.onActivityResult(requestCode, resultCode, data)
        }

        private lateinit var tableGenerator: TableGenerator
        private lateinit var adapter: FieldListsAdapter
        private var listId: Int? = null
        private fun openListWithFieldsDialog(fieldType: String) {

            val listItems = mutableListOf<ListItem>()
            val layout =
                LayoutInflater.from(context).inflate(
                    R.layout.list_with_fields_value_layout,
                    null
                )
            val listWithFieldsValueRecyclerView =
                layout.findViewById<RecyclerView>(R.id.list_with_fields_recycler_view)
            listWithFieldsValueRecyclerView.layoutManager = LinearLayoutManager(context)
            listWithFieldsValueRecyclerView.hasFixedSize()
            adapter = FieldListsAdapter(requireActivity(), listItems as ArrayList<ListItem>)
            listWithFieldsValueRecyclerView.adapter = adapter
            val closeDialogBtn = layout.findViewById<AppCompatImageView>(R.id.lwfv_dialog_close_btn)

            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setView(layout)
            builder.setCancelable(false)
            val alert = builder.create()
            alert.show()
            closeDialogBtn.setOnClickListener {
                alert.dismiss()
            }
            val tempList = tableGenerator.getList()
            if (tempList.isNotEmpty()) {
                listItems.clear()
                listItems.addAll(tempList)
                adapter.notifyDataSetChanged()
            } else {
                adapter.notifyDataSetChanged()
            }


            adapter.setOnItemClickListener(object : FieldListsAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val listValue = listItems[position]
                    listId = listValue.id
                    val list = tableGenerator.getListValues(listId!!)
                    if (list.isNotEmpty()) {
                        //selectedListTextView.text = listValue.value
                        if (fieldType == "ap_title") {
                            appSettings.putInt("AP_TITLE_LIST_ID", listId!!)
                            appSettings.putString("AP_TITLE_LIST_NAME", listValue.value)
                            apTitleActiveListNameView.text = "Active List: ${listValue.value}"
                        } else if (fieldType == "ap_description") {
                            appSettings.putInt("AP_DESCRIPTION_LIST_ID", listId!!)
                            appSettings.putString("AP_DESCRIPTION_LIST_NAME", listValue.value)
                            apDescriptionActiveListNameView.text = "Active List: ${listValue.value}"
                        } else if (fieldType == "ap_quantity") {
                            appSettings.putInt("AP_QUANTITY_LIST_ID", listId!!)
                            appSettings.putString("AP_QUANTITY_LIST_NAME", listValue.value)
                            apQuantityActiveListNameView.text = "Active Lis: ${listValue.value}"
                        } else if (fieldType == "ap_price") {
                            appSettings.putInt("AP_PRICE_LIST_ID", listId!!)
                            appSettings.putString("AP_PRICE_LIST_NAME", listValue.value)
                            apPriceActiveListNameView.text = "Active List: ${listValue.value}"
                        }
                        alert.dismiss()
                    } else {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(getString(R.string.field_list_value_empty_error_text))
                            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(getString(R.string.add_text)) { dialog, which ->
                                dialog.dismiss()
                                addTableDialog(listId!!)
                            }
                            .create().show()
                    }

                }

                override fun onAddItemClick(position: Int) {
                    alert.dismiss()
                    val intent = Intent(context, FieldListsActivity::class.java)
//                    intent.putExtra("TABLE_NAME", tableName)
//                    intent.putExtra("FLAG", "yes")
                    requireActivity().startActivity(intent)
                }
            })
        }

        private fun addTableDialog(id: Int) {
            val listValueLayout = LayoutInflater.from(context).inflate(
                R.layout.add_list_value_layout,
                null
            )
            val heading = listValueLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
            heading.text = getString(R.string.list_value_hint_text)
            val listValueInputBox =
                listValueLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
            val listValueAddBtn =
                listValueLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setView(listValueLayout)
            val alert = builder.create()
            alert.show()
            listValueAddBtn.setOnClickListener {
                if (listValueInputBox.text.toString().isNotEmpty()) {
                    val value = listValueInputBox.text.toString().trim()
                    tableGenerator.insertListValue(id, value)
                    alert.dismiss()
                } else {
                    BaseActivity.showAlert(
                        requireActivity(),
                        getString(R.string.add_list_value_error_text)
                    )
                }
            }
        }

        var launchActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data

                    if (data != null && data.hasExtra("TITLE")) {
                        val title = data.getStringExtra("TITLE") as String
                        if (title.isNotEmpty()) {
                            val currentPItemTitle = apTitleView.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemTitle)
                            stringBuilder.append(title)
                            apTitleView.setText(stringBuilder.toString())
                        }
                    }

                    if (data != null && data.hasExtra("DESCRIPTION")) {
                        val description = data.getStringExtra("DESCRIPTION") as String
                        if (description.isNotEmpty()) {

                            val currentPItemDescription = apDescriptionView.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemDescription)
                            stringBuilder.append(description)
                            apDescriptionView.setText(stringBuilder.toString())

                        }
                    }
                    if (apDescriptionView.text.toString().isNotEmpty()) {
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        apDescriptionView.requestFocus()
                        Constants.openKeyboar(requireActivity())
                    }
                }
            }

        var launchActivity1 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data

                    if (data != null && data.hasExtra("TITLE")) {
                        val title = data.getStringExtra("TITLE") as String
                        if (title.isNotEmpty()) {
//                            val currentPItemTitle = apTitleView.text.toString().trim()
//                            val stringBuilder = java.lang.StringBuilder()
//                            stringBuilder.append(currentPItemTitle)
//                            stringBuilder.append(title)
//                            apTitleView.setText(stringBuilder.toString())
                            appSettings.putString("AP_PRODUCT_TITLE", title)
                        }
                    }

                    if (data != null && data.hasExtra("DESCRIPTION")) {
                        val description = data.getStringExtra("DESCRIPTION") as String
                        if (description.isNotEmpty()) {
                            appSettings.putString("AP_PRODUCT_DESCRIPTION", description)
//                            val currentPItemDescription = apDescriptionView.text.toString().trim()
//                            val stringBuilder = java.lang.StringBuilder()
//                            stringBuilder.append(currentPItemDescription)
//                            stringBuilder.append(description)
//                            apDescriptionView.setText(stringBuilder.toString())

                        }
                    }
//                    if (apDescriptionView.text.toString().isNotEmpty()) {
//                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
//                        apDescriptionView.requestFocus()
//                        Constants.openKeyboar(requireActivity())
//                    }
                }
            }


        private fun addProductValidation(
            categoriesSpinner: AppCompatSpinner?,
            apTitleView: TextInputEditText?,
            apTitleSelectedPosition: Int,
            apQuantityView: TextInputEditText?,
            apQuantitySelectedPosition: Int,
            apPriceView: TextInputEditText?,
            apPriceSelectedPosition: Int
        ): Boolean {
            if (selectedCategoryId == 0) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.add_product_cate_error)
                )
                return false
            } else if (apTitleSelectedPosition == 1 && apTitleView!!.text.toString().isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.empty_text_error)
                )
                return false
            } else if (apQuantitySelectedPosition == 1 && apQuantityView!!.text.toString()
                    .isEmpty()
            ) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.empty_text_error)
                )
                return false
            } else if (apPriceSelectedPosition == 1 && apPriceView!!.text.toString().isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.empty_text_error)
                )
                return false
            }
            return true
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
//                        selectedImageBase64String =
//                            ImageManager.convertImageToBase64(
//                                requireActivity(),
//                                currentPhotoPath!!
//                            )
//                        Log.d("TEST199DIALOG", selectedImageBase64String)
                        Glide.with(requireActivity())
                            .load(currentPhotoPath)
                            .placeholder(R.drawable.placeholder)
                            .centerInside()
                            .into(selectedImageView)
                        barcodeImageList.add(currentPhotoPath!!)
                        multiImagesList.add(currentPhotoPath!!)
                        imagesAdapter.notifyDataSetChanged()
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
//                    selectedImageBase64String =
//                        ImageManager.convertImageToBase64(
//                            requireActivity(),
//                            currentPhotoPath!!
//                        )
//                    Log.d("TEST199DIALOG", selectedImageBase64String)
                    Glide.with(requireActivity())
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(selectedImageView)
                    barcodeImageList.add(currentPhotoPath!!)
                    multiImagesList.add(currentPhotoPath!!)
                    imagesAdapter.notifyDataSetChanged()
                }
            }

        private fun createImageFile(bitmap: Bitmap) {
            currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
        }

        private fun getCategories(adapter: ArrayAdapter<Category>) {

//            BaseActivity.startLoading(requireActivity())
            viewModel.callCategories(requireActivity(), shopName, email, password)
            viewModel.getCategoriesResponse().observe(this, Observer { response ->
                if (response != null) {
//                    BaseActivity.dismiss()
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
                            adapter.notifyDataSetChanged()
                            if (originalCategoriesList.size > 0) {
                                selectedCategoryId = originalCategoriesList[0].id
                                //categoriesSpinner.setSelection(0)
                            }
                        }
                    } else {
//                        BaseActivity.dismiss()
                    }
                } else {
//                    BaseActivity.dismiss()
                }
            })
        }

        internal class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(
            manager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            private val mFragmentList: MutableList<Fragment> = ArrayList()
            private val mFragmentTitleList: MutableList<String> = ArrayList()
            override fun getItem(position: Int): Fragment {
                return mFragmentList[position]
            }

            override fun getCount(): Int {
                return mFragmentList.size
            }

            fun addFragment(fragment: Fragment, title: String) {
                mFragmentList.add(fragment)
                mFragmentTitleList.add(title)
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mFragmentTitleList[position]
            }
        }


    }

}