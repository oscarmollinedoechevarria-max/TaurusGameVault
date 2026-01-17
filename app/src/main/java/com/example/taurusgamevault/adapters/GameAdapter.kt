package com.example.taurusgamevault.adapters

import android.R.attr.gravity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.R
import com.example.taurusgamevault.Model.room.entities.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameAdapter(private var list: List<Game>, context: Context): RecyclerView.Adapter<GameAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_card,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.gameNameTextView.text = item.name

        holder.releaseDateTextView.text = item.release_date

        holder.descriptionTextView.text = item.description

        holder.productImageView.load(item.game_image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            listener(
                onSuccess = { _, result ->
                    holder.productImageView.scaleType = ImageView.ScaleType.MATRIX

                    val drawable = result.drawable
                    val imageWidth = drawable.intrinsicWidth.toFloat()
                    val imageHeight = drawable.intrinsicHeight.toFloat()
                    val viewWidth = holder.productImageView.width.toFloat()
                    val viewHeight = holder.productImageView.height.toFloat()

                    val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)

                    val matrix = Matrix().apply {
                        postScale(scale, scale)
                        postTranslate((viewWidth - imageWidth * scale) / 2f, 0f)
                    }

                    holder.productImageView.imageMatrix = matrix
                },
                onError = { _, _ ->
                    holder.productImageView.scaleType = ImageView.ScaleType.FIT_XY
                },
                onStart = { _ ->
                    holder.productImageView.scaleType = ImageView.ScaleType.FIT_XY
                }

            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)

        val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDateTextView)

        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)

    }
}