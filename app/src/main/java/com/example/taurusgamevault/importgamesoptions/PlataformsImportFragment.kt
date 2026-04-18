package com.example.taurusgamevault.importgamesoptions

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.ImportPlataformAdapter
import com.example.taurusgamevault.classes.ItemPlataformImport
import com.example.taurusgamevault.databinding.FragmentPlataformsImportBinding

class PlataformsImportFragment : Fragment() {

    private val viewModel: PlataformsImportViewModel by viewModels()
    private lateinit var binding: FragmentPlataformsImportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlataformsImportBinding.inflate(inflater, container, false)

        val items = listOf(
            ItemPlataformImport(R.drawable.ic_import_contacts, "Import games"),
            ItemPlataformImport(R.drawable.gogicon, "Import games Steam"),
            ItemPlataformImport(R.drawable.store_steam, "Import games Gog"),
            )

        // Set up the RecyclerView(made like this for easy tests)
        binding.plataformsImportRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.plataformsImportRecyclerView.adapter = ImportPlataformAdapter(items) { item ->
            when (item.title) {
                "Import games" -> findNavController().navigate(R.id.action_plataformsImportFragment_to_importGamesNoAccountFragment)
                "Import games Steam" -> findNavController().navigate(R.id.action_plataformsImportFragment_to_importGamesSteamFragment)
                "Import games Gog" -> findNavController().navigate(R.id.action_plataformsImportFragment_to_importGamesGOGFragment)
            }
        }

        return binding.root
    }
}