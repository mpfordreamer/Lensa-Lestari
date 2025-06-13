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
import com.example.lensalestari.databinding.ActivityLoginBinding
import com.example.lensalestari.factory.ViewModelFactory
import com.example.lensalestari.data.model.LoginResponse // Pastikan import ke LoginResponse
import com.example.lensalestari.ui.main.MainActivity
import com.example.lensalestari.utils.SessionManager // Pastikan import SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val viewModel: AuthViewModel by viewModels {
        // 1. Dapatkan ApiService terlebih dahulu menggunakan context
        val apiService = ApiClient.getInstance(this)

        // 2. Jika apiService null (URL belum diset), kita tidak bisa membuat repository.
        // Kita akan melempar error di sini agar jelas. Alternatifnya, handle di ViewModel.
        if (apiService == null) {
            throw IllegalStateException("Base URL is not set. Cannot create ApiService.")
        }

        // 3. Buat AuthRepository dengan ApiService yang sudah didapat
        val repository = AuthRepository(apiService)

        // 4. Berikan repository yang sudah jadi ke ViewModelFactory
        ViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Observasi hasil login dari ViewModel
        viewModel.loginResult.observe(this) { response: LoginResponse? ->
            // Cek jika response dan user di dalamnya tidak null
            if (response?.user != null) {
                // Ambil data dari response
                val token = response.token
                val user = response.user
                val cleanedName = user.name.trim()

                // Buat instance SessionManager dan simpan data sesi
                val sessionManager = SessionManager(this)
                sessionManager.saveAuthData(cleanedName, user.email, token, user.totalPoints)

                Toast.makeText(this, "Login berhasil, selamat datang ${cleanedName}!", Toast.LENGTH_LONG).show()

                // Pindah ke MainActivity setelah data sesi disimpan
                val intent = Intent(this, MainActivity::class.java).apply {
                    // Hapus semua activity sebelumnya agar pengguna tidak bisa kembali ke halaman login
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } else {
                // Tampilkan pesan error jika login gagal
                Toast.makeText(this, "Login gagal, periksa kembali email dan password", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener untuk tombol login
        binding.btnLogin.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        // Listener untuk teks pindah ke halaman register
        binding.textToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}