package com.example.lensalestari.data.repository

import com.example.lensalestari.data.api.ApiService
import com.example.lensalestari.data.model.HistoryResponse
import com.example.lensalestari.data.model.TrashHistoryResponse
import retrofit2.Call

class HistoryRepository(private val apiService: ApiService) {

    fun getHistory(token: String, page: Int): Call<HistoryResponse> {
        return apiService.getAnalysisHistory(token, page)
    }

    fun getTrashHistory(token: String): Call<TrashHistoryResponse> {
        return apiService.getTrashHistory(token)
    }

    // Anda bisa menggunakan companion object untuk membuat instance, sama seperti di jawaban saya sebelumnya
    companion object {
        @Volatile
        private var instance: HistoryRepository? = null

        fun getInstance(apiService: ApiService): HistoryRepository =
            instance ?: synchronized(this) {
                instance ?: HistoryRepository(apiService).also { instance = it }
            }
    }
}
