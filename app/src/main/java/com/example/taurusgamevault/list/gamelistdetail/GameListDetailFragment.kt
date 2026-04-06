package com.example.taurusgamevault.list.gamelistdetail

import android.graphics.Matrix
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.ListGameAdapter
import com.example.taurusgamevault.adapters.SimplifiedGameAdapter
import com.example.taurusgamevault.classes.ListTempData
import com.example.taurusgamevault.classes.SimplifiedGame
import com.example.taurusgamevault.classes.toGame
import com.example.taurusgamevault.databinding.FragmentGameListDetailBinding
import com.example.taurusgamevault.gamedetail.GameDetailFragmentArgs
import com.example.taurusgamevault.gamepicker.GamePickerFragment
import io.ktor.http.Url
import kotlin.getValue
import androidx.core.net.toUri
import com.example.taurusgamevault.Model.room.entities.List_game

class GameListDetailFragment : Fragment() {

    lateinit var binding: FragmentGameListDetailBinding

    private val viewModel: GameListDetailViewModel by viewModels()

    private val args: GameListDetailFragmentArgs by navArgs()

    private var editMode: Boolean = false

    private var gamesSelected: MutableSet<SimplifiedGame>? = null

    private var selectedImageUri: Uri? = null

    private var oldImageUri: String? = null

    private var selectedGamesRecyclerView: SimplifiedGameAdapter? = null

    // system picker
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.imageViewListBanner.setImageURI(uri)

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private var listId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameListDetailBinding.inflate(inflater)

        listId = args.listId

        setupView(listId ?: 0)

        if(args.editMode){
            editMode()
        }

