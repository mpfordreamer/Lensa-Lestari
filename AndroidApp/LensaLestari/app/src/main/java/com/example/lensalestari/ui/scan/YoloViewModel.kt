package com.example.lensalestari.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lensalestari.data.model.YoloResponse
import com.example.lensalestari.data.repository.ScanRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class YoloViewModel(private val repository: ScanRepository) : ViewModel() {
    private val _yoloResult = MutableLiveData<YoloResponse>()
    val yoloResult: LiveData<YoloResponse> = _yoloResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var isRequestInProgress = false

    fun detectObjectFromBitmap(bitmap: Bitmap) {
        if (isRequestInProgress) return

        isRequestInProgress = true
        // PERBAIKAN: Gunakan postValue karena dipanggil dari background thread
        _error.postValue(null)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val requestBody = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", "frame.jpg", requestBody)

        // Catatan: Callback dari Retrofit.enqueue() sebenarnya sudah berjalan di Main Thread.
        // Namun, untuk konsistensi dan keamanan, menggunakan postValue di sini tetap pilihan yang baik.
        repository.detectObject(body).enqueue(object : Callback<YoloResponse> {
            override fun onResponse(call: Call<YoloResponse>, response: Response<YoloResponse>) {
                if (response.isSuccessful) {
                    // PERBAIKAN: Gunakan postValue
                    _yoloResult.postValue(response.body())
                } else {
                    // PERBAIKAN: Gunakan postValue
                    _error.postValue("Error: ${response.code()}")
                }
                isRequestInProgress = false
            }

            override fun onFailure(call: Call<YoloResponse>, t: Throwable) {
                // PERBAIKAN: Gunakan postValue
                _error.postValue(t.message)
                isRequestInProgress = false
            }
        })
    }
}