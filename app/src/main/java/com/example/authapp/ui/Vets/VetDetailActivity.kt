package com.example.authapp.ui.Vets

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.authapp.R
import com.example.authapp.ui.Appointments.BookAppointmentActivity
import com.example.authapp.ui.Chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VetDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vet_detail)

        val vetUid   = intent.getStringExtra("vetUid")   ?: ""
        val vetName  = intent.getStringExtra("vetName")  ?: ""
        val clinic   = intent.getStringExtra("clinic")   ?: ""
        val city     = intent.getStringExtra("city")     ?: ""
        val address  = intent.getStringExtra("address")  ?: ""
        val spec     = intent.getStringExtra("spec")     ?: ""
        val years    = intent.getIntExtra("years", 0)
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""

        val ivPhoto        = findViewById<ImageView>(R.id.ivVetPhoto)
        val tvName         = findViewById<TextView>(R.id.tvVetName)
        val tvSpec         = findViewById<TextView>(R.id.tvSpecialization)
        val tvClinic       = findViewById<TextView>(R.id.tvClinicName)
        val tvCity         = findViewById<TextView>(R.id.tvCity)
        val tvAddress      = findViewById<TextView>(R.id.tvAddress)
        val tvExperience   = findViewById<TextView>(R.id.tvExperience)
        val btnBook        = findViewById<Button>(R.id.btnCall)
        val btnMsg         = findViewById<Button>(R.id.btnMessage)

        tvName.text       = "Dr. $vetName"
        tvSpec.text       = spec
        tvClinic.text     = clinic
        tvCity.text       = city
        tvAddress.text    = address
        tvExperience.text = "$years years of experience"

        if (imageUrl.isNotEmpty()) {
            ivPhoto.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_pet_placeholder)
                error(R.drawable.ic_pet_placeholder)
            }
        }

        btnBook.setOnClickListener {
            startActivity(Intent(this, BookAppointmentActivity::class.java).apply {
                putExtra("vetId",   vetUid)
                putExtra("vetName", vetName)
                putExtra("clinic",  clinic)
            })
        }

        btnMsg.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("otherUserId", vetUid)
                putExtra("otherName",   "Dr. $vetName")
            })
        }

        supportActionBar?.apply {
            title = "Vet Profile"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}