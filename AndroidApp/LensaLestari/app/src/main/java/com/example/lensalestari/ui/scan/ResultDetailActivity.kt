package com.example.lensalestari.ui.scan

import android.graphics.BitmapFactory
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lensalestari.adapter.SimilarImageAdapter
import com.example.lensalestari.data.model.ClassificationResponse
import com.example.lensalestari.databinding.ActivityResultDetailBinding
import com.google.gson.Gson
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager

class ResultDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- GANTI BAGIAN INI ---
        val result = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("extra_result", ClassificationResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<ClassificationResponse>("extra_result")
        }

        val imageUriString = intent.getStringExtra("extra_image_uri")
        // -----------------------

        Log.d("ResultDetailActivity", "Objek Result diterima: $result")
        Log.d("ResultDetailActivity", "URI Gambar diterima: $imageUriString")
        Log.d("ResultDetail_Debug", "onCreate dipanggil. Data result yang diterima: $result")

        // Tampilkan gambar yang di-scan dari URI
        if (imageUriString != null) {
            val imageUri = imageUriString.toUri()
            try {
                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imgScanned.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("ResultDetailActivity", "Gagal memuat gambar dari URI", e)
            }
        }

        // Tampilkan hasil klasifikasi utama
        result?.let {
            // LOG 5: Jika result TIDAK NULL, blok ini akan jalan
            Log.d("ResultDetail_Debug", "Result TIDAK NULL. Menampilkan data ke TextViews...")
            binding.tvPredictedClass.text = "Kelas: ${it.classification.predictedClass}"
            binding.tvConfidence.text = "Confidence: ${String.format("%.2f", it.classification.confidence * 100)}%"
            binding.tvRewardMessage.text = it.rewardMessage
            binding.tvTotalPoints.text = "Total Poin Anda: ${it.totalPoints}"

            val adapter = SimilarImageAdapter(it.similarImages)
            binding.rvSimilarImages.layoutManager = GridLayoutManager(this, 2)
            binding.rvSimilarImages.adapter = adapter

        } ?: run {
            // LOG 6: Jika result NULL, blok ini yang akan jalan
            Log.e("ResultDetail_Debug", "FATAL: Result NULL! Tidak ada data yang bisa ditampilkan.")
            // Tampilkan pesan error di layar agar jelas
            binding.tvPredictedClass.text = "Error: Gagal menerima data hasil."
        }
    }
}
