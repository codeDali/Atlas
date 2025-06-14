package com.example.atlas.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.atlas.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EmergencyContactActivity : AppCompatActivity() {
    private lateinit var contact1Input: TextInputEditText
    private lateinit var contact2Input: TextInputEditText
    private lateinit var sarContactInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences

    private val pickContact1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val contactUri = data?.data
            if (contactUri != null) {
                val cursor = contentResolver.query(contactUri, null, null, null, null)
                if (cursor?.moveToFirst() == true) {
                    val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (phoneIndex != -1) {
                        val phoneNumber = cursor.getString(phoneIndex)
                        contact1Input.setText(phoneNumber)
                    }
                }
                cursor?.close()
            }
        }
    }

    private val pickContact2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val contactUri = data?.data
            if (contactUri != null) {
                val cursor = contentResolver.query(contactUri, null, null, null, null)
                if (cursor?.moveToFirst() == true) {
                    val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (phoneIndex != -1) {
                        val phoneNumber = cursor.getString(phoneIndex)
                        contact2Input.setText(phoneNumber)
                    }
                }
                cursor?.close()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contact)

        contact1Input = findViewById(R.id.contact1Input)
        contact2Input = findViewById(R.id.contact2Input)
        sarContactInput = findViewById(R.id.sarContactInput)
        saveButton = findViewById(R.id.saveButton)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        sarContactInput.setText("115")
        sarContactInput.isEnabled = false

        loadSavedContacts()

        contact1Input.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickContact1.launch(intent)
        }

        contact2Input.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickContact2.launch(intent)
        }

        saveButton.setOnClickListener {
            if (validateInput()) {
                saveContacts()
                InputActivity.start(this)
                finish()
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        val contact1 = contact1Input.text.toString().trim()
        val contact2 = contact2Input.text.toString().trim()

        if (!isValidPhoneNumber(contact1)) {
            contact1Input.error = "Format nomor tidak valid. Gunakan format +62 atau 08xx-xxxxx-xxxx"
            isValid = false
        }

        if (!isValidPhoneNumber(contact2)) {
            contact2Input.error = "Format nomor tidak valid. Gunakan format +62 atau 08xx-xxxxx-xxxx"
            isValid = false
        }

        return isValid
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.startsWith("+62")) {
            return cleanPhone.length >= 10 && cleanPhone.length <= 14
        }
        if (cleanPhone.startsWith("08")) {
            return cleanPhone.length >= 10 && cleanPhone.length <= 13
        }
        
        return false
    }

    private fun saveContacts() {
        with(sharedPreferences.edit()) {
            putString(KEY_CONTACT_1, contact1Input.text.toString())
            putString(KEY_CONTACT_2, contact2Input.text.toString())
            putString(KEY_SAR_CONTACT, "115")
            apply()
        }
        Toast.makeText(this, "Kontak berhasil disimpan", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedContacts() {
        contact1Input.setText(sharedPreferences.getString(KEY_CONTACT_1, ""))
        contact2Input.setText(sharedPreferences.getString(KEY_CONTACT_2, ""))
    }

    companion object {
        private const val PREFS_NAME = "EmergencyContacts"
        private const val KEY_CONTACT_1 = "contact1"
        private const val KEY_CONTACT_2 = "contact2"
        private const val KEY_SAR_CONTACT = "sar_contact"

        fun start(context: Context) {
            val intent = Intent(context, EmergencyContactActivity::class.java)
            context.startActivity(intent)
        }
    }
} 