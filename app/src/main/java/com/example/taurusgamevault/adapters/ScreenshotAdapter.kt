package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.ItemScreenshotBinding

class ScreenshotAdapter(
    private var items: List<String> = emptyList(),
    // block for image click
    private val onImageClick: (String) -> Unit,
    // block for edit screenshot
    private val onEditScreenshot: (position: Int) -> Unit
) : RecyclerView.Adapter<ScreenshotViewHolder>() {

    private var isEditMode = false

    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    fun updateScreenshots(newList: List<String>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotViewHolder {
        val binding = ItemScreenshotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScreenshotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
        holder.bind(items[position], isEditMode, onImageClick, onEditScreenshot)
    }

    override fun getItemCount(): Int = items.size
}

class ScreenshotViewHolder(
    private val binding: ItemScreenshotBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        imageUrl: String,
        isEditMode: Boolean,
        onImageClick: (String) -> Unit,
        onEditScreenshot: (Int) -> Unit
    ) {
        binding.screenshotImageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }

        binding.editScreenshot.visibility = if (isEditMode) View.VISIBLE else View.GONE

        binding.editScreenshot.setOnClickListener {
            onEditScreenshot(bindingAdapterPosition)
        }

        binding.root.setOnClickListener {
            onImageClick(imageUrl)
        }
    }
}