package com.example.taurusgamevault.adapters

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.R
import com.example.taurusgamevault.classes.SimplifiedGame
import com.example.taurusgamevault.list.gamelistdetail.GameListDetailFragmentDirections

class SimplifiedGameAdapter(
    private var list: List<SimplifiedGame>,
    context: Context,
    private val navigation: NavController,
    private val shouldBeClicable: Boolean,
    private val onRemove: ((SimplifiedGame) -> Unit)? = null
): RecyclerView.Adapter<SimplifiedGameAdapter.ViewHolder>() {

    private var isEditMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.gameNameTextView.text = item.name

        holder.releaseDateTextView.text = item.releaseDate

        holder.descriptionTextView.text = item.description

        holder.removeButton.isVisible = isEditMode
        holder.removeButton.setOnClickListener {
            onRemove?.invoke(item)
        }

        holder.productImageView.load(item.image) {
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

        holder.container.setOnClickListener {
            if (shouldBeClicable && !isEditMode) {
                navigation.navigate(
                    GameListDetailFragmentDirections
                        .actionGameListDetailFragmentToGameDetailFragment(item.gameId, false)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun submitList(newList: List<SimplifiedGame>) {
        list = newList
        notifyDataSetChanged()
    }

    fun toggleEditMode() {
        isEditMode = !isEditMode
        notifyDataSetChanged()
    }

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
        val removeButton: ImageView = itemView.findViewById(R.id.removeButton)

        val container: ConstraintLayout = itemView.findViewById(R.id.backgroundView)
    }
}