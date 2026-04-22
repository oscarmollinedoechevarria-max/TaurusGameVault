package com.example.taurusgamevault.adapters

import android.R.attr.gravity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import coil.request.CachePolicy
import coil.size.Scale
import coil.util.CoilUtils.result
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.R
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.databinding.GameCardBinding
import com.example.taurusgamevault.databinding.ListCardBinding
import com.example.taurusgamevault.mainscreen.MainFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameAdapter(
    private var list: List<Game>,
    private val context: Context,
    private val navigation: NavController,
    private val scope: CoroutineScope,
    // block for game click
    private val onItemClick: (Game) -> Unit = { game ->
        navigation.navigate(
            MainFragmentDirections.actionMainFragmentToGameDetailFragment(
                gameId = game.game_id, editMode = false, gameName = game.name
            )
        )
    },
    private val showContextMenu: Boolean = true
) : RecyclerView.Adapter<GameViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = GameCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val item = list[position]

        holder.binding.gameNameTextView.text = item.name

        holder.binding.releaseDateTextView.text = item.release_date

        holder.binding.descriptionTextView.text = item.description


        // custom position for image in card
        holder.binding.productImageView.load(item.game_image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
//            transformations(ImageListCache())*
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

        holder.binding.backgroundView.setOnClickListener {
            onItemClick(item)
        }

        //card context menu
        if (showContextMenu) {
            holder.binding.backgroundView.setOnLongClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.game_menu_context)

                popup.setOnMenuItemClickListener { menuItem ->
                    //edit
                    if (menuItem.itemId == R.id.action_edit) {
                        navigation.navigate(
                            MainFragmentDirections
                                .actionMainFragmentToGameDetailFragment(gameId = item.game_id, editMode = true, gameName = item.name)
                        )
                        true
                    }
                    //delete
                    else if (menuItem.itemId == R.id.action_delete) {
                        scope.launch {
                            Repository.deleteGame(context, item)
                        }
                        Toast.makeText(context, "Game deleted successfully", Toast.LENGTH_SHORT).show()
                        true
                    } else {
                        false
                    }
                }
                popup.show()
                true
            }
        } else {
            holder.binding.backgroundView.setOnLongClickListener(null)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    //custom image postion
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

class GameViewHolder(val binding: GameCardBinding) : RecyclerView.ViewHolder(binding.root)