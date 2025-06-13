package com.example.lensalestari.data.repository

import com.example.lensalestari.data.api.ApiService
import com.example.lensalestari.data.model.ClassificationResponse
import com.example.lensalestari.data.model.YoloResponse
import okhttp3.MultipartBody
import retrofit2.Call

class ScanRepository(private val apiService: ApiService) {
    fun analyzeImage(image: MultipartBody.Part): Call<ClassificationResponse> {
        return apiService.analyzeImage(image)
    }

    fun detectObject(image: MultipartBody.Part): Call<YoloResponse> {
        return apiService.detectObject(image)
    }
}
