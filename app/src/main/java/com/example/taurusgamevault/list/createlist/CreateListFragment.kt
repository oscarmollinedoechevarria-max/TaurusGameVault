package com.example.taurusgamevault.list.createlist

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.SimplifiedGameAdapter
import com.example.taurusgamevault.classes.SimplifiedGame
import com.example.taurusgamevault.classes.ListTempData
import com.example.taurusgamevault.databinding.FragmentCreateListBinding
import com.example.taurusgamevault.gamepicker.GamePickerFragment

class CreateListFragment : Fragment() {

    private val viewModel: CreateListViewModel by viewModels()

    lateinit var binding: FragmentCreateListBinding
    private var gamesSelected: List<SimplifiedGame>? = null

    private var selectedImageUri: Uri? = null

    private var selectedGamesRecyclerView: SimplifiedGameAdapter? = null

    // launch the photo picker and let the user choose only images
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.imageViewListBanner.setImageURI(uri)

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateListBinding.inflate(inflater)

        binding.fabGoBack.setOnClickListener {
            findNavController().navigate(R.id.action_createListFragment_to_gameListFragment)
        }

        setupGamePickerResultListener()

        setupRecyclerView()

        setupPickers()

        return binding.root
    }

    private fun setupPickers(){
        binding.saveListButton.setOnClickListener {
            saveList()
        }

        binding.selectBannerImageButton.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.fabAddGame.setOnClickListener {
            showGamePicker()
        }
    }

    // requirements and save the list to the database
    private fun saveList() {

        val name = binding.textViewListName.text.toString().trim()
        val description = binding.textViewListDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.textViewListName.error = "Name is required"
            binding.textViewListName.requestFocus()
            return
        }

        val safeDescription = description.ifEmpty { null }

        var safeGames: List<Long>? = null

        if(gamesSelected != null){
            val games = gamesSelected
            safeGames = games?.map { it.gameId }
        }


        val listTemp = ListTempData(
            name = name,
            description = safeDescription,
            image = selectedImageUri,
            games = safeGames
        )

        viewModel.saveList(requireContext(), listTemp)

        findNavController().navigate(R.id.action_createListFragment_to_gameListFragment)
    }

    // listen to the result of the game picker
    private fun setupGamePickerResultListener() {
        parentFragmentManager.setFragmentResultListener(
            GamePickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedGames = bundle.getParcelableArrayList<SimplifiedGame>(
                GamePickerFragment.RESULT_SELECTED_GAMES
            )

            selectedGames?.let { games ->
                handleSelectedGames(games)
            }
        }
    }

    // show the game picker dialog
    private fun showGamePicker() {
        val selectedGames = gamesSelected ?: emptyList()

        val dialog = GamePickerFragment.newInstance(selectedGames)
        dialog.show(parentFragmentManager, "GamePickerDialog")
    }


    private fun setupRecyclerView(){
        selectedGamesRecyclerView = SimplifiedGameAdapter(
            list = emptyList(),
            context = requireContext(),
            navigation = findNavController(),
            shouldBeClicable = false
        )

        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedGamesRecyclerView
        }
    }

    private fun handleSelectedGames(games: List<SimplifiedGame>) {
        gamesSelected = games

        val count = games.size
        binding.textViewGameCount.text = resources.getQuantityString(
            R.plurals.games_count_label,
            count,
            count
        )

        selectedGamesRecyclerView?.submitList(games)

        if (games.isEmpty()) {
            binding.recyclerViewGames.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.recyclerViewGames.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }
}