package com.example.lensalestari.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lensalestari.data.api.ApiClient
import com.example.lensalestari.databinding.FragmentScanBinding
import com.example.lensalestari.factory.ViewModelFactory
import com.example.lensalestari.data.repository.AuthRepository
import com.example.lensalestari.data.repository.ScanRepository
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!


    // Tambahan: simpan bitmap yang dipilih user
    private var currentBitmap: Bitmap? = null

    private val scanViewModel: ScanViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // 1. Dapatkan ApiService menggunakan requireContext()
                val apiService = ApiClient.getInstance(requireContext())
                    ?: throw IllegalStateException("Base URL is not set. Cannot create ApiService.")

                // 2. Buat repository
                val repository = ScanRepository(apiService)

                // 3. Buat ViewModel
                if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
                    return ScanViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    // Request permission kamera
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startCamera()
        else Toast.makeText(requireContext(), "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
    }

    // Kamera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            currentBitmap = bitmap
            scanViewModel.analyzeImageFromBitmap(bitmap) // <-- Langsung upload
        }
    }

    // Galeri
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = uriToBitmap(uri)
            currentBitmap = bmp
            bmp?.let { scanViewModel.analyzeImageFromBitmap(it) } // <-- Langsung upload
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCamera.setOnClickListener { checkCameraPermissionAndLaunch() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnStartYoloScan.setOnClickListener { // Gunakan ID baru dari XML
            val intent = Intent(requireContext(), YoloCameraActivity::class.java)
            startActivity(intent)
        }

        setupObservers()
    }

    private fun setupObservers() {
        scanViewModel.classificationResult.observe(viewLifecycleOwner) { result ->
            // LOG 1: Cek apakah observer ini terpanggil dan apa isinya
            Log.d("ScanFragment_Debug", "Observer terpanggil! Data result: $result")

            if (result != null) {
                val tempImageUri = currentBitmap?.let { saveBitmapToCache(requireContext(), it) }

                if (tempImageUri != null) {
                    // LOG 2: Cek data sesaat sebelum dikirim
                    Log.d("ScanFragment_Debug", "Data yang akan dikirim TIDAK NULL. Memulai activity...")

                    val intent = Intent(requireContext(), ResultDetailActivity::class.java).apply {
                        putExtra("extra_result", result)
                        putExtra("extra_image_uri", tempImageUri.toString())
                    }
                    startActivity(intent)
                } else {
                    Log.e("ScanFragment_Debug", "Gagal menyimpan gambar ke cache, tidak jadi pindah activity.")
                    Toast.makeText(requireContext(), "Gagal memproses gambar.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // LOG 3: Jika data dari ViewModel ternyata null
                Log.e("ScanFragment_Debug", "Observer terpanggil TAPI data result NULL!")
            }
        }

        scanViewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        scanViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // ...
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() { cameraLauncher.launch(null) }
    private fun startGallery() { galleryLauncher.launch("image/*") }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "JPEG_${timeStamp}_"
        return try {
            val tempFile = File.createTempFile(fileName, ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
