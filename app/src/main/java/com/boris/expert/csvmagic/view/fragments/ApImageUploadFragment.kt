package com.boris.expert.csvmagic.view.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
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
import com.boris.expert.csvmagic.adapters.InternetImageAdapter
import com.boris.expert.csvmagic.interfaces.APICallback
import com.boris.expert.csvmagic.utils.AppSettings
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.ImageManager
import com.boris.expert.csvmagic.utils.RuntimePermissionHelper
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
                searchedImagesList as ArrayList<String>
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

                                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

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


        apSubmitBtn.setOnClickListener {

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
                        finalPriceText
                    )
                    viewModel.getAddProductResponse()
                        .observe(requireActivity(), Observer { response ->
                            if (response != null) {
                                if (response.get("status").asString == "200") {
                                    val details = response.getAsJsonObject("details")
                                    val productId = details.get("id").asInt

                                    if (selectedImageBase64String.isNotEmpty()) {
                                        BaseActivity.dismiss()
                                        BaseActivity.startLoading(requireActivity())

                                        viewModel.callAddProductImage(
                                            requireActivity(),
                                            shopName,
                                            email,
                                            password,
                                            selectedImageBase64String,
                                            productId,
                                            "${System.currentTimeMillis()}.jpg",
                                            if (intentType != 3) {
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
                                                            Handler(Looper.myLooper()!!).postDelayed(
                                                                {
                                                                    BaseActivity.dismiss()
                                                                    creditCharged()
                                                                    val intent = Intent("dialog-dismiss")
                                                                    LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
                                                                },
                                                                2000
                                                            )
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
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            BaseActivity.dismiss()
                                            creditCharged()
                                            val intent = Intent("dialog-dismiss")
                                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)

                                        }, 2000)
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

    }

    private fun creditCharged(){
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
                selectedImageBase64String =
                    ImageManager.convertImageToBase64(
                        requireActivity(),
                        currentPhotoPath!!
                    )
                Log.d("TEST199DIALOG", selectedImageBase64String)
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
                        ImageManager.convertImageToBase64(
                            requireActivity(),
                            currentPhotoPath!!
                        )
                    Log.d("TEST199", selectedImageBase64String)
                    Glide.with(requireActivity())
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(selectedImageView)
                }

            }
        }

}