package com.homio.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LandlordDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupLandlordNav(this, bottomNav, R.id.nav_home_l)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvPropertiesCount = findViewById<TextView>(R.id.tvPropertiesCount)
        val tvOpenRequestsCount = findViewById<TextView>(R.id.tvOpenRequestsCount)
        // Tenants count is a placeholder ("0") until Property <-> Tenant linking
        // is built - a good next feature for the final PoE.

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("fullName") ?: "there"
                    tvWelcome.text = "Good morning, $name"
                }

            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    tvPropertiesCount.text = snapshot.size().toString()
                }

            // Simplified for the prototype: shows all open requests across tenants
            // rather than only ones tied to this landlord's own properties.
            db.collection("maintenance")
                .get()
                .addOnSuccessListener { snapshot ->
                    val openCount = snapshot.documents.count { it.getString("status") != "Completed" }
                    tvOpenRequestsCount.text = openCount.toString()
                }
        }
    }
}
