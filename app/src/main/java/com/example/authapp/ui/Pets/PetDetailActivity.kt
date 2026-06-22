package com.example.authapp.ui.pets

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.authapp.R
import com.example.authapp.model.Pet
import com.example.authapp.domain.repository.AuthRepository
import com.example.authapp.presentation.pets.PetActionState
import com.example.authapp.presentation.pets.PetEvent
import com.example.authapp.presentation.pets.PetViewModel
import com.example.authapp.ui.Chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PetDetailActivity : AppCompatActivity() {

    private val viewModel: PetViewModel by viewModels()

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_detail)

        // Read pet data from intent
        val pet = Pet(
            id          = intent.getStringExtra("petId")      ?: "",
            name        = intent.getStringExtra("petName")    ?: "",
            species     = intent.getStringExtra("petSpecies") ?: "",
            breed       = intent.getStringExtra("petBreed")   ?: "",
            age         = intent.getIntExtra("petAge", 0),
            gender      = intent.getStringExtra("petGender")  ?: "",
            description = intent.getStringExtra("petDesc")    ?: "",
            imageUrl    = intent.getStringExtra("petImage")   ?: "",
            ownerId     = intent.getStringExtra("ownerId")    ?: ""
        )

        bindPetData(pet)
        setupDeleteButton(pet)
        observeViewModel()

        supportActionBar?.apply {
            title = pet.name
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun bindPetData(pet: Pet) {
        findViewById<ImageView>(R.id.ivPetImage).load(pet.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_pet_placeholder)
            transformations(RoundedCornersTransformation(16f))
        }
        findViewById<TextView>(R.id.tvName).text        = pet.name
        findViewById<TextView>(R.id.tvSpeciesBreed).text = "${pet.species} • ${pet.breed}"
        findViewById<TextView>(R.id.tvAgeGender).text   = "${pet.age} year${if (pet.age != 1) "s" else ""} • ${pet.gender}"
        findViewById<TextView>(R.id.tvDescription).text = pet.description.ifEmpty { "No description added" }

//        // Only show delete button if this is the owner's pet
//        val isOwner = pet.ownerId == authRepository.getCurrentUid()
//        findViewById<Button>(R.id.btnDelete).visibility = if (isOwner) View.VISIBLE else View.GONE

        val btnMessageOwner = findViewById<Button>(R.id.btnMessageOwner)
        // Hide if viewing your own pet
        val isOwner = pet.ownerId == authRepository.getCurrentUid()
        btnMessageOwner.visibility = if (isOwner) View.GONE else View.VISIBLE
        btnMessageOwner.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("otherUserId", pet.ownerId)
                putExtra("otherName",   "Pet Owner")
            })
        }
    }

    private fun setupDeleteButton(pet: Pet) {
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete ${pet.name}?")
                .setMessage("This will permanently delete this pet and cannot be undone.")
                .setPositiveButton("Delete") { _, _ -> viewModel.deletePet(pet) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.actionState.collect { state ->
                        when (state) {
                            is PetActionState.Loading -> { }
                            is PetActionState.Success -> finish()
                            is PetActionState.Error   -> Toast.makeText(this@PetDetailActivity, state.message, Toast.LENGTH_LONG).show()
                            else -> { }
                        }
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is PetEvent.NavigateBack -> finish()
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
