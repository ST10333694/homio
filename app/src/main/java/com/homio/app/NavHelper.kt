package com.homio.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavHelper {

    fun setupTenantNav(activity: AppCompatActivity, bottomNav: BottomNavigationView, currentItemId: Int) {
        bottomNav.selectedItemId = currentItemId
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true
            val target: Class<*> = when (item.itemId) {
                R.id.nav_home -> TenantDashboardActivity::class.java
                R.id.nav_rent -> TenantRentActivity::class.java
                R.id.nav_maintenance -> TenantMaintenanceActivity::class.java
                R.id.nav_messages -> TenantMessagesActivity::class.java
                R.id.nav_settings -> SettingsActivity::class.java
                else -> return@setOnItemSelectedListener false
            }
            activity.startActivity(Intent(activity, target))
            activity.finish()
            true
        }
    }

    fun setupLandlordNav(activity: AppCompatActivity, bottomNav: BottomNavigationView, currentItemId: Int) {
        bottomNav.selectedItemId = currentItemId
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true
            val target: Class<*> = when (item.itemId) {
                R.id.nav_home_l -> LandlordDashboardActivity::class.java
                R.id.nav_properties -> PropertiesActivity::class.java
                R.id.nav_maintenance_l -> LandlordMaintenanceActivity::class.java
                R.id.nav_messages_l -> LandlordMessagesActivity::class.java
                R.id.nav_settings_l -> SettingsActivity::class.java
                else -> return@setOnItemSelectedListener false
            }
            activity.startActivity(Intent(activity, target))
            activity.finish()
            true
        }
    }
}
