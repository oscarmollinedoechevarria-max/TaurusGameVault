package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.R

class TagsAdapter(
    private val items: List<Tag>,
    private val onItemClick: (Tag) -> Unit = {},
    private val selectable: Boolean = false,
    private val onNavigate: ((Tag) -> Unit)? = null
) : RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    private val selectedTags = mutableSetOf<Tag>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgItem)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val container: View = view.findViewById(R.id.backgroundView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plataformimportcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.title.text = item.name

        holder.image.load(item.image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }

        if (selectable) {
            val isSelected = item in selectedTags
            holder.container.isSelected = isSelected
            holder.container.alpha = if (isSelected) 1f else 0.5f
        } else {
            holder.container.isSelected = false
            holder.container.alpha = 1f
        }

        holder.container.setOnClickListener {
            if (selectable) {
                if (item in selectedTags) selectedTags.remove(item) else selectedTags.add(item)
                val current = holder.adapterPosition
                if (current != RecyclerView.NO_ID.toInt()) notifyItemChanged(current)
            }
            onItemClick(item)
            onNavigate?.invoke(item)
        }
    }

    override fun getItemCount() = items.size

    fun getSelectedTags(): Set<Tag> = selectedTags.toSet()

    fun clearSelection() {
        val changed = selectedTags.toList()
        selectedTags.clear()
        changed.forEach { tag ->
            val index = items.indexOf(tag)
            if (index != -1) notifyItemChanged(index)
        }
    }
}