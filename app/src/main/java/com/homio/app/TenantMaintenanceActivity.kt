package com.homio.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.homio.app.models.MaintenanceRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TenantMaintenanceActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var llList: LinearLayout

    private val categories = arrayOf("Plumbing", "Electrical", "Appliance", "Structural", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_maintenance)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        llList = findViewById(R.id.llMaintenanceList)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_maintenance)

        findViewById<android.widget.Button>(R.id.btnReportProblem).setOnClickListener {
            showReportDialog()
        }

        loadMyRequests()
    }

    override fun onResume() {
        super.onResume()
        loadMyRequests()
    }

    private fun loadMyRequests() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("maintenance")
            .whereEqualTo("tenantId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                llList.removeAllViews()
                if (snapshot.isEmpty) {
                    val empty = TextView(this)
                    empty.text = "No maintenance requests yet."
                    empty.setTextColor(ContextCompat.getColor(this, R.color.homio_gray))
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
                        .inflate(R.layout.item_maintenance_request, llList, false)

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

                    llList.addView(itemView)
                }
            }
    }

    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_problem, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etReportTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etReportDescription)
        val spCategory = dialogView.findViewById<Spinner>(R.id.spCategory)
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        AlertDialog.Builder(this)
            .setTitle("Report a Problem")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val category = spCategory.selectedItem?.toString() ?: "Other"

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                val newDocRef = db.collection("maintenance").document()
                val request = MaintenanceRequest(
                    requestId = newDocRef.id,
                    tenantId = uid,
                    title = title,
                    description = description,
                    category = category,
                    status = "Submitted",
                    createdDate = com.google.firebase.Timestamp(Date())
                )
                newDocRef.set(request)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request submitted", Toast.LENGTH_SHORT).show()
                        loadMyRequests()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
