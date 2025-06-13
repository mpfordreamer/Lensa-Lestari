package com.example.lensalestari.data.model

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("data")
    val data: List<HistoryItem>,

    @SerializedName("total")
    val total: Int
)

data class HistoryItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("confidence")
    val confidence: Double,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("image_base64")
    val imageBase64: String,

    @SerializedName("predicted_class")
    val predictedClass: String
)
