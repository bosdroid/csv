package com.boris.expert.csvmagic.view.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.BarcodeImageAdapter
import com.boris.expert.csvmagic.utils.TableGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BarcodeImageListActivity : BaseActivity(), BarcodeImageAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private lateinit var barcodeImageRecyclerView: RecyclerView
    private var imageList = mutableListOf<String>()
    private var tableName: String = ""
    private var images: String = ""
    private var recordId: Int = 0
    private lateinit var adapter: BarcodeImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_image_list)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        barcodeImageRecyclerView = findViewById(R.id.barcode_images_recyclerview)

        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }

        if (intent != null && intent.hasExtra("ID")) {
            recordId = intent.getIntExtra("ID", 0)
        }

        images = tableGenerator.getBarcodeImages(tableName, recordId)

        if (images.contains(",")) {
            imageList.addAll(images.split(",").toList())
        } else {
            imageList.add(images)
        }

        barcodeImageRecyclerView.layoutManager = LinearLayoutManager(context)
        barcodeImageRecyclerView.hasFixedSize()
        adapter = BarcodeImageAdapter(context, imageList as ArrayList<String>)
        barcodeImageRecyclerView.adapter = adapter
        adapter.notifyItemRangeChanged(0, imageList.size)
        adapter.setOnItemClickListener(this)

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.barcode_image_list)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onItemDeleteClick(position: Int) {
        val image = imageList[position]
        val builder = MaterialAlertDialogBuilder(context)
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
                }, recordId
            )
            adapter.notifyItemRemoved(position)
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onAddItemEditClick(position: Int) {
        val image = imageList[position]
    }

    override fun onImageClick(position: Int) {

    }
}