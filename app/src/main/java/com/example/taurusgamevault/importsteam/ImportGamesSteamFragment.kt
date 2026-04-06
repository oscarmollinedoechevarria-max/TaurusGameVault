package com.example.taurusgamevault.importsteam

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.FragmentImportGamesSteamBinding
import com.example.taurusgamevault.importgamesigdb.ImportState

class ImportGamesSteamFragment : Fragment() {

    private val viewModel: ImportGamesSteamViewModel by viewModels()
    private lateinit var binding: FragmentImportGamesSteamBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportGamesSteamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnImport.setOnClickListener {
            val steamId = binding.etSteamId.text.toString().trim()
            val apiKey  = binding.etApiKey.text.toString().trim()

            if (steamId.isEmpty() || apiKey.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.importGames(requireContext(), steamId, apiKey)
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
                    binding.tvProgress.text = if (state.total == 0)
                        state.currentGame
                    else
                        "${state.currentGame} (${state.current}/${state.total})"
                }
                is ImportState.Done -> {
                    binding.btnImport.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = "✓ Completed"
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