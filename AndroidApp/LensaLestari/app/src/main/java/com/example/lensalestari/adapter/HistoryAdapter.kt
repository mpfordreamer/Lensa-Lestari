package com.example.lensalestari.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lensalestari.R
import com.example.lensalestari.data.model.HistoryItem
import com.example.lensalestari.databinding.ItemHistoryBinding
import kotlin.io.encoding.ExperimentalEncodingApi

// Ganti com.example.app dengan package aplikasi Anda

class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        @OptIn(ExperimentalEncodingApi::class)
        fun bind(item: HistoryItem) {
            binding.tvHistoryClass.text = item.predictedClass
            binding.tvHistoryDate.text = item.createdAt // Anda mungkin perlu memformat tanggal ini

            try {
                val imageBytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                Glide.with(itemView.context)
                    .asBitmap()
                    .load(imageBytes)
                    .into(binding.ivHistoryImage)
            } catch (e: Exception) {
                // Handle error, misalnya tampilkan placeholder
                binding.ivHistoryImage.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryItem>() {
            override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}