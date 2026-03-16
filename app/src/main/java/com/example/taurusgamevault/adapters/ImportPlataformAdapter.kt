package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.classes.ItemPlataformImport

class ImportPlataformAdapter(
    private val items: List<ItemPlataformImport>,
    private val onClick: (ItemPlataformImport) -> Unit
) : RecyclerView.Adapter<ImportPlataformAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgItem)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val arrow: ImageView = view.findViewById(R.id.imgArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plataformimportcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageRes)
        holder.title.text = item.title
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}