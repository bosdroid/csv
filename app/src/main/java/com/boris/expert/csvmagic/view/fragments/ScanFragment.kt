package com.boris.expert.csvmagic.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.TablesDataAdapter
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.FileUtil
import com.boris.expert.csvmagic.utils.TableGenerator
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.boris.expert.csvmagic.view.activities.MainActivity
import com.boris.expert.csvmagic.view.activities.TableViewActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.opencsv.CSVReader
import java.io.FileReader
import java.io.IOException


class ScanFragment : Fragment(), TablesDataAdapter.OnItemClickListener {

//    private lateinit var qrCodeHistoryRecyclerView: RecyclerView
//    private lateinit var emptyView: MaterialTextView
//    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
//    private lateinit var adapter: QrCodeHistoryAdapter
//    private lateinit var appViewModel: AppViewModel
private lateinit var tableDataRecyclerView: RecyclerView
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesDataAdapter
    private lateinit var fabUploadFile:FloatingActionButton

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
        displayTableList()
        return v
    }

    private fun initViews(view:View){
        tableGenerator = TableGenerator(requireActivity())
        tableDataRecyclerView = view.findViewById(R.id.tables_data_recyclerview)
        fabUploadFile = view.findViewById(R.id.fab_upload_file)
        tableDataRecyclerView.layoutManager = LinearLayoutManager(context)
        tableDataRecyclerView.hasFixedSize()
        adapter = TablesDataAdapter(requireActivity(), tableList as ArrayList<String>)
        tableDataRecyclerView.adapter = adapter


        fabUploadFile.setOnClickListener {
            if (Constants.userData != null){
                importCsv()
            }
            else{
                //showAlert(context,"You can not create dynamic table without account login!")
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.alert_text))
                    .setMessage(getString(R.string.login_error_text))
                    .setNegativeButton(getString(R.string.later_text)){dialog,which->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.login_text)){dialog,which->
                        dialog.dismiss()
//                        val intent = Intent(context, MainActivity::class.java)
//                        intent.putExtra("REQUEST","login")
//                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                        startActivity(intent)
//                        finish()
                    }
                    .create().show()
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

    private fun importCsv() {
        openFilePicker()
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
                    val ext = file.name.substring(file.name.lastIndexOf(".")+1)
                    val fileName = file.name.substring(0,file.name.lastIndexOf("."))
                    if (ext != "csv") {
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.csv_file_chooser_error_message_text)
                        )
                    } else {
                        try {

                            var nextLine: Array<String>
                            var counter = 0
                            val columnsList = mutableListOf<String>()
                            var tableData = mutableListOf<Pair<String, String>>()
                            val listRecord = mutableListOf<List<Pair<String,String>>>()
                            val tableName =
                                fileName.replace(" ", "_").replace("[-+.^:,]", "").trim()
                            BaseActivity.startLoading(requireActivity())
                            val reader = CSVReader(FileReader(file))
                            while (reader.readNext().also { nextLine = it } != null) {
                                // nextLine[] is an array of values from the line
                                if (counter == 0) {
                                    for (i in nextLine.indices) {
                                        columnsList.add(nextLine[i].replace(" ","_").trim())
                                    }
                                    counter += 1
                                    continue
                                }
                                if (nextLine.isNotEmpty() && columnsList.size == nextLine.size) {
                                    for (i in nextLine.indices) {
                                        tableData.add(Pair(columnsList[i], nextLine[i]))
                                    }
                                    listRecord.add(tableData)
                                    tableData = mutableListOf()
                                    counter += 1
                                } else {
                                    break
                                }
                                if (reader.readNext() == null) {
                                    break
                                }
                            }

                            if (tableName.isNotEmpty() && listRecord.isNotEmpty()) {
                                val isFound = tableGenerator.tableExists(tableName)
                                if (isFound) {
                                    BaseActivity.dismiss()
                                    BaseActivity.showAlert(
                                        requireActivity(),
                                        getString(R.string.table_already_exist_message)
                                    )
                                } else {

                                    tableGenerator.createTable(
                                        tableName,
                                        columnsList as ArrayList<String>
                                    )

                                    Handler(Looper.myLooper()!!).postDelayed({

                                        val isExist = tableGenerator.tableExists(tableName)
                                        if (isExist) {
                                            displayTableList()
                                            for (j in 0 until listRecord.size){
                                                tableGenerator.insertData(tableName, listRecord[j])
                                            }
                                            BaseActivity.dismiss()
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
                                getString(R.string.table_csv_import_error_message)
                            )
                            e.printStackTrace()
                        }
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
        adapter.notifyDataSetChanged()
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
        intent.putExtra("TABLE_NAME",table)
        requireActivity().startActivity(intent)
    }

}