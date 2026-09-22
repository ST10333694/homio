package com.homio.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TenantDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_home)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvOpenRequestsCount = findViewById<TextView>(R.id.tvOpenRequestsCount)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("fullName") ?: "there"
                    tvWelcome.text = "Good morning, $name"
                }

            // Live count of this tenant's maintenance requests that are not yet completed.
            // Rent/lease card values above are placeholders until Property <-> Tenant
            // linking is added - noted for the final PoE.
            db.collection("maintenance")
                .whereEqualTo("tenantId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val openCount = snapshot.documents.count { it.getString("status") != "Completed" }
                    tvOpenRequestsCount.text = openCount.toString()
                }
        }

        findViewById<TextView>(R.id.tvViewRequests).setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_maintenance
        }
    }
}