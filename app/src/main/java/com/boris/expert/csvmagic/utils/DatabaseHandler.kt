package com.boris.expert.csvmagic.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.boris.expert.csvmagic.interfaces.BackupListener
import com.boris.expert.csvmagic.view.activities.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

object DatabaseHandler {

    fun exporter(context: Context, listener: BackupListener, type: String) {
        val auth = FirebaseAuth.getInstance()
        var userId = if (auth.currentUser != null) {
            auth.currentUser!!.uid
        } else {
            "123456"
        }
        if (type == "logout"){
            userId = "123456"
        }
        val appSettings = AppSettings(context)
        val alreadyExported = appSettings.getString(Constants.dbExport)
        if (alreadyExported != null && alreadyExported.isEmpty()) {
            BaseActivity.startLoading(context)
            val tableGenerator = TableGenerator(context)
            Log.d("TEST1999", tableGenerator.getDbPath())

            try {
                //Existing DB Path
                val DB_PATH = tableGenerator.getDbPath()
                val folder =
                    File(context.filesDir.toString() + File.separator + userId)

                if (!folder.exists()) {
                    folder.mkdir()
                }

                val COPY_DB = if (type == "login") {
                    "${folder.absolutePath}/${userId}_backup.db"
                } else {
                    "${folder.absolutePath}/${userId}_backup_logout.db"
                }
                val COPY_DB_PATH = File(COPY_DB)

                val srcChannel = FileInputStream(DB_PATH).channel

                val dstChannel = FileOutputStream(COPY_DB_PATH).channel
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size())
                srcChannel.close()
                dstChannel.close()
                tableGenerator.deleteDatabase()
                BaseActivity.dismiss()
                appSettings.putString(Constants.dbExport, "yes")
                listener.onSuccess()

            } catch (excep: Exception) {
                BaseActivity.dismiss()
                Toast.makeText(context, "ERROR IN COPY $excep", Toast.LENGTH_LONG).show()
                Log.e("FILECOPYERROR>>>>", excep.toString())
                excep.printStackTrace()
            }
        }


    }

    fun importer(context: Context, type: String) {
        val appSettings = AppSettings(context)
        val auth = FirebaseAuth.getInstance()
        var userId = if (auth.currentUser != null) {
            auth.currentUser!!.uid
        } else {
            "123456"
        }
        if (type == "logout"){
            userId = "123456"
        }
//        val alreadyImported = appSettings.getString(Constants.dbImport)
//        if (alreadyImported != null && alreadyImported.isEmpty()) {

            BaseActivity.startLoading(context)
            val tableGenerator = TableGenerator(context)
            Log.d("TEST1999", tableGenerator.getDbPath())
            Thread.sleep(1000)
            try {
                //Existing DB Path
                val DB_PATH = tableGenerator.getDbPath()
                val folder = File(context.filesDir.toString() + File.separator + userId)
                if (folder.exists()) {

                    val COPY_DB = if (type == "login") {
                        "${folder.absolutePath}/${userId}_backup.db"
                    } else {
                        "${folder.absolutePath}/${userId}_backup_logout.db"
                    }
                    val COPY_DB_PATH = File(COPY_DB)
                    if (COPY_DB_PATH.exists()) {
                        val temp = if (type == "login") {
                            "backup"
                        } else {
                            "backup_logout"
                        }
                        tableGenerator.mergeDatabases(
                            "${userId}_$temp",
                            COPY_DB_PATH.absolutePath
                        )
                        appSettings.putString(Constants.dbImport, "yes")
                    }
                    BaseActivity.dismiss()
                } else {
                    BaseActivity.dismiss()
                }
            } catch (excep: Exception) {
                BaseActivity.dismiss()
                BaseActivity.showAlert(context, "ERROR IN COPY $excep")
                Log.e("FILECOPYERROR>>>>", excep.toString())
                excep.printStackTrace()

            }
//        }

    }

}