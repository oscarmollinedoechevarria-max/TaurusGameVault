package com.example.taurusgamevault.adapters

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class GamePickerAdapter(
    private var list: List<Game>,
    // block for game click
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

        holder.gameNameTextView.text = item.name
        holder.releaseDateTextView.text = item.release_date ?: "Unknown"
        holder.descriptionTextView.text = item.description ?: "No description available"

        // Set checkbox state
        holder.gameCheckBox.isChecked = selectedGames.contains(item)

        // Set overlay visibility based on selection
        holder.selectionOverlay.visibility = if (selectedGames.contains(item)) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Load game image
        holder.productImageView.load(item.game_image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            networkCachePolicy(CachePolicy.ENABLED)

            listener(
                onSuccess = { _, result ->
                    if (holder.productImageView.width > 0 && holder.productImageView.height > 0) {
                        applyImageMatrix(holder.productImageView, result.drawable)
                    } else {
                        holder.productImageView.post {
                            applyImageMatrix(holder.productImageView, result.drawable)
                        }
                    }
                },
                onError = { _, _ ->
                    holder.productImageView.scaleType = ImageView.ScaleType.FIT_XY
                },
                onStart = { _ ->
                    holder.productImageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            )
        }

        // Handle clicks on the entire card
        holder.container.setOnClickListener {
            onGameClick(item)
        }

        // Handle checkbox clicks
        holder.gameCheckBox.setOnClickListener {
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

    //custom image position
    private fun applyImageMatrix(imageView: ImageView, drawable: Drawable) {
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
        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)
        val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val gameCheckBox: CheckBox = itemView.findViewById(R.id.gameCheckBox)
        val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
        val container: ConstraintLayout = itemView.findViewById(R.id.backgroundView)
    }
}