        return binding.root
    }

    fun setupView(gameListId: Long) {
        setupRecyclerView()
        setupGamePickerResultListener()

        binding.fabEdit.setOnClickListener {
            editMode()
        }

        binding.savListButton.setOnClickListener {
            saveList()
        }

        binding.fabAddGame.setOnClickListener {
            showGamePicker()
        }

        binding.addGameEdit.setOnClickListener {
            showGamePicker()
        }

        binding.buttonChangeBanner.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        val context = requireContext()

        viewModel.getList(context, gameListId)

        viewModel.getGames(context, gameListId)

        viewModel.list?.observe(viewLifecycleOwner) { list ->
            oldImageUri = list.image

            binding.textViewListNameEdit.setText(list.name)
            binding.textViewListDescriptionEdit.setText(list.description ?: "")

            binding.imageViewListBanner.load(list.image) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)

                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)

                listener(
                    onStart = { _ ->
                        binding.imageViewListBanner.scaleType = ImageView.ScaleType.FIT_XY
                    },
                    onSuccess = { _, result ->
                        binding.imageViewListBanner.scaleType = ImageView.ScaleType.MATRIX

                        val drawable = result.drawable
                        val imageWidth = drawable.intrinsicWidth.toFloat()
                        val imageHeight = drawable.intrinsicHeight.toFloat()
                        val viewWidth = binding.imageViewListBanner.width.toFloat()
                        val viewHeight = binding.imageViewListBanner.height.toFloat()

                        val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)

                        val matrix = Matrix().apply {
                            postScale(scale, scale)
                            postTranslate((viewWidth - imageWidth * scale) / 2f, 0f)
                        }

                        binding.imageViewListBanner.imageMatrix = matrix
                        Log.d("ImageCache", "Fuente: ${result.dataSource}")
                    },
                    onError = { _, _ ->
                        binding.imageViewListBanner.scaleType = ImageView.ScaleType.FIT_XY
                    }
                )
            }

            binding.textViewListName.text = list.name

            binding.textViewListDescription.text = list.description
        }

        viewModel.games?.observe(viewLifecycleOwner) { games ->
            binding.textViewGameCount.text = games.size.toString()
            handleSelectedGames(games)
        }

    }

    fun editMode() {
        editMode = !editMode

        binding.textViewListName.isVisible = !editMode
        binding.textViewListNameEdit.isVisible = editMode

        binding.textViewListDescription.isVisible = !editMode
        binding.textViewListDescriptionEdit.isVisible = editMode

        binding.addGameEdit.isVisible = editMode

        binding.textViewGameCount.isVisible = !editMode

        binding.buttonChangeBanner.isVisible = editMode
        binding.savListButton.isVisible = editMode

        binding.fabAddGame.isVisible = !editMode
        binding.buttonChangeBanner.isVisible = editMode

        selectedGamesRecyclerView?.toggleEditMode()

        binding.fabEdit.setImageResource(
            if (editMode) R.drawable.outline_check_24 else R.drawable.editimage
        )
    }

    // requirements and save the list
    private fun saveList() {
        val name = binding.textViewListNameEdit.text.toString().trim()
        val description = binding.textViewListDescriptionEdit.text.toString().trim()

        if (name.isEmpty()) {
            binding.textViewListNameEdit.error = "Name is required"
            binding.textViewListNameEdit.requestFocus()
            return
        }

        val safeDescription = description.ifEmpty { null }

        var safeGames: List<Long>? = null
        if (gamesSelected != null) {
            safeGames = gamesSelected?.map { it.gameId }
        }

        val listTemp = ListTempData(
            name = name,
            description = safeDescription,
            image = selectedImageUri,
            games = safeGames
        )

        viewModel.saveList(requireContext(), listTemp, listId ?: 0, oldImageUri)

        editMode()
    }

    // show the game picker dialog
    private fun showGamePicker() {
        val selectedGames = gamesSelected ?: emptyList()

        val dialog = GamePickerFragment.newInstance(selectedGames.toList())
        dialog.show(parentFragmentManager, "GamePickerDialog")
    }

    // listen to the result of the game picker
    private fun setupGamePickerResultListener() {
        parentFragmentManager.setFragmentResultListener(
            GamePickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val newGames = bundle.getParcelableArrayList<SimplifiedGame>(
                GamePickerFragment.RESULT_SELECTED_GAMES
            ) ?: return@setFragmentResultListener

            if (!editMode) {
                val existingIds = gamesSelected?.map { it.gameId }?.toSet() ?: emptySet()

                val onlyNew = newGames.filter { it.gameId !in existingIds }

                if (onlyNew.isNotEmpty()) {
                    onlyNew.forEach { game ->
                        viewModel.addListGame(
                            requireContext(), List_game(
                                list_id = listId ?: 0,
                                game_id = game.gameId
                            )
                        )
                    }
                    Toast.makeText(context, "Games added successfully!", Toast.LENGTH_SHORT).show()
                }

                handleSelectedGames(
                    (gamesSelected?.plus(newGames) ?: newGames).distinctBy { it.gameId }
                )
            } else {
                handleSelectedGames(newGames)
            }
        }
    }

    private fun setupRecyclerView() {
        selectedGamesRecyclerView = SimplifiedGameAdapter(
            list = emptyList(),
            context = requireContext(),
            navigation = findNavController(),
            shouldBeClicable = true,
            onRemove = { game ->
                val updated = gamesSelected.orEmpty().filter { it.gameId != game.gameId }
                handleSelectedGames(updated)
                val temp = List_game(
                    list_id = listId ?: 0,
                    game_id = game.gameId
                )
                viewModel.deleteGame(requireContext(), temp)
            }
        )

        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedGamesRecyclerView
        }
    }

    private fun handleSelectedGames(games: List<SimplifiedGame>) {
        val updated = games.toMutableSet()
        gamesSelected = updated

        val count = updated.size
        binding.textViewGameCount.text = resources.getQuantityString(
            R.plurals.games_count_label,
            count,
            count
        )

        selectedGamesRecyclerView?.submitList(updated.toList())

        binding.recyclerViewGames.isVisible = updated.isNotEmpty()
        binding.layoutEmpty.isVisible = updated.isEmpty()
    }
}