package com.example.taurusgamevault.adapters

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.ItemGamePickerBinding
import com.example.taurusgamevault.databinding.PlataformimportcardBinding
import com.google.android.material.card.MaterialCardView

class GamePickerAdapter(
    private var list: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<ItemGamePickerViewHolder>() {

    private val selectedGames = mutableSetOf<Game>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemGamePickerViewHolder {
        val binding = ItemGamePickerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemGamePickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemGamePickerViewHolder, position: Int) {
        val item = list[position]
        val isSelected = selectedGames.contains(item)

        holder.binding.gameNameTextView.text = item.name
        holder.binding.releaseDateTextView.text = item.release_date ?: "Unknown"
        holder.binding.descriptionTextView.text = item.description ?: "No description available"

        // overlay, stroke
        holder.binding.cardContainer.isChecked = isSelected
        holder.binding.cardContainer.isChecked = isSelected
        holder.binding.ivSelectionCheck.isVisible = isSelected

        // Image load
        holder.binding.productImageView.load(item.game_image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            networkCachePolicy(CachePolicy.ENABLED)

            listener(onSuccess = { _, result ->
                holder.binding.productImageView.post {
                    applyImageMatrix(holder.binding.productImageView, result.drawable)
                }
            })
        }

        // Handle clicks on the entire card
        holder.binding.cardContainer.setOnClickListener {
            onGameClick(item)
        }
    }

    override fun getItemCount(): Int {
      return list.size
    }

    fun updateSelection(selected: Set<Game>) {
        selectedGames.clear()
        selectedGames.addAll(selected)
        notifyDataSetChanged()
    }

    fun submitList(newList: List<Game>) {
        list = newList
        notifyDataSetChanged()
    }

    private fun applyImageMatrix(imageView: ImageView, drawable: Drawable) {
        if (imageView.width <= 0) return

        imageView.scaleType = ImageView.ScaleType.MATRIX

        val imageWidth = drawable.intrinsicWidth.toFloat()

        val imageHeight = drawable.intrinsicHeight.toFloat()

        val viewWidth = imageView.width.toFloat()

        val viewHeight = imageView.height.toFloat()

        val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)

        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate((viewWidth - imageWidth * scale) / 2f, 0f)
        }
        imageView.imageMatrix = matrix
    }
}

class ItemGamePickerViewHolder(val binding: ItemGamePickerBinding) :
    RecyclerView.ViewHolder(binding.root)