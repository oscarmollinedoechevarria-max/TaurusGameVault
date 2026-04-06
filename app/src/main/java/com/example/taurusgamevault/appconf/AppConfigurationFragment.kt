package com.example.taurusgamevault.appconf

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import com.example.taurusgamevault.databinding.FragmentAppConfigurationBinding
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.taurusgamevault.annotations.AnnotationsViewModel
import kotlin.getValue

class AppConfigurationFragment : Fragment() {
    private lateinit var binding: FragmentAppConfigurationBinding

    private val viewModel: AppConfigurationViewModel by viewModels()

    val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            viewModel.importDatabase(requireContext(), uri)
        } else {
            Toast.makeText(context, "Import canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppConfigurationBinding.inflate(inflater, container, false)

        val prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        binding.switchDayNight.isChecked = isDarkMode

        // switch dark mode
        binding.switchDayNight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // create backup
        binding.btnCreateBackup.setOnClickListener {
            viewModel.shareDatabase(requireContext())
        }

        // restore backup
        binding.btnRestoreBackup.setOnClickListener {
            importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        return binding.root
    }
}