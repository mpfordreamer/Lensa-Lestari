package com.example.lensalestari.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.lensalestari.data.api.ApiClient
import com.example.lensalestari.data.repository.AuthRepository
import com.example.lensalestari.databinding.ActivityRegisterBinding
import com.example.lensalestari.factory.ViewModelFactory
import com.example.lensalestari.data.model.RegisterResponse // Pastikan import ke RegisterResponse
import com.example.lensalestari.ui.main.MainActivity
import com.example.lensalestari.utils.SessionManager // Pastikan import SessionManager

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        // 1. Dapatkan ApiService menggunakan context
        val apiService = ApiClient.getInstance(this)

        // 2. Handle jika URL belum diset
        if (apiService == null) {
            throw IllegalStateException("Base URL is not set. Cannot create ApiService.")
        }

        // 3. Buat AuthRepository
        val repository = AuthRepository(apiService)

        // 4. Berikan repository ke ViewModelFactory
        ViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Observasi hasil register dari ViewModel
        viewModel.registerResult.observe(this) { response: RegisterResponse? ->
            // Cek jika response dan user di dalamnya tidak null
            if (response?.user != null) {
                // Ambil data dari response
                val token = response.token
                val user = response.user
                val cleanedName = user.name.trim()

                // Buat instance SessionManager dan simpan data sesi
                val sessionManager = SessionManager(this)
                sessionManager.saveAuthData(cleanedName, user.email, token, user.totalPoints)

                Toast.makeText(this, "Registrasi berhasil, selamat datang ${cleanedName}!", Toast.LENGTH_LONG).show()

                // Pindah ke MainActivity setelah data sesi disimpan
                val intent = Intent(this, MainActivity::class.java).apply {
                    // Hapus semua activity sebelumnya dari tumpukan
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } else {
                // Tampilkan pesan error jika registrasi gagal
                Toast.makeText(this, "Registrasi gagal, email mungkin sudah digunakan", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener untuk tombol register
        binding.btnRegister.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nama, email, dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(name, email, password)
        }

        // Listener untuk teks kembali ke halaman login
        binding.textToLogin.setOnClickListener {
            finish() // Tutup activity ini untuk kembali ke halaman login
        }
    }
}