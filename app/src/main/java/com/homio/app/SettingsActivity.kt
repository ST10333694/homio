package com.homio.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button
    private lateinit var radioGroupLanguage: RadioGroup

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage)

        // Reflect the currently saved language in the radio buttons
        when (LocaleHelper.getSavedLanguage(this)) {
            "zu" -> radioGroupLanguage.check(R.id.radioZulu)
            "tn" -> radioGroupLanguage.check(R.id.radioSetswana)
            else -> radioGroupLanguage.check(R.id.radioEnglish)
        }

        loadProfile()

        btnSave.setOnClickListener { saveProfile() }

        radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.radioZulu -> "zu"
                R.id.radioSetswana -> "tn"
                else -> "en"
            }
            if (languageCode != LocaleHelper.getSavedLanguage(this)) {
                LocaleHelper.saveLanguage(this, languageCode)

                // Persist to Firestore too, since User.language already exists for this
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("users").document(uid).update("language", languageCode)
                }

                recreate()
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etFullName.setText(doc.getString("fullName") ?: "")
                etPhone.setText(doc.getString("phone") ?: "")
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "fullName" to etFullName.text.toString().trim(),
            "phone" to etPhone.text.toString().trim()
        )
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${getString(R.string.settings_save_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}