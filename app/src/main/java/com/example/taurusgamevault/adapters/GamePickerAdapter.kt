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
import com.google.android.material.card.MaterialCardView

class GamePickerAdapter(
    private var list: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<GamePickerAdapter.ViewHolder>() {

    private val selectedGames = mutableSetOf<Game>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_picker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val isSelected = selectedGames.contains(item)

        holder.gameNameTextView.text = item.name
        holder.releaseDateTextView.text = item.release_date ?: "Unknown"
        holder.descriptionTextView.text = item.description ?: "No description available"

        // overlay, stroke
        holder.cardContainer.isChecked = isSelected
        holder.cardContainer.isChecked = isSelected
        holder.ivSelectionCheck.isVisible = isSelected

        // Image load
        holder.productImageView.load(item.game_image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            networkCachePolicy(CachePolicy.ENABLED)

            listener(onSuccess = { _, result ->
                holder.productImageView.post {
                    applyImageMatrix(holder.productImageView, result.drawable)
                }
            })
        }

        // Handle clicks on the entire card
        holder.cardContainer.setOnClickListener {
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardContainer)

        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)

        val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)

        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)

        val ivSelectionCheck: ImageView = itemView.findViewById(R.id.ivSelectionCheck)
    }
}