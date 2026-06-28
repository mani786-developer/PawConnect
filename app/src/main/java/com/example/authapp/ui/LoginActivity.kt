package com.example.authapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.authapp.R
import com.example.authapp.presentation.auth.LoginEvent
import com.example.authapp.presentation.auth.LoginUiState
import com.example.authapp.presentation.auth.LoginViewModel
import com.example.authapp.utils.ValidationUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint                          // ← Hilt activated on this Activity
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()   // ← no more manual Firebase

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindViews()
        setupToggle()
        setupClickListeners()
        observeViewModel()
    }

    private fun bindViews() {
        etEmail     = findViewById(R.id.etEmail)
        etPhone     = findViewById(R.id.etPhone)
        etPassword  = findViewById(R.id.etPassword)
        tilEmail    = findViewById(R.id.tilEmail)
        tilPhone    = findViewById(R.id.tilPhone)
        tilPassword = findViewById(R.id.tilPassword)
        btnLogin    = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        findViewById<TextView>(R.id.tvSignUp).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun setupToggle() {
        findViewById<RadioGroup>(R.id.rgLoginMethod)
            .setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.rbEmail) {
                    tilEmail.visibility    = View.VISIBLE
                    tilPhone.visibility    = View.GONE
                    tilPassword.visibility = View.VISIBLE
                } else {
                    tilEmail.visibility    = View.GONE
                    tilPhone.visibility    = View.VISIBLE
                    tilPassword.visibility = View.GONE
                }
            }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val isEmail = findViewById<RadioButton>(R.id.rbEmail).isChecked
            if (isEmail) {
                // ViewModel handles Firebase — Activity just passes values
                viewModel.loginWithEmail(
                    email    = etEmail.text.toString().trim(),
                    password = etPassword.text.toString()
                )
            } else {
                initiatePhoneLogin()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // State → update UI
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is LoginUiState.Idle    -> {
                                showLoading(false)
                                tilPassword.error = null
                            }
                            is LoginUiState.Loading -> showLoading(true)
                            is LoginUiState.Error   -> {
                                showLoading(false)
                                tilPassword.error = state.message
                            }
                        }
                    }
                }

                // Events → navigate (one-shot, won't re-fire on rotation)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is LoginEvent.NavigateTo -> {
                                val dest = DashboardActivity::class.java
                                startActivity(Intent(this@LoginActivity, dest).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }


    private fun initiatePhoneLogin() {
        val phone = etPhone.text.toString().trim()
        if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.error = "Enter a valid phone number"
            return
        }
        tilPhone.error = null
        showLoading(true)

        val formatted = ValidationUtils.formatPhoneForFirebase(phone)

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(formatted)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(c: PhoneAuthCredential) { }
                    override fun onVerificationFailed(e: FirebaseException) {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, e.message ?: "Failed", Toast.LENGTH_LONG).show()
                    }
                    override fun onCodeSent(vId: String, t: PhoneAuthProvider.ForceResendingToken) {
                        showLoading(false)
                        startActivity(Intent(this@LoginActivity, OtpActivity::class.java).apply {
                            putExtra("type",           "phone_login")
                            putExtra("phone",          formatted)
                            putExtra("verificationId", vId)
                        })
                    }
                }).build()
        )
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled     = !show
    }
}