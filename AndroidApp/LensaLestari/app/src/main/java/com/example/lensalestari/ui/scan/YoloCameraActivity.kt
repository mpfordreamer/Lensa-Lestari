package com.example.lensalestari.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Base64 // <-- IMPORT INI
import android.util.Log
import android.view.View // <-- IMPORT INI
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.TransformExperimental
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lensalestari.data.api.ApiClient
import com.example.lensalestari.data.repository.ScanRepository
import com.example.lensalestari.databinding.ActivityYoloCameraBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@OptIn(TransformExperimental::class)
class YoloCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoloCameraBinding

    private val viewModel: YoloViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val apiService = ApiClient.getInstance(this@YoloCameraActivity)
                    ?: throw IllegalStateException("Base URL is not set. Cannot create ApiService.")
                val repository = ScanRepository(apiService)
                if (modelClass.isAssignableFrom(YoloViewModel::class.java)) {
                    return YoloViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var lastSentTimestamp = 0L
    private val frameIntervalMillis = 1500L // Sedikit diperlambat untuk memberi waktu server

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk fitur ini.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoloCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCameraPermission()

        viewModel.yoloResult.observe(this) { response ->
            if (response == null) return@observe

            // Hentikan analisis frame lebih lanjut
            imageAnalysis?.let { cameraProvider?.unbind(it) }

            // --- MULAI PERUBAHAN DI SINI (MENERAPKAN SARAN TEMAN ANDA) ---
            try {
                // 1. Ambil string base64 dari respons
                val base64String = response.imageWithBoxesBase64

                // 2. Bersihkan string dari prefix "data:image/..." jika ada
                val cleanBase64 = if (base64String.contains(",")) {
                    base64String.substringAfter(",")
                } else {
                    base64String
                }

                // 3. Decode string yang sudah bersih menjadi byte array
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

                // 4. Ubah byte array menjadi sebuah Bitmap (gambar)
                val decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                // 5. Atur Bitmap tersebut ke ImageView Anda
                binding.resultImageView.setImageBitmap(decodedImage)

                // 6. Atur visibilitas untuk menampilkan hasil
                binding.previewView.visibility = View.GONE
                binding.bboxOverlay.visibility = View.GONE
                binding.resultImageView.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("YoloCameraActivity", "Gagal decode gambar Base64 dari server", e)
                Toast.makeText(this, "Gagal menampilkan hasil gambar.", Toast.LENGTH_SHORT).show()
            }
            // --- AKHIR PERUBAHAN ---
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Log.e("YoloCameraActivity", "API Error: $errorMsg")
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val cameraExecutor = Executors.newSingleThreadExecutor()
            imageAnalysis?.setAnalyzer(cameraExecutor) { imageProxy ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSentTimestamp >= frameIntervalMillis) {
                    val bitmap = imageProxy.toBitmap()
                    if (bitmap != null) {
                        viewModel.detectObjectFromBitmap(bitmap)
                    }
                    lastSentTimestamp = currentTime
                }
                imageProxy.close()
            }

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("YoloCameraActivity", "Gagal melakukan bind use case kamera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 90, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val rotationDegrees = this.imageInfo.rotationDegrees.toFloat()

        if (rotationDegrees == 0f) {
            return bitmap
        }

        val matrix = Matrix()
        matrix.postRotate(rotationDegrees)

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        bitmap.recycle()
        return rotatedBitmap
    }
}
