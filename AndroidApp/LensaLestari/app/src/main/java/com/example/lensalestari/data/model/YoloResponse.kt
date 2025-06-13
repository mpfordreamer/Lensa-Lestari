package com.example.lensalestari.data.model

// Import yang diperlukan
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class YoloResponse(
    @SerializedName("detections")
    val detections: List<Detection>,

    // MENANGKAP GAMBAR YANG SUDAH JADI DARI SERVER
    @SerializedName("image_with_boxes_base64")
    val imageWithBoxesBase64: String,

    // Menangkap field lainnya jika perlu
    @SerializedName("reward_message")
    val rewardMessage: String,

    @SerializedName("total_points")
    val totalPoints: Int,

    @SerializedName("success")
    val success: Boolean? = null
) : Parcelable


// Setiap item Detection juga harus bisa diparsel
@Parcelize
data class Detection(
    @SerializedName("class")
    val className: String,

    @SerializedName("confidence")
    val confidence: Float,

    @SerializedName("bbox")
    val boundingBox: List<Int>
) : Parcelable
