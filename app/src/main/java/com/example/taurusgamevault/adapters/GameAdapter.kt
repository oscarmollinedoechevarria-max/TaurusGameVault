package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.database.entities.Game

class GameAdapter(private var list: List<Game>): RecyclerView.Adapter<GameAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_card,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameAdapter.ViewHolder, position: Int) {
        val item = list[position]

        holder.gameNameTextView.text = item.name

        holder.releaseDateTextView.text = item.release_date

        holder.descriptionTextView.text = item.description

//        holder.productImageView =
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