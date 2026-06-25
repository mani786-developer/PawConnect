package com.example.authapp.ui.Vets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authapp.R
import com.example.authapp.domain.repository.AppointmentRepository
import com.example.authapp.domain.repository.AuthRepository
import com.example.authapp.model.PatientItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyPatientsActivity : AppCompatActivity() {

    @Inject lateinit var appointmentRepository: AppointmentRepository
    @Inject lateinit var authRepository: AuthRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_patients)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = PatientAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        supportActionBar?.apply {
            title = "My Patients"
            setDisplayHomeAsUpEnabled(true)
        }

        loadPatients()
    }

    private fun loadPatients() {
        val uid = authRepository.getCurrentUid() ?: return
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = appointmentRepository.getAppointmentsForVet(uid)
            progressBar.visibility = View.GONE

            if (result.isSuccess) {
                val accepted = result.getOrThrow()
                    .filter { it.status == "accepted" }

                if (accepted.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    val enriched = accepted.map { appointment ->
                        val ownerName = try {
                            val ownerResult = authRepository.getUserFromFirestore(appointment.petOwnerId)
                            if (ownerResult.isSuccess) {
                                ownerResult.getOrThrow().displayName
                            } else {
                                "Unknown Owner"
                            }
                        } catch (e: Exception) {
                            "Unknown Owner"
                        }

                        PatientItem(
                            appointment = appointment,
                            ownerName = ownerName
                        )
                    }

                    adapter.submitList(enriched)
                }
            } else {
                Toast.makeText(
                    this@MyPatientsActivity,
                    "Failed to load patients",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class PatientAdapter : RecyclerView.Adapter<PatientAdapter.ViewHolder>() {

    private val items = mutableListOf<PatientItem>()

    fun submitList(list: List<PatientItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPetName: TextView = itemView.findViewById(R.id.tvPetName)
        private val tvOwnerName: TextView = itemView.findViewById(R.id.tvOwnerName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(item: PatientItem) {
            val appointment = item.appointment

            tvPetName.text = "🐾 ${appointment.petName}"
            tvOwnerName.text = "Owner: ${item.ownerName}"
            tvDate.text = appointment.date
            tvTime.text = appointment.time
        }
    }
}