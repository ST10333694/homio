package com.homio.app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

// Single shared conversation thread for the prototype - not tied to a specific
// tenant login yet. Real per-tenant conversations are a good next step for the final PoE.
class TenantMessagesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var llMessagesList: LinearLayout
    private lateinit var scrollMessages: ScrollView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSendMessage: Button

    private val myRole = "tenant"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_messages)

        db = FirebaseFirestore.getInstance()
        llMessagesList = findViewById(R.id.llMessagesList)
        scrollMessages = findViewById(R.id.scrollMessages)
        etMessageInput = findViewById(R.id.etMessageInput)
        btnSendMessage = findViewById(R.id.btnSendMessage)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_messages)

        btnSendMessage.setOnClickListener { sendMessage() }

        loadMessages()
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
    }

    private fun sendMessage() {
        val text = etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val message = hashMapOf(
            "text" to text,
            "senderRole" to myRole,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("messages").add(message)
            .addOnSuccessListener {
                etMessageInput.setText("")
                loadMessages()
            }
    }

    private fun loadMessages() {
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                llMessagesList.removeAllViews()

                if (snapshot.isEmpty) {
                    val empty = TextView(this)
                    empty.text = "No messages yet. Say hello!"
                    empty.setPadding(0, 12, 0, 12)
                    llMessagesList.addView(empty)
                    return@addOnSuccessListener
                }

                for (doc in snapshot.documents) {
                    val text = doc.getString("text") ?: continue
                    val senderRole = doc.getString("senderRole") ?: "landlord"
                    val timestamp = doc.getTimestamp("timestamp")
                    val timeStr = if (timestamp != null) {
                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(timestamp.toDate())
                    } else ""

                    val isMine = senderRole == myRole
                    val layoutRes = if (isMine) R.layout.item_message_sent else R.layout.item_message_received
                    val itemView = LayoutInflater.from(this).inflate(layoutRes, llMessagesList, false)

                    itemView.findViewById<TextView>(R.id.tvMessageText).text = text
                    itemView.findViewById<TextView>(R.id.tvMessageTime).text = timeStr

                    if (!isMine) {
                        itemView.findViewById<TextView>(R.id.tvSenderLabel)?.text = "Landlord"
                    }

                    llMessagesList.addView(itemView)
                }

                scrollMessages.post { scrollMessages.fullScroll(ScrollView.FOCUS_DOWN) }
            }
    }
}
