package com.example.lensalestari.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lensalestari.data.repository.AuthRepository
import com.example.lensalestari.data.repository.HistoryRepository
import com.example.lensalestari.data.repository.PointHistoryRepository
import com.example.lensalestari.data.repository.ScanRepository
import com.example.lensalestari.ui.auth.AuthViewModel
import com.example.lensalestari.ui.home.HomeViewModel
import com.example.lensalestari.ui.reward.PointHistoryViewModel
import com.example.lensalestari.ui.scan.ScanViewModel
import com.example.lensalestari.ui.scan.YoloViewModel

class ViewModelFactory(
    private val repository: Any // Gunakan 'Any' agar fleksibel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                // Pastikan repository yang diberikan adalah tipe yang benar
                if (repository is AuthRepository) {
                    AuthViewModel(repository) as T
                } else {
                    throw IllegalArgumentException("AuthRepository required for AuthViewModel")
                }
            }
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                if (repository is ScanRepository) {
                    ScanViewModel(repository) as T
                } else {
                    throw IllegalArgumentException("ScanRepository required for ScanViewModel")
                }
            }
            modelClass.isAssignableFrom(YoloViewModel::class.java) -> {
                if (repository is ScanRepository) {
                    YoloViewModel(repository) as T
                } else {
                    throw IllegalArgumentException("ScanRepository required for YoloViewModel")
                }
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                if (repository is HistoryRepository) { // <-- PERIKSA HistoryRepository
                    HomeViewModel(repository) as T      // <-- Lewatkan HistoryRepository
                } else {
                    // Pesan error juga diperbaiki
                    throw IllegalArgumentException("HistoryRepository required for HomeViewModel")
                }
            }
            modelClass.isAssignableFrom(PointHistoryViewModel::class.java) -> {
                if (repository is PointHistoryRepository) {
                    PointHistoryViewModel(repository) as T
                } else {
                    throw IllegalArgumentException("PointHistoryRepository required for PointHistoryViewModel")
                }
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}