package com.example.taurusgamevault.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.classes.ItemPlataformImport
import com.example.taurusgamevault.databinding.PlataformimportcardBinding

class ImportPlataformAdapter(
    private val list: List<ItemPlataformImport>,
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
        holder.bind(list[position], onClick)
    }

    override fun getItemCount(): Int = list.size
}

// for each item in the list add the image and text of the item to the view
class ImportPlataformViewHolder(
    private val binding: PlataformimportcardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ItemPlataformImport, onClick: (ItemPlataformImport) -> Unit) {
        binding.imgItem.setImageResource(item.imageRes)
        binding.tvTitle.text = item.title

        binding.root.setOnClickListener {
            onClick(item)
        }
    }
}