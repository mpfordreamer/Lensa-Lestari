package com.example.lensalestari.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentItem(
    // @SerializedName ditambahkan agar siap jika data diambil dari API
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("imageName") // Sesuaikan dengan key di JSON jika ada
    val imageName: String? = null,

    @SerializedName("fullContent") // Sesuaikan dengan key di JSON jika ada
    val fullContent: String? = null
) : Parcelable
