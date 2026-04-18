package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.classes.ItemPlataformImport
import com.example.taurusgamevault.databinding.PlataformimportcardBinding

class ImportPlataformAdapter(
    private var list: List<ItemPlataformImport>,
    // block for item click
    private val onClick: (ItemPlataformImport) -> Unit
) : RecyclerView.Adapter<ImportPlataformViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportPlataformViewHolder {
        val binding = PlataformimportcardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImportPlataformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImportPlataformViewHolder, position: Int) {
        val item = list[position]

        // for each item in the list add the image and text of the item to the view
        holder.binding.tvTitle.text = item.title
        holder.binding.imgItem.setImageResource(item.imageRes)

        //tint for the first icon
        if (item.title == "Import games") {
            holder.binding.imgItem.setColorFilter(
                ContextCompat.getColor(holder.binding.imgItem.context, R.color.text_primary_adaptive)
            )
        } else {
            holder.binding.imgItem.colorFilter = null
        }

        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int {
     return  list.size
    }
}

class ImportPlataformViewHolder(val binding: PlataformimportcardBinding) :
    RecyclerView.ViewHolder(binding.root)