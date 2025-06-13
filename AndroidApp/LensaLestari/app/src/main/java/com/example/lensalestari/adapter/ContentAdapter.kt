package com.example.lensalestari.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lensalestari.R
import com.example.lensalestari.data.model.ContentItem
import com.example.lensalestari.databinding.ItemArticleCardBinding

class ContentAdapter(private val contentList: List<ContentItem>) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    private var onItemClickCallback: ((ContentItem) -> Unit)? = null

    fun setOnItemClickCallback(callback: (ContentItem) -> Unit) {
        this.onItemClickCallback = callback
    }

    inner class ContentViewHolder(private val binding: ItemArticleCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(content: ContentItem) {
            binding.tvArticleTitle.text = content.title

            // Load gambar dari drawable jika imageName ada
            val context = binding.root.context
            val imageResId = content.imageName?.let {
                context.resources.getIdentifier(it, "drawable", context.packageName)
            } ?: 0

            if (imageResId != 0) {
                binding.imgArticle.setImageResource(imageResId)
            } else {
                // Fallback ke placeholder kalau gambar tidak ditemukan
                binding.imgArticle.setImageResource(R.drawable.placeholder)
            }

            itemView.setOnClickListener {
                onItemClickCallback?.invoke(content)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemArticleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(contentList[position])
    }

    override fun getItemCount(): Int = contentList.size
}