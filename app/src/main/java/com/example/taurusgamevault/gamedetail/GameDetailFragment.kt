package com.example.taurusgamevault.gamedetail

// TODO: https://github.com/wasabeef/richeditor-android Tiene licencia Apache 2.0.

import com.example.taurusgamevault.adapters.ScreenshotAdapter
import android.app.AlertDialog
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.TagsAdapter
import com.example.taurusgamevault.classes.GameTempData
import com.example.taurusgamevault.databinding.FragmentGameDetailBinding
import com.example.taurusgamevault.enums.GameStates
import com.example.taurusgamevault.enums.Priority
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class GameDetailFragment : Fragment() {
    private val viewModel: GameDetailViewModel by viewModels()
    private val args: GameDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentGameDetailBinding

    private val maxScreenshots = 5
    private var editingScreenshotPosition: Int = -1
    private var currentScreenshots: MutableList<String> = mutableListOf()
    private var editMode: Boolean = false
    private var tagsSelectedIds: MutableList<Long> = mutableListOf()
    private var allTags: List<Tag> = listOf()
    private var priority: Int = 0
    private var gameImage: Uri? = null

    private var menu: Menu? = null

    val pickMainImageLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { loadMainImage(it.toString()) }
            gameImage = uri
        }

    private lateinit var screenshotAdapter: ScreenshotAdapter

    private val pickScreenshotLauncher: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxScreenshots)
        ) { uris ->
            if (uris.isNotEmpty()) {
                uris.forEachIndexed { index, uri ->
                    val targetPosition = editingScreenshotPosition + index
                    if (targetPosition < currentScreenshots.size) {
                        currentScreenshots[targetPosition] = uri.toString()
                    } else {
                        currentScreenshots.add(uri.toString())
                    }
                }
                binding.screenshotsRecyclerView.isVisible = true
                screenshotAdapter.updateScreenshots(currentScreenshots.toList())
                editingScreenshotPosition = -1
                binding.addScreenshotButton.isVisible = editMode && currentScreenshots.size < maxScreenshots
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameDetailBinding.inflate(inflater)

        screenshotAdapter = ScreenshotAdapter(
            onImageClick = { imageUrl ->
                loadMainImage(imageUrl)
            },
            onEditScreenshot = { position ->
                editingScreenshotPosition = position
                pickScreenshotLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        setupScreenshotCarousel()
        setupGameData()

        binding.saveGameButton.setOnClickListener {
            saveCurrentData(args.gameId)
        }

        binding.editMainImage.setOnClickListener {
            pickMainImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        setupMenu()
        setupPickers()

        if (args.editMode) {
            editMode()
        }

        return binding.root
    }

    private fun setupScreenshotCarousel() {
        binding.screenshotsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = screenshotAdapter
        }
    }

    // setup game detail superior menu

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_game_detail_menu, menu)
                this@GameDetailFragment.menu = menu
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        editMode()
                        menu?.findItem(R.id.action_edit)?.setIcon(
                            if (editMode) R.drawable.outline_check_24 else R.drawable.ic_edit_image
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // setup ui fragment
    private fun setupGameData() {
        val gameId = args.gameId

        viewModel.getGame(requireContext(), gameId)
        viewModel.getGameTags(requireContext(), gameId)
        viewModel.getScreenshots(requireContext(), gameId)
        viewModel.getTags(requireContext())

        viewModel.game?.observe(viewLifecycleOwner) { game ->

            (activity as? AppCompatActivity)?.supportActionBar?.title = game.name

            loadMainImage(game.game_image)

            binding.textViewName.text = game.name
            binding.textViewDescription.text = game.description
            binding.textViewReleaseDate.text = game.release_date
            binding.textViewPlaytime.text = game.playtime?.toString()
            binding.textViewPersonalRating.text = game.personal_rating?.toString()
            binding.textViewGameState.text = game.game_state
            binding.textViewStartDate.text = game.start_date
            binding.textViewEndDate.text = game.end_date
            binding.textViewDeadline.text = game.deadline
            binding.textViewPriority.text = game.priority

            binding.editName.setText(game.name)
            binding.editDescription.setText(game.description)
            binding.editReleaseDate.setText(game.release_date)
            binding.editPlaytime.setText(game.playtime?.toString())
            binding.editPersonalRating.setText(game.personal_rating?.toString())
            binding.editGameState.setText(game.game_state)
            binding.editStartDate.setText(game.start_date)
            binding.editEndDate.setText(game.end_date)
            binding.editDeadline.setText(game.deadline)
            binding.editPriority.setText(game.priority)

            priority = Priority.stringToPriority(game.priority ?: "1")?.number ?: 1

            updatePriorityStars(Priority.stringToPriority(game.priority ?: "")?.number ?: 0)
            updateRatingStars(game.personal_rating ?: 0f)
        }

        viewModel.gameTags?.observe(viewLifecycleOwner) { tagsFromBd ->
            tagsSelectedIds = tagsFromBd.map { it.tag_id }.toMutableList()
            
            val platformsOnly = tagsFromBd.filter { it.isPlataform }
            val tagsOnly = tagsFromBd.filter { !it.isPlataform }

            binding.platformsRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = TagsAdapter(
                    items = platformsOnly,
                    onItemClick = { tag ->
                        findNavController().navigate(
                            GameDetailFragmentDirections.actionGameDetailFragmentToListByTagFragment(tag.tag_id, tag.name)
                        )
                    }
                )
            }

            binding.tagsRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = TagsAdapter(
                    items = tagsOnly,
                    onItemClick = { tag ->
                        findNavController().navigate(
                            GameDetailFragmentDirections.actionGameDetailFragmentToListByTagFragment(tag.tag_id, tag.name)
                        )
                    }
                )
            }

            binding.btnEditPlatforms.text = platformsOnly.joinToString(", ") { it.name }.ifEmpty { "Select platforms" }
            binding.btnEditTags.text = tagsOnly.joinToString(", ") { it.name }.ifEmpty { "Select tags" }
        }

        viewModel.allTags?.observe(viewLifecycleOwner) { tagsFromBd ->
            allTags = tagsFromBd
        }

        viewModel.screenshots?.observe(viewLifecycleOwner) { screenshots ->
            val screenshotUrls = screenshots.map { it.image }
            if (screenshotUrls.isEmpty()) {
                showEmptyScreenshots()
            } else {
                showScreenshots(screenshotUrls)
            }
        }
    }

    // requirements and save
    fun saveCurrentData(gameId: Long) {
        val name = binding.editName.text.toString().trim()
        val personalRating = binding.editPersonalRating.text.toString().toFloatOrNull() ?: -1.0f
        val playtime = binding.editPlaytime.text.toString().toIntOrNull() ?: 0

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
            binding.editName.requestFocus()
        } else if (priority == 0) {
            Toast.makeText(requireContext(),
                "Priority must be between 1 and 5", Toast.LENGTH_SHORT).show()
        } else {
            val gameData = GameTempData(
                imageUri = gameImage,
                name = name,
                description = binding.editDescription.text.toString().trim().ifEmpty { null },
                releaseDate = binding.editReleaseDate.text.toString().trim().ifEmpty { null },
                playtime = playtime,
                personalRating = personalRating,
                gameState = binding.editGameState.text.toString().trim().ifEmpty { null },
                startDate = binding.editStartDate.text.toString().trim().ifEmpty { null },
                endDate = binding.editEndDate.text.toString().trim().ifEmpty { null },
                deadline = binding.editDeadline.text.toString().trim().ifEmpty { null },
                priority = Priority.Companion.numberToPriority(priority)?.text,
                screenshots = currentScreenshots
                    .filter { it.startsWith("content://") }
                    .map { it.toUri() },
                allScreenshots = currentScreenshots.toList(),
                plataforms = tagsSelectedIds.toList()
            )

            viewModel.saveGame(requireContext(), gameData, gameId) {
                viewModel.getGame(requireContext(), gameId)
                viewModel.getGameTags(requireContext(), gameId)
                editMode()
            }
        }
    }

    // editmode for ui
    private fun editMode() {
        editMode = !editMode

        screenshotAdapter.setEditMode(editMode)

        binding.addScreenshotButton.isVisible = editMode && currentScreenshots.size < maxScreenshots
        binding.textViewName.isVisible = !editMode
        binding.textViewDescription.isVisible = !editMode
        binding.textViewReleaseDate.isVisible = !editMode
        binding.textViewPlaytime.isVisible = !editMode
        binding.textViewPersonalRating.isVisible = !editMode
        binding.textViewGameState.isVisible = !editMode
        binding.textViewStartDate.isVisible = !editMode
        binding.textViewEndDate.isVisible = !editMode
        binding.textViewDeadline.isVisible = !editMode
        binding.textViewPriority.isVisible = !editMode
        binding.priorityIndicators.isVisible = !editMode
        binding.starRatingLayout.isVisible = !editMode
        
        binding.platformsRecyclerView.isVisible = !editMode
        binding.tagsRecyclerView.isVisible = !editMode

        binding.editMainImage.isVisible = editMode
        binding.editName.isVisible = editMode
        binding.editDescription.isVisible = editMode
        binding.editReleaseDate.isVisible = editMode
        binding.editPlaytime.isVisible = editMode
        binding.editPersonalRating.isVisible = editMode
        binding.editGameState.isVisible = editMode
        binding.editStartDate.isVisible = editMode
        binding.editEndDate.isVisible = editMode
        binding.editDeadline.isVisible = editMode
        binding.editPriority.isVisible = editMode
        binding.btnEditPlatforms.isVisible = editMode
        binding.btnEditTags.isVisible = editMode
        binding.saveGameButton.isVisible = editMode
    }

    private fun loadMainImage(imageUrl: String?) {
        binding.selectedImageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            listener(
                onStart = { _ ->
                    binding.selectedImageView.scaleType = ImageView.ScaleType.FIT_XY
                },
                onSuccess = { _, result ->
                    binding.selectedImageView.scaleType = ImageView.ScaleType.MATRIX
                    val drawable = result.drawable
                    val imageWidth = drawable.intrinsicWidth.toFloat()
                    val imageHeight = drawable.intrinsicHeight.toFloat()
                    val viewWidth = binding.selectedImageView.width.toFloat()
                    val viewHeight = binding.selectedImageView.height.toFloat()
                    val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
                    val matrix = Matrix().apply {
                        postScale(scale, scale)
                        postTranslate((viewWidth - imageWidth * scale) / 2f, 0f)
                    }
                    binding.selectedImageView.imageMatrix = matrix
                },
                onError = { _, _ ->
                    binding.selectedImageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            )
        }
    }

    private fun showEmptyScreenshots() {
        binding.screenshotsRecyclerView.isVisible = false
        binding.addScreenshotButton.isVisible = editMode
    }

    private fun showScreenshots(screenshots: List<String>) {
        currentScreenshots = screenshots.toMutableList()
        binding.screenshotsRecyclerView.isVisible = true
        binding.addScreenshotButton.isVisible = editMode && currentScreenshots.size < maxScreenshots
        screenshotAdapter.updateScreenshots(currentScreenshots.toList())
    }

    private fun updatePriorityStars(priority: Int) {
        for (i in 1..5) {
            val resId = resources.getIdentifier("priority$i", "id", requireContext().packageName)
            if (resId != 0) {
                view?.findViewById<View>(resId)?.visibility = if (i <= priority) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateRatingStars(personalRating: Float) {
        val rating = personalRating / 2f
        for (i in 1..5) {
            val starValue = i.toFloat()
            val resId = resources.getIdentifier("star$i", "id", requireContext().packageName)
            val star = view?.findViewById<ImageView>(resId) ?: continue
            when {
                rating >= starValue -> star.setImageResource(R.drawable.starimg)
                rating >= starValue - 0.5f -> star.setImageResource(R.drawable.halfstarimg)
                else -> star.setImageResource(R.drawable.emptystarimage)
            }
        }
    }

    // screenshot system image picker
    private fun setupPickers() {
        binding.addScreenshotButton.setOnClickListener {
            editingScreenshotPosition = currentScreenshots.size
            pickScreenshotLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        val priorities: Array<String> = Priority.entries.map { it.text }.toTypedArray()

        // priorities dialog
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

        // game state dialog
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
                    }.show()
            }
        }

        // edit platforms dialog
        binding.btnEditPlatforms.setOnClickListener {
            val platformsOnly = allTags.filter { it.isPlataform }
            val selectedItems = BooleanArray(platformsOnly.size) { index ->
                tagsSelectedIds.contains(platformsOnly[index].tag_id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select platforms")
                .setMultiChoiceItems(
                    platformsOnly.map { it.name }.toTypedArray(),
                    selectedItems
                ) { _, which, isChecked ->
                    val id = platformsOnly[which].tag_id
                    if (isChecked) {
                        if (!tagsSelectedIds.contains(id)) tagsSelectedIds.add(id)
                    } else {
                        tagsSelectedIds.remove(id)
                    }
                }
                .setPositiveButton("Accept") { _, _ ->
                    val selected = platformsOnly.filter { tagsSelectedIds.contains(it.tag_id) }
                    binding.btnEditPlatforms.text = selected.joinToString(", ") { it.name }.ifEmpty { "Select platforms" }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // edit tags dialog
        binding.btnEditTags.setOnClickListener {
            val tagsOnly = allTags.filter { !it.isPlataform }
            val selectedItems = BooleanArray(tagsOnly.size) { index ->
                tagsSelectedIds.contains(tagsOnly[index].tag_id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select tags")
                .setMultiChoiceItems(
                    tagsOnly.map { it.name }.toTypedArray(),
                    selectedItems
                ) { _, which, isChecked ->
                    val id = tagsOnly[which].tag_id
                    if (isChecked) {
                        if (!tagsSelectedIds.contains(id)) tagsSelectedIds.add(id)
                    } else {
                        tagsSelectedIds.remove(id)
                    }
                }
                .setPositiveButton("Accept") { _, _ ->
                    val selected = tagsOnly.filter { tagsSelectedIds.contains(it.tag_id) }
                    binding.btnEditTags.text = selected.joinToString(", ") { it.name }.ifEmpty { "Select tags" }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.editReleaseDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(text.toString()) { setText(it) } }
        }

        binding.editStartDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(text.toString()) { setText(it) } }
        }

        binding.editEndDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(text.toString()) { setText(it) } }
        }

        binding.editDeadline.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(text.toString()) { setText(it) } }
        }

        binding.btnGoToAnnotations.setOnClickListener {
            val action = GameDetailFragmentDirections.actionGameDetailFragmentToAnnotationsFragment(gameId = args.gameId, gameName = args.gameName)
            findNavController().navigate(action)
        }

        binding.btnGoToObjectives.setOnClickListener {
            val action = GameDetailFragmentDirections.actionGameDetailFragmentToObjectivesFragment(gameId = args.gameId, gameName = args.gameName)
            findNavController().navigate(action)
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
            onDateSelected(formatDate(selection))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timestamp
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month, year)
    }

    private fun parseDateToMillis(dateString: String): Long {
        return try {
            val parts = dateString.split("/")
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt(), 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        } catch (_: Exception) {
            MaterialDatePicker.todayInUtcMilliseconds()
        }
    }
}