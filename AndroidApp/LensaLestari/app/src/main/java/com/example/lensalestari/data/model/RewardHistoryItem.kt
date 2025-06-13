package com.example.lensalestari.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class untuk merepresentasikan satu item dalam daftar riwayat perolehan poin.
 * Menggunakan @Parcelize agar mudah dikirim antar activity/fragment jika diperlukan.
 */
@Parcelize
data class RewardHistoryItem(
    val actionName: String,
    val points: Int,
    val timestamp: String,
    val iconName: String // Kunci untuk menentukan ikon mana yang akan ditampilkan, misal: "scan", "quiz"
) : Parcelable