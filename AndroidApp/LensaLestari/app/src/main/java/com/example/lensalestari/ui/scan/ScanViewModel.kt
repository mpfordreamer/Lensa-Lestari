package com.example.lensalestari.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lensalestari.data.model.ClassificationResponse
import com.example.lensalestari.data.repository.ScanRepository
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class ScanViewModel(private val repository: ScanRepository) : ViewModel() {
    private val _classificationResult = MutableLiveData<ClassificationResponse>()
    val classificationResult: LiveData<ClassificationResponse> = _classificationResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun analyzeImageFromBitmap(bitmap: Bitmap) {
        _loading.value = true
        _error.value = null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        val reqFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val body = MultipartBody.Part.createFormData("image", "image.jpg", reqFile)

        repository.analyzeImage(body).enqueue(object : Callback<ClassificationResponse> {
            override fun onResponse(
                call: Call<ClassificationResponse>,
                response: Response<ClassificationResponse>
            ) {
                _loading.value = false
                if (response.isSuccessful) {
                    _classificationResult.value = response.body()
                } else {
                    _error.value = response.message()
                }
            }

            override fun onFailure(call: Call<ClassificationResponse>, t: Throwable) {
                _loading.value = false
                _error.value = t.message
            }
        })
    }
}
