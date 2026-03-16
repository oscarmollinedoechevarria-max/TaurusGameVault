package com.example.taurusgamevault.importgamesigdb

import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresExtension
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.databinding.FragmentImportGamesNoAccountBinding
import androidx.lifecycle.ViewModelProvider
import com.example.taurusgamevault.Model.retrofit.igdb.IgdbRetrofit

class ImportGamesNoAccountFragment : Fragment() {

    private val viewModel: ImportGamesNoAccountViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository =
                @Suppress("UNCHECKED_CAST")
                return ImportGamesNoAccountViewModel(IgdbRetrofit) as T
            }
        }
    }

    private lateinit var binding: FragmentImportGamesNoAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportGamesNoAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnImport.setOnClickListener {
            val text = binding.etGameNames.text.toString()
            viewModel.importGames(requireContext(), text)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ImportState.Idle -> {
                    binding.btnImport.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = ""
                }
                is ImportState.Loading -> {
                    binding.btnImport.isEnabled = false
                    binding.progressBar.isVisible = true
                    binding.progressBar.max = state.total
                    binding.progressBar.progress = state.current
                    binding.tvProgress.text = "(${state.current}/${state.total}) ${state.currentGame}"
                }
                is ImportState.Done -> {
                    binding.btnImport.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = "✓ Completado"
                }
                is ImportState.Error -> {
                    binding.btnImport.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = "Error: ${state.message}"
                }
            }
        }
    }
}