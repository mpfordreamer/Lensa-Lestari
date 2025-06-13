package com.example.lensalestari.ui.reward // Anda bisa menempatkannya di package ui.reward atau yang sesuai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lensalestari.data.model.PointHistoryItem
import com.example.lensalestari.data.model.PointHistoryResponse
import com.example.lensalestari.data.repository.PointHistoryRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PointHistoryViewModel(private val repository: PointHistoryRepository) : ViewModel() {

    // Menyimpan daftar item riwayat poin
    private val _pointHistory = MutableLiveData<List<PointHistoryItem>>()
    val pointHistory: LiveData<List<PointHistoryItem>> = _pointHistory

    // Status loading untuk menampilkan/menyembunyikan progress bar
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Menyimpan pesan error jika terjadi kegagalan
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Untuk menangani paginasi
    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingData = false

    /**
     * Fungsi untuk mengambil data riwayat poin dari repository.
     * @param token Token otorisasi pengguna.
     */
    fun fetchPointHistory(token: String) {
        // Mencegah request berulang jika sedang loading atau sudah halaman terakhir
        if (isLoadingData || isLastPage) return

        _isLoading.value = true
        isLoadingData = true
        _error.value = null

        repository.getPointHistory(token, currentPage).enqueue(object : Callback<PointHistoryResponse> {
            override fun onResponse(
                call: Call<PointHistoryResponse>,
                response: Response<PointHistoryResponse>
            ) {
                _isLoading.value = false
                isLoadingData = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Menggabungkan data lama dengan data baru (untuk infinite scroll)
                        val currentList = _pointHistory.value.orEmpty()
                        val newList = body.data
                        _pointHistory.value = currentList + newList

                        // Cek apakah ini halaman terakhir
                        if (currentPage >= body.pages) {
                            isLastPage = true
                        }
                        currentPage++ // Naikkan nomor halaman untuk request selanjutnya
                    } else {
                        _error.value = "Tidak ada data yang diterima."
                    }
                } else {
                    _error.value = "Gagal memuat data: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<PointHistoryResponse>, t: Throwable) {
                _isLoading.value = false
                isLoadingData = false
                _error.value = "Terjadi kesalahan: ${t.message}"
            }
        })
    }

    /**
     * Fungsi untuk mereset data dan memuat ulang dari halaman pertama.
     */
    fun refreshHistory(token: String) {
        currentPage = 1
        isLastPage = false
        isLoadingData = false
        _pointHistory.value = emptyList() // Kosongkan list
        fetchPointHistory(token)
    }
}