package com.example.taurusgamevault.objectives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.Model.room.entities.Objective
import com.example.taurusgamevault.databinding.ItemObjectiveBinding

class ObjectivesAdapter(
    private var items: List<Objective> = emptyList(),
    private var editMode: Boolean = false,
    private val onChecked: (Objective) -> Unit,
    private val onDelete: (Objective) -> Unit
) : RecyclerView.Adapter<ObjectivesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemObjectiveBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Objective) {
            binding.tvObjectiveTitle.text = item.title
            binding.cbCompleted.isChecked = item.completed
            binding.cbCompleted.isEnabled = editMode
            binding.cbCompleted.setOnClickListener {
                onChecked(item)
            }
            binding.btnDeleteObjective.visibility = if (editMode) View.VISIBLE else View.GONE
            binding.btnDeleteObjective.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectiveBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Objective>, newEditMode: Boolean) {
        items = newItems
        editMode = newEditMode
        notifyDataSetChanged()
    }

    fun updateEditMode(newEditMode: Boolean) {
        editMode = newEditMode
        notifyDataSetChanged()
    }
}
