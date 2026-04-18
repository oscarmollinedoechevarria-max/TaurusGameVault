package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.Model.room.entities.Objective
import com.example.taurusgamevault.databinding.ItemObjectiveBinding

class ObjectivesAdapter(
    private var items: List<Objective> = emptyList(),
    private var editMode: Boolean = false,
    // block for checkbox click
    private val onChecked: (Objective) -> Unit,
    // block for delete button click
    private val onDelete: (Objective) -> Unit
) : RecyclerView.Adapter<ObjectivesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectivesViewHolder {
        val binding = ItemObjectiveBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ObjectivesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItems: List<Objective>, newEditMode: Boolean) {
        items = newItems
        editMode = newEditMode
        notifyDataSetChanged()
    }

    fun updateEditMode(newEditMode: Boolean) {
        editMode = newEditMode
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ObjectivesViewHolder, position: Int) {
        val item = items[position]

        // bind data and state
        holder.binding.tvObjectiveTitle.text = item.title
        holder.binding.cbCompleted.isChecked = item.completed
        holder.binding.cbCompleted.isEnabled = editMode

        // checked box click
        holder.binding.cbCompleted.setOnClickListener {
            onChecked(item)
        }

        // delete button visibility and click
        holder.binding.btnDeleteObjective.visibility = if (editMode) View.VISIBLE else View.GONE
        holder.binding.btnDeleteObjective.setOnClickListener {
            onDelete(item)
        }
    }
}

class ObjectivesViewHolder(val binding: ItemObjectiveBinding) :
    RecyclerView.ViewHolder(binding.root)