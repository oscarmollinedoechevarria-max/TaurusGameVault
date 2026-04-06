package com.example.taurusgamevault.adapters

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil.request.CachePolicy
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.R
import coil.load
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.list.gamelist.GameListFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ListGameAdapter(
    private var list: List<GameList>,
    private val context: Context,
    private val navigation: NavController,
    private val scope: CoroutineScope
): RecyclerView.Adapter<ListGameAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.listNameTextView.text = item.name

        holder.listDescriptionTextView.text = item.description ?: ""

        holder.listBannerImageView.load(item.image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            networkCachePolicy(CachePolicy.ENABLED)

            listener(
                onSuccess = { _, result ->
                    if (holder.listBannerImageView.width > 0 && holder.listBannerImageView.height > 0) {
                        applyImageMatrix(holder.listBannerImageView, result.drawable)
                    } else {
                        holder.listBannerImageView.post {
                            applyImageMatrix(holder.listBannerImageView, result.drawable)
                        }
                    }
                },
                onError = { _, _ ->
                    holder.listBannerImageView.scaleType = ImageView.ScaleType.FIT_XY
                },
                onStart = { _ ->
                    holder.listBannerImageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            )
        }

        // navigation to list detail
        holder.container.setOnClickListener {
            navigation.navigate(
                GameListFragmentDirections.actionGameListFragmentToGameListDetailFragment(item.list_id, item.name, false)
            )
        }

        // context menu
        holder.container.setOnLongClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.game_menu_context)

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_edit) {
                    navigation.navigate(
                        GameListFragmentDirections.actionGameListFragmentToGameListDetailFragment(item.list_id, item.name, true)
                    )
                    true
                } else if (menuItem.itemId == R.id.action_delete) {
                    scope.launch {
                        Repository.deleteList(context, item)
                    }
                    Toast.makeText(context, "List deleted successfully", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    false
                }
            }
            popup.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return list.size
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
        val listNameTextView: TextView = itemView.findViewById(R.id.listNameTextView)

        val listDescriptionTextView: TextView = itemView.findViewById(R.id.listDescriptionTextView)

        val listBannerImageView: ImageView = itemView.findViewById(R.id.listBannerImageView)

        val container: ConstraintLayout = itemView.findViewById(R.id.backgroundView)
    }
}