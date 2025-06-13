package com.example.lensalestari.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lensalestari.data.model.HistoryItem
import com.example.lensalestari.data.model.HistoryResponse
import com.example.lensalestari.data.model.TrashHistoryResponse // <-- IMPORT BARU
import com.example.lensalestari.data.repository.HistoryRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Asumsi HomeViewModel Anda sebelumnya seperti ini
class HomeViewModel(private val repository: HistoryRepository) : ViewModel() {

    // LiveData untuk daftar riwayat (sudah ada)
    private val _historyList = MutableLiveData<List<HistoryItem>>()
    val historyList: LiveData<List<HistoryItem>> = _historyList

    // BARU: LiveData untuk statistik sampah
    private val _trashStats = MutableLiveData<TrashHistoryResponse>()
    val trashStats: LiveData<TrashHistoryResponse> = _trashStats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Mengambil semua data yang dibutuhkan untuk Halaman Home.
     * @param token Token otorisasi pengguna.
     */
    fun fetchAllHomeData(token: String) {
        _loading.value = true
        fetchAnalysisHistory(token) // Panggil fungsi untuk mengambil daftar riwayat
        fetchTrashStats(token)      // Panggil fungsi untuk mengambil statistik
    }

    private fun fetchAnalysisHistory(token: String) {
        repository.getHistory(token, 1).enqueue(object : Callback<HistoryResponse> {
            override fun onResponse(call: Call<HistoryResponse>, response: Response<HistoryResponse>) {
                _loading.value = false // Set loading false di sini atau setelah kedua panggilan selesai
                if (response.isSuccessful) {
                    _historyList.value = response.body()?.data
                } else {
                    _error.value = "Gagal memuat riwayat: ${response.message()}"
                }
            }
            override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                _loading.value = false
                _error.value = t.message
            }
        })
    }

    private fun fetchTrashStats(token: String) {
        repository.getTrashHistory(token).enqueue(object : Callback<TrashHistoryResponse> {
            override fun onResponse(call: Call<TrashHistoryResponse>, response: Response<TrashHistoryResponse>) {
                if(response.isSuccessful) {
                    _trashStats.value = response.body()
                } else {
                    _error.value = "Gagal memuat statistik: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<TrashHistoryResponse>, t: Throwable) {
                // Jangan set loading false di sini agar tidak konflik
                _error.value = t.message
            }
        })
    }
}