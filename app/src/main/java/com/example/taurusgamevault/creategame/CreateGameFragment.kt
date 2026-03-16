package com.example.taurusgamevault.creategame

import android.app.AlertDialog
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.taurusgamevault.R
import com.example.taurusgamevault.classes.GameTempData
import com.example.taurusgamevault.databinding.FragmentCreateGameBinding
import androidx.constraintlayout.helper.widget.Carousel
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.SharedViewModel
import com.example.taurusgamevault.enums.GameStates
import com.example.taurusgamevault.enums.Priority
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import java.util.TimeZone

class CreateGameFragment : Fragment() {
    private val viewModel: CreateGameViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by viewModels()

    lateinit var binding: FragmentCreateGameBinding

    private var selectedImageUri: Uri? = null
    private val selectedScreenshotUris: MutableList<Uri> = mutableListOf()

    private var plataformsSelected: MutableList<Long> = mutableListOf()
    private var tagsSelected: MutableList<Long> = mutableListOf()

    private var allTags: List<Tag> = listOf()

    private val MAX_SCREENSHOTS = 5

    private var priority = 0

    private val pickMultipleMedia =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(MAX_SCREENSHOTS)
        ) { uris ->
            if (uris.isNotEmpty()) {
                selectedScreenshotUris.clear()
                selectedScreenshotUris.addAll(uris)

                val imageViews = listOf(
                    binding.imageView0,
                    binding.imageView1,
                    binding.imageView2,
                    binding.imageView3,
                    binding.imageView4
                )

                imageViews.forEachIndexed { index, imageView ->
                    if (index < selectedScreenshotUris.size) {
                        imageView.setImageURI(selectedScreenshotUris[index])
                        imageView.visibility = View.VISIBLE
                    } else {
                        imageView.visibility = View.GONE
                    }
                }

                binding.motionLayout.visibility = View.VISIBLE

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

        binding.carousel.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return selectedScreenshotUris.size
            }

            override fun populate(view: View, index: Int) {
                val imageView = view as ImageView
                imageView.setImageURI(selectedScreenshotUris[index])
            }

            override fun onNewItem(index: Int) {
            }
        })

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

        viewModel.getTags(requireContext())

        viewModel.tags?.observe(viewLifecycleOwner) { tagsList ->
            allTags = tagsList
        }

        binding.saveGameButton.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val personalRating = binding.editPersonalRating.text.toString().toFloatOrNull() ?: -1.0f
            val playtime = binding.editPlaytime.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
                binding.editName.requestFocus()
            } else if (personalRating < 0.0f || personalRating > 10.0f) {
                Toast.makeText(requireContext(), "Rating must be between 0 and 10", Toast.LENGTH_SHORT).show()
            } else if (playtime < 0 || playtime > 10000) {
                Toast.makeText(requireContext(), "Playtime must be between 0 and 10000", Toast.LENGTH_SHORT).show()
            } else if (priority == 0) {
                Toast.makeText(requireContext(), "Priority must be between 1 and 5", Toast.LENGTH_SHORT).show()
            } else {
                val gameData = GameTempData(
                    imageUri = selectedImageUri,
                    name = name,
                    description = binding.editDescription.text.toString().trim().ifEmpty { null },
                    releaseDate = binding.editReleaseDate.text.toString().trim().ifEmpty { null },
                    playtime = playtime,
                    personalRating = personalRating,
                    gameState = binding.editGameState.text.toString().trim().ifEmpty { null },
                    startDate = binding.editStartDate.text.toString().trim().ifEmpty { null },
                    endDate = binding.editEndDate.text.toString().trim().ifEmpty { null },
                    deadline = binding.editDeadline.text.toString().trim().ifEmpty { null },
                    priority = Priority.numberToPriority(priority)?.text,
                    screenshots = selectedScreenshotUris.toList(),
                    plataforms = (plataformsSelected + tagsSelected).toList(),
                    allScreenshots = null
                )

                viewModel.saveGame(requireContext(), gameData, (plataformsSelected + tagsSelected).toList())

                findNavController().navigate(R.id.action_createGameFragment_to_mainFragment)
            }
        }

        binding.fabGoBack.setOnClickListener {
            findNavController().navigate(R.id.action_createGameFragment_to_mainFragment)
        }

        setupPickers()

        return binding.root
    }

    private fun setupPickers() {

        binding.multiSelectPlataform.setOnClickListener {
            val platformsOnly = allTags.filter { it.isPlataform }
            val selectedItems = BooleanArray(platformsOnly.size) { index ->
                plataformsSelected.contains(platformsOnly[index].tag_id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select platforms")
                .setMultiChoiceItems(
                    platformsOnly.map { it.name }.toTypedArray(),
                    selectedItems
                ) { _, which, isChecked ->
                    val id = platformsOnly[which].tag_id
                    if (isChecked) {
                        if (!plataformsSelected.contains(id)) plataformsSelected.add(id)
                    } else {
                        plataformsSelected.remove(id)
                    }
                }
                .setPositiveButton("Accept") { _, _ ->
                    val selected = platformsOnly.filter { plataformsSelected.contains(it.tag_id) }
                    binding.multiSelectPlataform.text = selected.joinToString(", ") { it.name }.ifEmpty { "Select platforms" }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.multiSelectTags.setOnClickListener {
            val tagsOnly = allTags.filter { !it.isPlataform }
            val selectedItems = BooleanArray(tagsOnly.size) { index ->
                tagsSelected.contains(tagsOnly[index].tag_id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select tags")
                .setMultiChoiceItems(
                    tagsOnly.map { it.name }.toTypedArray(),
                    selectedItems
                ) { _, which, isChecked ->
                    val id = tagsOnly[which].tag_id
                    if (isChecked) {
                        if (!tagsSelected.contains(id)) tagsSelected.add(id)
                    } else {
                        tagsSelected.remove(id)
                    }
                }
                .setPositiveButton("Accept") { _, _ ->
                    val selected = tagsOnly.filter { tagsSelected.contains(it.tag_id) }
                    binding.multiSelectTags.text = selected.joinToString(", ") { it.name }.ifEmpty { "Select tags" }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Priority
        val priorities: Array<String> = Priority.entries.map { it.text }.toTypedArray()

        binding.editPriority.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select priority")
                    .setItems(priorities) { _, which ->
                        val state = priorities[which]
                        binding.editPriority.setText(state)
                        binding.editPriority.clearFocus()
                        binding.editPriority.setSelection(0)
                        priority = Priority.stringToPriority(state)?.number ?: 0
                    }.show()
            }
        }

        val states: Array<String> = GameStates.entries.map { it.text }.toTypedArray()

        // Game state
        binding.editGameState.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select game state")
                    .setItems(states) { _, which ->
                        val selectedPriority = states[which]
                        binding.editGameState.setText(selectedPriority)
                        binding.editGameState.clearFocus()
                    }
                    .show()
            }
        }

        // Release Date
        binding.editReleaseDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePicker(text.toString()) { selectedDate ->
                    setText(selectedDate)
                }
            }
        }

        // Start Date
        binding.editStartDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePicker(text.toString()) { selectedDate ->
                    setText(selectedDate)
                }
            }
        }

        // End Date
        binding.editEndDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePicker(text.toString()) { selectedDate ->
                    setText(selectedDate)
                }
            }
        }

        // Deadline
        binding.editDeadline.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePicker(text.toString()) { selectedDate ->
                    setText(selectedDate)
                }
            }
        }
    }

    private fun showDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
        val initialSelection = if (currentDate.isNotEmpty() && currentDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            parseDateToMillis(currentDate)
        } else {
            MaterialDatePicker.todayInUtcMilliseconds()
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(initialSelection)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formattedDate = formatDate(selection)
            onDateSelected(formattedDate)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timestamp

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        return String.format("%02d/%02d/%04d", day, month, year)
    }

    private fun parseDateToMillis(dateString: String): Long {
        return try {
            val parts = dateString.split("/")
            val day = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val year = parts[2].toInt()
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.set(year, month, day, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        } catch (e: Exception) {
            MaterialDatePicker.todayInUtcMilliseconds()
        }
    }
}
