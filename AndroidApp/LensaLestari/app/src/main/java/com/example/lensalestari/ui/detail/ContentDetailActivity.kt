package com.example.lensalestari.ui.detail

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lensalestari.data.model.ContentItem
import com.example.lensalestari.databinding.ActivityContentDetailBinding
import com.example.lensalestari.R

class ContentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentDetailBinding

    companion object {
        const val EXTRA_CONTENT = "extra_content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val content = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_CONTENT, ContentItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONTENT)
        }

        if (content != null) {
            binding.tvDetailTitle.text = content.title
            binding.tvDetailContent.text = content.fullContent

            // LOAD GAMBAR DARI DRAWABLE
            val imageResId = content.imageName?.let {
                resources.getIdentifier(it, "drawable", packageName)
            } ?: 0

            if (imageResId != 0) {
                binding.imgDetail.setImageResource(imageResId)
            } else {
                binding.imgDetail.setImageResource(R.drawable.placeholder)
            }
        }
    }
}
