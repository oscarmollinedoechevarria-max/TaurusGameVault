package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.ItemScreenshotBinding

class ScreenshotAdapter(
    private val onImageClick: (String) -> Unit
) : ListAdapter<String, ScreenshotAdapter.ViewHolder>(ScreenshotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScreenshotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onImageClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemScreenshotBinding,
        private val onImageClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            binding.screenshotImageView.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)

                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }

            binding.root.setOnClickListener {
                onImageClick(imageUrl)
            }
        }
    }

    private class ScreenshotDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}