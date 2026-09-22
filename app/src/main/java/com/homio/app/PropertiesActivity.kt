package com.homio.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class PropertiesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var llList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_properties)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        llList = findViewById(R.id.llPropertiesList)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupLandlordNav(this, bottomNav, R.id.nav_properties)

        findViewById<android.widget.Button>(R.id.btnAddProperty).setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }

        loadProperties()
    }

    override fun onResume() {
        super.onResume()
        loadProperties()
    }

    private fun loadProperties() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("properties")
            .whereEqualTo("landlordId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                llList.removeAllViews()
                if (snapshot.isEmpty) {
                    val empty = TextView(this)
                    empty.text = "No properties added yet. Tap + Add Property to get started."
                    llList.addView(empty)
                    return@addOnSuccessListener
                }
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
                for (doc in snapshot.documents) {
                    val address = doc.getString("address") ?: ""
                    val type = doc.getString("propertyType") ?: ""
                    val rooms = doc.getLong("rooms") ?: 0
                    val rent = doc.getDouble("rentAmount") ?: 0.0

                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_property, llList, false)

                    itemView.findViewById<TextView>(R.id.tvPropertyAddress).text = address
                    itemView.findViewById<TextView>(R.id.tvPropertyType).text = "$type · $rooms rooms"
                    itemView.findViewById<TextView>(R.id.tvPropertyRent).text =
                        "${currencyFormat.format(rent)} / month"

                    llList.addView(itemView)
                }
            }
    }
}
