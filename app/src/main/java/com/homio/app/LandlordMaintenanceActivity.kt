package com.homio.app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

// NOTE: For simplicity in the prototype this shows ALL maintenance requests
// rather than filtering to only this landlord's own properties, since
// Property <-> Tenant <-> Landlord linking isn't built yet. That linking is
// a good next step for the final PoE.
class LandlordMaintenanceActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var llList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_maintenance)

        db = FirebaseFirestore.getInstance()
        llList = findViewById(R.id.llRequestsList)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupLandlordNav(this, bottomNav, R.id.nav_maintenance_l)

        loadRequests()
    }

    override fun onResume() {
        super.onResume()
        loadRequests()
    }

    private fun loadRequests() {
        db.collection("maintenance")
            .get()
            .addOnSuccessListener { snapshot ->
                llList.removeAllViews()
                if (snapshot.isEmpty) {
                    val empty = TextView(this)
                    empty.text = "No maintenance requests yet."
                    llList.addView(empty)
                    return@addOnSuccessListener
                }
                for (doc in snapshot.documents) {
                    val title = doc.getString("title") ?: "Untitled"
                    val category = doc.getString("category") ?: ""
                    val status = doc.getString("status") ?: "Submitted"
                    val timestamp = doc.getTimestamp("createdDate")
                    val dateStr = if (timestamp != null) {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(timestamp.toDate())
                    } else ""

                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_maintenance_request_landlord, llList, false)

                    itemView.findViewById<TextView>(R.id.tvItemTitle).text = title
                    itemView.findViewById<TextView>(R.id.tvItemSubtitle).text = "$category · $dateStr"

                    val statusView = itemView.findViewById<TextView>(R.id.tvItemStatus)
                    statusView.text = status
                    val color = when (status) {
                        "Completed" -> R.color.homio_green
                        "In Progress" -> R.color.homio_orange
                        else -> R.color.homio_purple
                    }
                    statusView.backgroundTintList = ContextCompat.getColorStateList(this, color)

                    val btnComplete = itemView.findViewById<android.widget.Button>(R.id.btnMarkComplete)
                    if (status == "Completed") {
                        btnComplete.isEnabled = false
                        btnComplete.text = "Completed"
                    } else {
                        btnComplete.setOnClickListener {
                            db.collection("maintenance").document(doc.id)
                                .update("status", "Completed")
                                .addOnSuccessListener { loadRequests() }
                        }
                    }

                    llList.addView(itemView)
                }
            }
    }
}
