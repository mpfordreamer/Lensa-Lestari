package com.example.lensalestari.ui.scan

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.OutputTransform

// Pastikan Anda sudah meng-import data class YoloBox dari file-nya.
// import com.example.lensalestari.ui.scan.YoloBox


class BoundingBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxes = mutableListOf<YoloBox>()
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    @SuppressLint("UnsafeOptInUsageError")
    private var outputTransform: OutputTransform? = null

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 50f
        style = Paint.Style.FILL
    }

    @OptIn(TransformExperimental::class)
    fun setResults(
        boxes: List<YoloBox>,
        imageWidth: Int,
        imageHeight: Int,
        outputTransform: OutputTransform
    ) {
        this.boxes.clear()
        this.boxes.addAll(boxes)
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.outputTransform = outputTransform

        invalidate()
    }

    // --- MULAI PERUBAHAN DI SINI: LOGIKA PENGGAMBARAN UNTUK DEBUGGING ---
    @SuppressLint("RestrictedApi")
    @OptIn(TransformExperimental::class)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (boxes.isEmpty()) {
            return
        }

        Log.d("BoundingBoxOverlay", "Drawing ${boxes.size} boxes.")

        // Pendekatan ini secara langsung mengubah skala koordinat dari ukuran model (640x640)
        // ke ukuran View ini (BoundingBoxOverlay), mengabaikan rotasi dan rasio aspek kamera untuk sementara.
        // Ini adalah langkah debugging untuk memastikan sesuatu bisa digambar.

        val viewWidth = this.width.toFloat()
        val viewHeight = this.height.toFloat()

        // Ukuran input model yang Anda gunakan
        val modelInputWidth = 640f
        val modelInputHeight = 640f

        // Faktor skala langsung dari model ke view
        val scaleX = viewWidth / modelInputWidth
        val scaleY = viewHeight / modelInputHeight

        for (box in boxes) {
            // Buat RectF baru dengan mengalikan koordinat asli dengan faktor skala
            val scaledRect = RectF(
                box.rect.left * scaleX,
                box.rect.top * scaleY,
                box.rect.right * scaleX,
                box.rect.bottom * scaleY
            )

            // Gambar kotak yang sudah diskalakan langsung ke canvas
            canvas.drawRect(scaledRect, boxPaint)

            // Gambar label teks di atas kotak
            val labelText = "${box.label} (${String.format("%.2f", box.confidence)})"
            canvas.drawText(labelText, scaledRect.left, scaledRect.top - 10, textPaint)
        }
    }
    // --- AKHIR PERUBAHAN ---
}
