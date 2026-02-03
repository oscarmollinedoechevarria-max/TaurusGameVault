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
import android.widget.TextView
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
import com.example.taurusgamevault.mainscreen.MainFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameAdapter(private var list: List<Game>, context: Context, private val navigation: NavController): RecyclerView.Adapter<GameAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_card,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        //TODO: fix editmode cache screenshots
        //tODO: date pickers on detail edit mode
        //todo: edit mode main screen

        holder.gameNameTextView.text = item.name

        holder.releaseDateTextView.text = item.release_date

        holder.descriptionTextView.text = item.description

        //TODO: implement image cache locally
        holder.productImageView.load(item.game_image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
//            transformations(ImageListCache())*
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
            navigation.navigate(
                MainFragmentDirections
                    .actionMainFragmentToGameDetailFragment(gameId = item.game_id)
            )
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
        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)

        val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)

        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)

        val container: ConstraintLayout = itemView.findViewById(R.id.backgroundView)
    }
}