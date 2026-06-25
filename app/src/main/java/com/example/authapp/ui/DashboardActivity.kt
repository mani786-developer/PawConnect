package com.example.authapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.authapp.R
import com.example.authapp.domain.repository.AuthRepository
import com.example.authapp.presentation.chat.ChatViewModel
import com.example.authapp.ui.Appointments.VetAppointmentsActivity
import com.example.authapp.ui.Appointments.MyAppointmentsActivity
import com.example.authapp.ui.Chat.InboxActivity
import com.example.authapp.ui.Vets.MyPatientsActivity
import com.example.authapp.ui.discover.DiscoverActivity
import com.example.authapp.ui.pets.MyPetsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        loadUserAndSetupDashboard()
        setupLogout()
    }

    private fun loadUserAndSetupDashboard() {
        val uid = authRepository.getCurrentUid() ?: return
        lifecycleScope.launch {
            val result = authRepository.getUserFromFirestore(uid)
            if (result.isSuccess) {
                val user = result.getOrThrow()

                // Welcome header
                val displayName = user.displayName.ifEmpty { "User" }
                val roleLabel = when (user.role) {
                    "pet_owner"    -> "Pet Owner"
                    "veterinarian" -> "Veterinarian"
                    else           -> ""
                }
                findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $displayName 👋"
                findViewById<TextView>(R.id.tvRole).text    = roleLabel

                // Show unread badge on owner + vet message cards
                val chatViewModel: ChatViewModel by viewModels()
                chatViewModel.loadUnreadCount()

                lifecycleScope.launch {
                    chatViewModel.unreadCount.collect { count ->

                        // Owner badge
                        findViewById<TextView>(R.id.tvMessageBadge)?.apply {
                            visibility = if (count > 0) View.VISIBLE else View.GONE
                            text = count.toString()
                        }

                        // Vet badge
                        findViewById<TextView>(R.id.tvVetMessageBadge)?.apply {
                            visibility = if (count > 0) View.VISIBLE else View.GONE
                            text = count.toString()
                        }
                    }
                }
                // Show correct dashboard based on role
                if (user.role == "veterinarian") {
                    setupVetDashboard()
                } else {
                    setupOwnerDashboard()
                }
            }
        }
    }

    private fun setupOwnerDashboard() {
        // Show owner rows, hide vet rows
        findViewById<View>(R.id.rowOwner1).visibility = View.VISIBLE
        findViewById<View>(R.id.rowOwner2).visibility = View.VISIBLE
        findViewById<View>(R.id.rowOwner3).visibility = View.VISIBLE
        findViewById<View>(R.id.rowVet1).visibility   = View.GONE
        findViewById<View>(R.id.rowVet2).visibility   = View.GONE

        // My Pets
        findViewById<CardView>(R.id.cardMyPets).setOnClickListener {
            startActivity(Intent(this, MyPetsActivity::class.java))
        }
        // Stubs for now
        findViewById<CardView>(R.id.cardFindPets).setOnClickListener {
            startActivity(Intent(this, DiscoverActivity::class.java))
        }
        findViewById<CardView>(R.id.cardFindVets).setOnClickListener {
            startActivity(Intent(this, com.example.authapp.ui.Vets.FindVetsActivity::class.java))

        }
        findViewById<CardView>(R.id.cardMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }
        findViewById<CardView>(R.id.cardAppointments).setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))
        }


        findViewById<CardView>(R.id.cardProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }



         }

    private fun setupVetDashboard() {
        // Show vet rows, hide owner rows
        findViewById<View>(R.id.rowOwner1).visibility = View.GONE
        findViewById<View>(R.id.rowOwner2).visibility = View.GONE
        findViewById<View>(R.id.rowOwner3).visibility = View.GONE
        findViewById<View>(R.id.rowVet1).visibility = View.VISIBLE
        findViewById<View>(R.id.rowVet2).visibility = View.VISIBLE

        findViewById<CardView>(R.id.cardVetAppointments).setOnClickListener {
            startActivity(Intent(this, VetAppointmentsActivity::class.java))
        }
        findViewById<CardView>(R.id.cardVetProfile).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    com.example.authapp.ui.Vets.VetProfileSetupActivity::class.java
                )
            )
        }
        findViewById<CardView>(R.id.cardVetMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }
        findViewById<CardView>(R.id.cardVetProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<CardView>(R.id.cardPatients).setOnClickListener {
            startActivity(Intent(this, MyPatientsActivity::class.java))
        }
    }


    private fun setupLogout() {
        findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            lifecycleScope.launch {
                authRepository.logout()
                startActivity(Intent(this@DashboardActivity, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
