package com.example.lensalestari.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lensalestari.databinding.ActivitySetupUrlBinding
import com.example.lensalestari.ui.launcher.LauncherActivity
import com.example.lensalestari.utils.UrlManager

class SetupUrlActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupUrlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan ViewBinding untuk mengakses komponen UI
        binding = ActivitySetupUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memberi aksi pada tombol simpan
        binding.btnSaveUrl.setOnClickListener {
            val url = binding.etBaseUrl.text.toString().trim()

            // Validasi input sederhana, pastikan tidak kosong dan diawali http
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                // Panggil UrlManager yang sudah kita buat untuk menyimpan URL
                UrlManager.saveBaseUrl(this, url)

                Toast.makeText(this, "Alamat server disimpan!", Toast.LENGTH_SHORT).show()

                // Arahkan ke halaman selanjutnya
                navigateToNextScreen()
            } else {
                Toast.makeText(this, "Masukkan alamat server yang valid (diawali http:// atau https://)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToNextScreen() {
        // Pindah ke Launcher Activity Anda setelah setup selesai
        val intent = Intent(this, LauncherActivity::class.java).apply {
            // Hapus semua activity sebelumnya dari back stack agar pengguna tidak bisa kembali ke halaman setup
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Tutup activity ini
    }
}