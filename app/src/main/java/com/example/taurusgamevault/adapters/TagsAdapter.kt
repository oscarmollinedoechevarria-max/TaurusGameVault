package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.PlataformimportcardBinding

class TagsAdapter(
    private var items: List<Tag>,
    private val onItemClick: (Tag) -> Unit = {},
    private val selectable: Boolean = false,
    private val onEditClick: ((Tag) -> Unit)? = null,
    private val onDeleteClick: ((Tag) -> Unit)? = null,
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
        val isSelected = selectedTags.contains(item)

        // Bind text data
        holder.binding.tvTitle.text = item.name

        // Load image with conditional logic
        val imageSource = if (item.image.isNullOrBlank() || !item.image!!.startsWith("http")) {
            val resId = holder.binding.root.context.resources.getIdentifier(
                item.image, "drawable", holder.binding.root.context.packageName
            )
            if (resId != 0) resId else R.drawable.ic_launcher_background
        } else {
            item.image
        }

        holder.binding.imgItem.load(imageSource) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }

        // Update UI based on selection state
        if (selectable) {
            holder.binding.backgroundView.isSelected = isSelected
            holder.binding.backgroundView.alpha = if (isSelected) 1f else 0.5f
        } else {
            holder.binding.backgroundView.isSelected = false
            holder.binding.backgroundView.alpha = 1f
        }

        // Handle click
        holder.binding.backgroundView.setOnClickListener {
            handleSelection(item, holder.bindingAdapterPosition)
        }

        // Handle Context Menu
        val showContextMenu = onEditClick != null || onDeleteClick != null
        if (showContextMenu) {
            holder.binding.backgroundView.setOnLongClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.tag_menu_context)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onEditClick?.invoke(item)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick?.invoke(item)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
                true
            }
        } else {
            holder.binding.backgroundView.setOnLongClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size

    // Handle selection logic and trigger callbacks
    private fun handleSelection(item: Tag, position: Int) {
        if (selectable) {
            if (item in selectedTags) {
                selectedTags.remove(item)
            } else {
                selectedTags.add(item)
            }
            notifyItemChanged(position)
        }
        onItemClick(item)
    }

    fun getSelectedTags(): Set<Tag> = selectedTags.toSet()

    fun setSelectedTags(tags: Set<Tag>) {
        selectedTags.clear()
        selectedTags.addAll(tags)
        notifyDataSetChanged()
    }

    fun submitList(newList: List<Tag>) {
        items = newList
        notifyDataSetChanged()
    }

    fun clearSelection() {
        val changedIndices = selectedTags.map { items.indexOf(it) }
        selectedTags.clear()
        changedIndices.forEach { if (it != -1) notifyItemChanged(it) }
    }
}

class TagsViewHolder(val binding: PlataformimportcardBinding) :
    RecyclerView.ViewHolder(binding.root)