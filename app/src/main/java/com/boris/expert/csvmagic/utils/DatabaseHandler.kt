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

    fun exporter(context: Context, listener: BackupListener){
        BaseActivity.startLoading(context)
        val tableGenerator = TableGenerator(context)
        Log.d("TEST1999", tableGenerator.getDbPath())

        try {
            //Existing DB Path
            val DB_PATH = tableGenerator.getDbPath()
            val folder = File(context.filesDir.toString() + File.separator + Constants.firebaseUserId)

            if (!folder.exists()) {
                folder.mkdir()
            }

                val COPY_DB = "${folder.absolutePath}/${Constants.firebaseUserId}_backup.db"
                val COPY_DB_PATH = File(COPY_DB)

                val srcChannel = FileInputStream(DB_PATH).channel

                val dstChannel = FileOutputStream(COPY_DB_PATH).channel
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size())
                srcChannel.close()
                dstChannel.close()
                tableGenerator.deleteDatabase()
                BaseActivity.dismiss()
                listener.onSuccess()

        } catch (excep: Exception) {
            BaseActivity.dismiss()
            Toast.makeText(context, "ERROR IN COPY $excep", Toast.LENGTH_LONG).show()
            Log.e("FILECOPYERROR>>>>", excep.toString())
            excep.printStackTrace()
        }

    }

    fun importer(context: Context){
        val appSettings = AppSettings(context)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            val userId = auth.currentUser!!.uid

        val alreadyImported = appSettings.getString(Constants.dbImport)
        if (alreadyImported!= null && alreadyImported.isEmpty()){

            BaseActivity.startLoading(context)
            val tableGenerator = TableGenerator(context)
            Log.d("TEST1999", tableGenerator.getDbPath())

            try {
                //Existing DB Path
                val DB_PATH = tableGenerator.getDbPath()
                val folder = File(context.filesDir.toString() + File.separator + userId)
                if (folder.exists()) {


                    val COPY_DB = "${folder.absolutePath}/${userId}_backup.db"
                    val COPY_DB_PATH = File(COPY_DB)
                     if (COPY_DB_PATH.exists()) {
                         tableGenerator.mergeDatabases("${userId}_backup",COPY_DB_PATH.absolutePath)
                         appSettings.putString(Constants.dbImport, "yes")
                     }
                    BaseActivity.dismiss()
                }
                else{
                    BaseActivity.dismiss()
                }
            } catch (excep: Exception) {
                BaseActivity.dismiss()
                BaseActivity.showAlert(context,"ERROR IN COPY $excep")
                Log.e("FILECOPYERROR>>>>", excep.toString())
                excep.printStackTrace()

            }
        }

        }
    }

}