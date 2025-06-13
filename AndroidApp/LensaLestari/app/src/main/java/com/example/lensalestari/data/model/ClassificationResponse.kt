package com.example.lensalestari.data.model

// 1. Import Parcelable dan Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// 2. Tambahkan @Parcelize dan : Parcelable
@Parcelize
data class ClassificationResponse(
    @SerializedName("classification")
    val classification: ClassificationResult,

    @SerializedName("similar_images")
    val similarImages: List<SimilarImage>,

    @SerializedName("reward_message")
    val rewardMessage: String,

    @SerializedName("total_points")
    val totalPoints: Int
) : Parcelable

// 3. Lakukan hal yang sama untuk kelas di dalamnya
@Parcelize
data class ClassificationResult(
    @SerializedName("confidence")
    val confidence: Double,

    @SerializedName("predicted_class")
    val predictedClass: String
) : Parcelable

// 4. Lakukan hal yang sama untuk kelas ini juga
@Parcelize
data class SimilarImage(
    @SerializedName("distance")
    val distance: Double,

    @SerializedName("label")
    val label: String,

    @SerializedName("path")
    val imageUrl: String,

    @SerializedName("rank")
    val rank: Int
) : Parcelable