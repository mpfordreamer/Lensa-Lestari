package com.example.lensalestari.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lensalestari.data.model.PointHistoryItem
import com.example.lensalestari.databinding.ItemRewardHistoryBinding // <-- PASTIKAN NAMA FILE INI SESUAI DENGAN LAYOUT ITEM ANDA

/**
 * Adapter yang sudah diperbaiki menggunakan ListAdapter.
 * Ini akan menyelesaikan kedua error yang Anda alami.
 */
// 1. Mewarisi ListAdapter, akan memberikan fungsi 'submitList()' secara otomatis.
// 2. Constructor-nya kosong, sehingga tidak perlu parameter 'historyList' saat dibuat.
class RewardHistoryAdapter : ListAdapter<PointHistoryItem, RewardHistoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    class MyViewHolder(private val binding: ItemRewardHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PointHistoryItem) {
            binding.tvActionName.text = item.actionName
            // Pastikan ID di layout item Anda sesuai (misal: tvActionName, tvPoints, tvTimestamp)
            binding.tvPointsAdded.text = "+${item.points} Poin"
            binding.tvActionTimestamp.text = item.timestamp
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRewardHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PointHistoryItem>() {
            override fun areItemsTheSame(oldItem: PointHistoryItem, newItem: PointHistoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PointHistoryItem, newItem: PointHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}