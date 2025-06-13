package com.example.lensalestari.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.lensalestari.R
import com.example.lensalestari.ui.launcher.LauncherActivity
import com.example.lensalestari.ui.setup.SetupUrlActivity
import com.example.lensalestari.utils.UrlManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    // Durasi splash screen dalam milidetik (misalnya 2.5 detik)
    private val SPLASH_DELAY: Long = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tampilkan layout splash screen
        setContentView(R.layout.activity_splash)

        // Gunakan Handler untuk menunda eksekusi
        Handler(Looper.getMainLooper()).postDelayed({
            // Kode di dalam sini akan dijalankan setelah SPLASH_DELAY berlalu

            // Periksa apakah URL sudah ada
            val baseUrl = UrlManager.getBaseUrl(this)

            val nextActivity = if (baseUrl.isNullOrBlank()) {
                // Jika BELUM ADA, arahkan ke halaman setup URL
                SetupUrlActivity::class.java
            } else {
                // Jika SUDAH ADA, arahkan ke Launcher Activity Anda
                LauncherActivity::class.java
            }

            startActivity(Intent(this, nextActivity))

            // Tutup SplashActivity agar tidak bisa kembali ke sini
            finish()

        }, SPLASH_DELAY)
    }
}