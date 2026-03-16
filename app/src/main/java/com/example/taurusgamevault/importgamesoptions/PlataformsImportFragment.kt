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
            ItemPlataformImport(R.drawable.circlereddarkened, "Import games"),
        )

        binding.plataformsImportRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.plataformsImportRecyclerView.adapter = ImportPlataformAdapter(items) { item ->
            findNavController().navigate(R.id.action_plataformsImportFragment_to_importGamesNoAccountFragment)
        }

        return binding.root
    }
}