package com.example.taurusgamevault.creategame

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.taurusgamevault.R
import com.example.taurusgamevault.classes.GameTempData
import com.example.taurusgamevault.databinding.FragmentCreateGameBinding
import com.example.taurusgamevault.databinding.FragmentMainBinding

class CreateGameFragment : Fragment() {
    private val viewModel: CreateGameViewModel by viewModels()

    lateinit var binding: FragmentCreateGameBinding

    private var selectedImageUri: Uri? = null
    private val selectedScreenshotUris: MutableList<Uri> = mutableListOf()

    private val MAX_SCREENSHOTS = 5

    private val pickMultipleMedia =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(MAX_SCREENSHOTS)
        ) { uris ->
            if (uris.isNotEmpty()) {
                selectedScreenshotUris.clear()
                selectedScreenshotUris.addAll(uris)
                binding.selectedScreenshotView.setImageURI(uris.first())
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.selectedImageView.setImageURI(uri)

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateGameBinding.inflate(inflater)

        binding.selectImageButton.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.selectScreenshotButton.setOnClickListener {
            pickMultipleMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.saveGameButton.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val personalRating = binding.editPersonalRating.text.toString().toDoubleOrNull() ?: -1.0
            val playtime = binding.editPlaytime.text.toString().toIntOrNull() ?: 0
            val priority = binding.editPriority.text.toString().toIntOrNull() ?: -1

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
                binding.editName.requestFocus()
            } else if (personalRating < 0.0 || personalRating > 10.0) {
                Toast.makeText(requireContext(), "Rating must be between 0 and 10", Toast.LENGTH_SHORT).show()
            } else if (playtime < 0 || playtime > 10000) {
                Toast.makeText(requireContext(), "Playtime must be between 0 and 10000", Toast.LENGTH_SHORT).show()
            } else if (priority < 1 || priority > 5) {
                Toast.makeText(requireContext(), "Priority must be between 1 and 5", Toast.LENGTH_SHORT).show()
            } else {
                val gameData = GameTempData(
                    imageUri = selectedImageUri,
                    screenshotUris = selectedScreenshotUris.toList(),
                    name = name,
                    description = binding.editDescription.text.toString().trim().ifEmpty { null },
                    releaseDate = binding.editReleaseDate.text.toString().trim().ifEmpty { null },
                    playtime = playtime,
                    personalRating = personalRating,
                    gameState = binding.editGameState.text.toString().trim().ifEmpty { null },
                    startDate = binding.editStartDate.text.toString().trim().ifEmpty { null },
                    endDate = binding.editEndDate.text.toString().trim().ifEmpty { null },
                    deadline = binding.editDeadline.text.toString().trim().ifEmpty { null },
                    priority = priority
                )

                viewModel.saveGame(requireContext(), gameData)

                findNavController().navigate(R.id.action_createGameFragment_to_mainFragment)
            }
        }

        return binding.root
    }
}