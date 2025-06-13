package com.example.lensalestari.ui.scan

import android.graphics.RectF

/**
 * Data class ini mewakili satu kotak pembatas yang SIAP untuk digambar.
 * Ia mengambil data mentah dari server dan mengubahnya ke format yang ramah UI.
 */
data class YoloBox(
    val rect: RectF,
    val label: String,
    val confidence: Float
)