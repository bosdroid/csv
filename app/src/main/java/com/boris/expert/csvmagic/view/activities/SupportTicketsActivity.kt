package com.boris.expert.csvmagic.view.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.R
import com.boris.expert.csvmagic.adapters.TicketsAdapter
import com.boris.expert.csvmagic.model.SupportTicket
import com.boris.expert.csvmagic.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SupportTicketsActivity : BaseActivity(), View.OnClickListener,
    TicketsAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var fabCreateTicket: FloatingActionButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var ticketList = mutableListOf<SupportTicket>()
    private lateinit var supportTicketsRecyclerView: RecyclerView
    private lateinit var adapter: TicketsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_tickets)

        initViews()
        setUpToolbar()
    }

    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        fabCreateTicket = findViewById(R.id.fab_add_support_ticket)
        supportTicketsRecyclerView = findViewById(R.id.support_tickets_recyclerview)
        fabCreateTicket.setOnClickListener(this)
        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        supportTicketsRecyclerView.layoutManager = LinearLayoutManager(context)
        supportTicketsRecyclerView.hasFixedSize()
        adapter = TicketsAdapter(context, ticketList as ArrayList<SupportTicket>)
        supportTicketsRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)

    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.support_tickets_text)
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
        getAllTickets()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_add_support_ticket -> {
                openCreateTicketDialog()
            }
            else -> {

            }
        }
    }


    private fun getAllTickets() {
        startLoading(context)
        databaseReference.child(Constants.ticketsReference).orderByChild("status")
            .equalTo("pending").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dismiss()
                    if (snapshot.hasChildren()) {
                        ticketList.clear()
                        for (dataSnap: DataSnapshot in snapshot.children) {
                            ticketList.add(dataSnap.getValue(SupportTicket::class.java) as SupportTicket)
                        }

                        if (ticketList.isNotEmpty()) {
                            adapter.notifyItemRangeChanged(0, ticketList.size)
                        }
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun openCreateTicketDialog() {
        val layout =
            LayoutInflater.from(context).inflate(R.layout.create_ticket_dialog_layout, null)
        val cancelBtn = layout.findViewById<MaterialButton>(R.id.create_ticket_dialog_cancel_btn)
        val submitBtn = layout.findViewById<MaterialButton>(R.id.create_ticket_dialog_submit_btn)
        val titleBox = layout.findViewById<TextInputEditText>(R.id.create_ticket_title_input_field)
        val messageBox =
            layout.findViewById<TextInputEditText>(R.id.create_ticket_message_input_field)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(layout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelBtn.setOnClickListener {
            alert.dismiss()
        }

        submitBtn.setOnClickListener {
            val title = titleBox.text.toString().trim()
            val message = messageBox.text.toString().trim()
            when {
                title.isEmpty() -> {
                    titleBox.error = getString(R.string.empty_text_error)
                    titleBox.requestFocus()
                }
                message.isEmpty() -> {
                    messageBox.error = getString(R.string.empty_text_error)
                    messageBox.requestFocus()
                }
                else -> {
                    alert.dismiss()
                    val id = databaseReference.push().key as String
                    val ticket = SupportTicket(
                        id, getString(R.string.app_name),
                        if (auth.currentUser != null) {
                            auth.currentUser!!.displayName.toString()
                        } else {
                            ""
                        }, title, message, System.currentTimeMillis(), "open",0,""
                    )
                    startLoading(context)
                    databaseReference.child(Constants.ticketsReference)
                        .child(id).setValue(ticket).addOnSuccessListener {
                            dismiss()
                            titleBox.setText("")
                            messageBox.setText("")
                            getAllTickets()
                        }.addOnFailureListener {
                            dismiss()
                        }
                }
            }

        }

    }

    override fun onItemClick(position: Int) {
       val item = ticketList[position]
        startActivity(Intent(context,SupportChatActivity::class.java).apply {
            putExtra("S_TICKET",item)
        })
    }
}