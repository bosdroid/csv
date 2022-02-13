package com.boris.expert.csvmagic.view.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.ChatAdapter
import com.boris.expert.csvmagic.interfaces.UploadImageCallback
import com.boris.expert.csvmagic.model.Message
import com.boris.expert.csvmagic.model.SupportTicket
import com.boris.expert.csvmagic.utils.Constants
import com.boris.expert.csvmagic.utils.FileUtil
import com.boris.expert.csvmagic.utils.WrapContentLinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SupportChatActivity : BaseActivity(), View.OnClickListener {


    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var supportTicket: SupportTicket? = null
    private lateinit var chatMessageRecyclerView: RecyclerView
    private lateinit var chatBoxTextInputField: TextInputEditText
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var imageUploadBtn: ImageButton
    private var chatList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private var name = ""
    private var userId = ""
    private var messageType = "text"
    private var selectedImageServerUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_chat)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        chatMessageRecyclerView = findViewById(R.id.support_chat_recyclerview)
        chatBoxTextInputField = findViewById(R.id.chat_box_input_field)
        sendMessageBtn = findViewById(R.id.chat_send_btn)
        sendMessageBtn.setOnClickListener(this)
        imageUploadBtn = findViewById(R.id.image_send_btn)
        imageUploadBtn.setOnClickListener(this)
        if (intent != null && intent.hasExtra("S_TICKET")) {
            supportTicket = intent.getSerializableExtra("S_TICKET") as SupportTicket
        }

        val wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(context,RecyclerView.VERTICAL,false)
        wrapContentLinearLayoutManager.stackFromEnd = true
        chatMessageRecyclerView.layoutManager = wrapContentLinearLayoutManager
        chatMessageRecyclerView.hasFixedSize()
        adapter = ChatAdapter(context, chatList as ArrayList<Message>)
        chatMessageRecyclerView.adapter = adapter

        if (auth.currentUser != null){
            userId = auth.currentUser!!.uid
            name = auth.currentUser!!.displayName as String
        }
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        if (supportTicket != null) {
            supportActionBar!!.title = supportTicket!!.title
        } else {
            supportActionBar!!.title = ""
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
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
        getAllSupportMessages()
    }

    private fun getAllSupportMessages() {
        databaseReference.child(Constants.supportChat).child(supportTicket!!.id)
            .orderByChild("timeStamp").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        chatList.clear()
                        for (dataSnap: DataSnapshot in snapshot.children) {
                            chatList.add(dataSnap.getValue(Message::class.java) as Message)
                        }
                        if (chatList.isNotEmpty()) {
                            adapter.notifyItemRangeChanged(0, chatList.size)
                            chatMessageRecyclerView.scrollToPosition(chatList.size)
                        }
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.chat_send_btn -> {
                sendMessage(messageType)
            }
            R.id.image_send_btn -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getImageFromGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> {

            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                getImageFromGallery()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                showAlert(context, "Read External Storage permission is necessary to upload image!")
            }
        }

    private fun getImageFromGallery() {
        val fileIntent =
            Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { oldData ->
                    val path = oldData.data//FileUtils.getRealPathFromURI(context, oldData.data!!)
                    val realPath: String? = FileUtil.getRealPathFromURI(context, path!!)
                    startLoading(context)
                    uploadImageOnFirebaseStorage("ChatImages/$userId",realPath!!,object : UploadImageCallback {
                        override fun onSuccess(imageUrl: String) {
                            dismiss()
                            Toast.makeText(context,"Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                            selectedImageServerUrl = imageUrl
                            messageType = "image"
                            sendMessage(messageType)
                            messageType = "text"
                        }

                    })
                }
            }
        }

    private fun sendMessage(type: String) {
        val message = chatBoxTextInputField.text.toString().trim()
        if (message.isNotEmpty() || selectedImageServerUrl.isNotEmpty()) {
            val id = databaseReference.push().key.toString()
            val timeStamp = System.currentTimeMillis()
            val messageItem = if (type == "text") {
                Message(id, Constants.firebaseUserId, messageType,message, timeStamp)
            } else {
                Message(
                    id,
                    Constants.firebaseUserId,
                    messageType,
                    selectedImageServerUrl,
                    timeStamp
                )
            }

            databaseReference.child(Constants.supportChat).child(supportTicket!!.id).child(id)
                .setValue(messageItem).addOnSuccessListener {
                    chatBoxTextInputField.setText("")
                    selectedImageServerUrl = ""
                    chatMessageRecyclerView.scrollToPosition(chatList.size - 1)
                    val hashMap = HashMap<String, Any>()
                    hashMap["lastReply"] = timeStamp
                    hashMap["lastReplyBy"] = name

                    databaseReference.child(Constants.ticketsReference).child(supportTicket!!.userId).child(supportTicket!!.id)
                        .updateChildren(hashMap)
                }.addOnFailureListener {
                    Log.d("TEST199",it.localizedMessage!!)
                }
        }
    }
}