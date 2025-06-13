package com.example.lensalestari.data.repository

import com.example.lensalestari.data.api.ApiService
import com.example.lensalestari.data.model.PointHistoryResponse
import retrofit2.Call

/**
 * Repository yang bertanggung jawab untuk mengambil data riwayat poin dari API.
 */
class PointHistoryRepository(private val apiService: ApiService) {

    /**
     * Mengambil riwayat perolehan poin dari server.
     * @param token Token otorisasi pengguna.
     * @param page Nomor halaman yang ingin diambil.
     * @return Call<PointHistoryResponse>
     */
    fun getPointHistory(token: String, page: Int): Call<PointHistoryResponse> {
        // Menambahkan "Bearer " pada token sesuai standar otorisasi
        return apiService.getPointHistory("Bearer $token", page)
    }

    companion object {
        @Volatile
        private var instance: PointHistoryRepository? = null

        /**
         * Mendapatkan satu instance dari PointHistoryRepository (Singleton Pattern).
         */
        fun getInstance(apiService: ApiService): PointHistoryRepository =
            instance ?: synchronized(this) {
                instance ?: PointHistoryRepository(apiService).also { instance = it }
            }
    }
}
