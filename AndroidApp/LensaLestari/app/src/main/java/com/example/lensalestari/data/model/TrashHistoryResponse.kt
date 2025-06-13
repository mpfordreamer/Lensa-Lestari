package com.example.lensalestari.data.model

import com.google.gson.annotations.SerializedName

/**
 * Merepresentasikan respons dari API yang berisi rekap jumlah sampah.
 * Digunakan untuk endpoint /trash-history.
 */
data class TrashHistoryResponse(

    @SerializedName("anorganik")
    val anorganik: Int,

    @SerializedName("organik")
    val organik: Int,

    @SerializedName("total")
    val total: Int
)