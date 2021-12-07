package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.HelpsVideoAdapter
import com.boris.expert.csvmagic.model.HelpObject
import com.boris.expert.csvmagic.viewmodel.HelpActivityViewModel
import com.boris.expert.csvmagic.viewmodelfactory.ViewModelFactory
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class HelpActivity : BaseActivity(),HelpsVideoAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var currentAppLocale: String = ""
    private var helpsVideoList = mutableListOf<HelpObject>()
    private lateinit var helpsVideoListRecyclerview: RecyclerView
    private lateinit var adapter: HelpsVideoAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var viewModel:HelpActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        initViews()
        setUpToolbar()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        helpsVideoListRecyclerview = findViewById(R.id.help_center_list_recyclerview)
        databaseReference = FirebaseDatabase.getInstance().reference
        currentAppLocale = Locale.getDefault().language
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(HelpActivityViewModel()).createFor()
        )[HelpActivityViewModel::class.java]
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.help_center_text)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))

        helpsVideoListRecyclerview.layoutManager = LinearLayoutManager(context)
        helpsVideoListRecyclerview.hasFixedSize()
        adapter = HelpsVideoAdapter(context, helpsVideoList as ArrayList<HelpObject>,lifecycle)
        helpsVideoListRecyclerview.adapter = adapter
        adapter.setOnItemClickListener(this)

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


    override fun onResume() {
        super.onResume()
        getHelpVideoList()
    }

    private fun getHelpVideoList() {
        val videoLangRef = if (currentAppLocale == "en") {
            "engvideos"
        } else {
            "ruvideos"
        }
         startLoading(context)
        viewModel.callHelpVideosList(context,videoLangRef)
        viewModel.getHelpVideosList().observe(this, { list ->
            dismiss()
            if (list != null) {
                if (helpsVideoList.size > 0) {
                    helpsVideoList.clear()
                }
                helpsVideoList.addAll(list)
                adapter.notifyItemRangeChanged(0,helpsVideoList.size)
            }
        })

    }


    override fun onItemClick(position: Int) {

    }

    override fun onItemFullScreenView(position: Int) {
        val video = helpsVideoList[position]
        startActivity(Intent(context,VideoFullScreenActivity::class.java).apply {
            putExtra("VIDEO_ID",video.link)
        })
    }
}