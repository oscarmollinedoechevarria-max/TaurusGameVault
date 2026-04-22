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
import com.example.taurusgamevault.databinding.GameCardBinding
import com.example.taurusgamevault.databinding.PlataformimportcardBinding
import com.example.taurusgamevault.list.gamelistdetail.GameListDetailFragmentDirections

class SimplifiedGameAdapter(
    private var list: List<SimplifiedGame>,
    context: Context,
    private val navigation: NavController,
    private val shouldBeClicable: Boolean,
    //block for remove button
    private val onRemove: ((SimplifiedGame) -> Unit)? = null
): RecyclerView.Adapter<SimplifiedGameViewHolder>() {

    private var isEditMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimplifiedGameViewHolder {
        val binding = GameCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SimplifiedGameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimplifiedGameViewHolder, position: Int) {
        val item = list[position]

        holder.binding.gameNameTextView.text = item.name

        holder.binding.releaseDateTextView.text = item.releaseDate

        holder.binding.descriptionTextView.text = item.description

        holder.binding.removeButton.isVisible = isEditMode
        holder.binding.removeButton.setOnClickListener {
            onRemove?.invoke(item)
        }

        holder.binding.productImageView.load(item.image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            networkCachePolicy(CachePolicy.ENABLED)

            listener(
                onSuccess = { _, result ->
                    if (holder.binding.productImageView.width > 0 && holder.binding.productImageView.height > 0) {
                        applyImageMatrix(holder.binding.productImageView, result.drawable)
                    } else {
                        holder.binding.productImageView.post {
                            applyImageMatrix(holder.binding.productImageView, result.drawable)
                        }
                    }
                },
                onError = { _, _ ->
                    holder.binding.productImageView.scaleType = ImageView.ScaleType.FIT_XY
                },
                onStart = { _ ->
                    holder.binding.productImageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            )
        }

        // navigation to game detail if should be clickable
        holder.binding.backgroundView.setOnClickListener {
            if (shouldBeClicable && !isEditMode) {
                navigation.navigate(
                    GameListDetailFragmentDirections
                        .actionGameListDetailFragmentToGameDetailFragment(item.gameId, item.name, false)
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
}

//view holder with biding, better and more secure than FindByID for simplified game
class SimplifiedGameViewHolder(val binding: GameCardBinding) : RecyclerView.ViewHolder(binding.root)