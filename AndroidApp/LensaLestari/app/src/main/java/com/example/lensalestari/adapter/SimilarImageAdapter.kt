package com.example.lensalestari.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lensalestari.R
import com.example.lensalestari.data.model.SimilarImage

class SimilarImageAdapter(private val items: List<SimilarImage>) :
    RecyclerView.Adapter<SimilarImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgSimilar: ImageView = view.findViewById(R.id.img_similar)
        val tvLabel: TextView = view.findViewById(R.id.tv_similar_label)
        val tvDistance: TextView = view.findViewById(R.id.tv_similar_distance)
        // TAMBAHKAN KEMBALI INI
        val tvRank: TextView = view.findViewById(R.id.tv_similar_rank)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_similar_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvLabel.text = item.label

        // TAMBAHKAN KEMBALI BARIS INI
        holder.tvRank.text = "Rank: ${item.rank}"

        holder.tvDistance.text = "Distance: ${String.format("%.2f", item.distance)}"

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.badge_circle_none)
            .error(R.drawable.badge_circle_bronze)
            .into(holder.imgSimilar)
    }

    override fun getItemCount() = items.size
}