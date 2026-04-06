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
import com.example.taurusgamevault.databinding.PlataformimportcardBinding

class TagsAdapter(
    private val items: List<Tag>,
    private val onItemClick: (Tag) -> Unit = {},
    private val selectable: Boolean = false,
    private val onNavigate: ((Tag) -> Unit)? = null
) : RecyclerView.Adapter<TagsViewHolder>() {
    private val selectedTags = mutableSetOf<Tag>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        val binding = PlataformimportcardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TagsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        val item = items[position]

        // pass data and click logic to viewHolder
        holder.bind(item, selectable, selectedTags.contains(item)) {
            handleSelection(item, holder.bindingAdapterPosition)
        }
    }

    // handle selection logic and trigger callbacks
    private fun handleSelection(item: Tag, position: Int) {
        if (selectable) {
            if (item in selectedTags) {
                selectedTags.remove(item)
            } else {
                selectedTags.add(item)
            }

            // refresh only the clicked item
            notifyItemChanged(position)
        }
        onItemClick(item)
        onNavigate?.invoke(item)
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedTags(): Set<Tag> = selectedTags.toSet()

    fun clearSelection() {
        val changedIndices = selectedTags.map { items.indexOf(it) }
        selectedTags.clear()
        changedIndices.forEach { if (it != -1) notifyItemChanged(it) }
    }
}

class TagsViewHolder(
    private val binding: PlataformimportcardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: Tag,
        selectable: Boolean,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        binding.tvTitle.text = item.name

        binding.imgItem.load(item.image) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
        }

        // update ui based on selection state
        if (selectable) {
            binding.backgroundView.isSelected = isSelected
            binding.backgroundView.alpha = if (isSelected) 1f else 0.5f
        } else {
            binding.backgroundView.isSelected = false
            binding.backgroundView.alpha = 1f
        }

        binding.backgroundView.setOnClickListener { onClick() }
    }
}