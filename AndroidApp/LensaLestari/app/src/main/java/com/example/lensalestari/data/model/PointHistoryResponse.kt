package com.example.lensalestari.data.model

import com.google.gson.annotations.SerializedName

/**
 * Merepresentasikan respons lengkap dari API riwayat poin.
 */
data class PointHistoryResponse(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("data")
    val data: List<PointHistoryItem>,

    @SerializedName("pages")
    val pages: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("total")
    val total: Int
)

/**
 * Merepresentasikan satu item dalam riwayat perolehan poin.
 */
data class PointHistoryItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("action_name")
    val actionName: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("points")
    val points: Int,

    @SerializedName("timestamp")
    val timestamp: String
)