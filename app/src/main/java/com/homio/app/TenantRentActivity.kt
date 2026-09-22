package com.homio.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

// NOTE: Rent figures shown here are placeholder values for the prototype demo.
// Wiring this to real per-tenant lease data in Firestore is a good next step
// for the final PoE once Property <-> Tenant linking is built.
class TenantRentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_rent)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_rent)
    }
}
