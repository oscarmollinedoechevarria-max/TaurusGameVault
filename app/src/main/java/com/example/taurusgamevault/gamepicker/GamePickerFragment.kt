package com.example.taurusgamevault.gamepicker

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.adapters.GamePickerAdapter
import com.example.taurusgamevault.databinding.FragmentGamePickerBinding
import android.text.TextWatcher
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.taurusgamevault.Model.room.entities.toSimplifiedGame
import com.example.taurusgamevault.classes.SimplifiedGame

class GamePickerFragment : DialogFragment() {

    lateinit var binding: FragmentGamePickerBinding
    private lateinit var adapter: GamePickerAdapter
    private var allGames: List<Game> = listOf()
    private val selectedGames = mutableSetOf<Game>()

    private val viewModel: GamePickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGamePickerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }


    private fun observeViewModel() {
        viewModel.getGames(requireContext())

        viewModel.games?.observe(viewLifecycleOwner) { games ->
            if (games != null) {
                allGames = games
                // If there's already text in the search box (e.g., on rotation), filter immediately
                val currentQuery = binding.searchEditText.text.toString()
                filterGames(currentQuery)

                // Restore preselected games only after data is loaded
                if (selectedGames.isEmpty()) {
                    restorePreselectedGames(games)
                }
            }
        }
    }

    private fun restorePreselectedGames(loadedGames: List<Game>) {
        val preselected = arguments?.getParcelableArrayList<SimplifiedGame>(ARG_PRESELECTED_GAMES)

        preselected?.let { list ->
            val matches = loadedGames.filter { game ->
                list.any { it.name == game.name }
            }
            selectedGames.addAll(matches)
            adapter.updateSelection(selectedGames)
            updateSelectedCount()
        }
    }

    private fun setupRecyclerView() {
        adapter = GamePickerAdapter(
            onGameClick = { game -> toggleGameSelection(game) },
            list = listOf()
        )

        binding.gamesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GamePickerFragment.adapter
        }
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.doneButton.setOnClickListener {
            val simplifiedGames = selectedGames.map { it.toSimplifiedGame() }
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_SELECTED_GAMES to ArrayList(simplifiedGames))
            )
            dismiss()
        }

        // textWatcher for search
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterGames(s.toString())
            }
        })
    }

    // filterGames by the query on the edit text
    private fun filterGames(query: String) {
        val filteredList = if (query.isBlank()) {
            allGames
        } else {
            allGames.filter { game ->
                game.name.contains(query, ignoreCase = true)
            }
        }

        adapter.submitList(filteredList)
        updateEmptyState(filteredList.isEmpty())
    }

    private fun toggleGameSelection(game: Game) {
        if (selectedGames.contains(game)) {
            selectedGames.remove(game)
        } else {
            selectedGames.add(game)
        }
        adapter.updateSelection(selectedGames)
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        val count = selectedGames.size
        binding.selectedCountChip.apply {
            text = "$count selected"
            visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.gamesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    companion object {
        const val REQUEST_KEY = "game_picker_request"
        const val RESULT_SELECTED_GAMES = "selected_games"
        private const val ARG_PRESELECTED_GAMES = "preselected_games"

        fun newInstance(preselectedGames: List<SimplifiedGame>? = null): GamePickerFragment {
            return GamePickerFragment().apply {
                arguments = bundleOf(
                    ARG_PRESELECTED_GAMES to preselectedGames?.let { ArrayList(it) }
                )
            }
        }
    }
}