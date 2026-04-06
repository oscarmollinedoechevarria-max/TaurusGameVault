package com.example.taurusgamevault.gamepicker

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.Model.room.entities.Game
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.GamePickerAdapter
import com.example.taurusgamevault.databinding.FragmentGamePickerBinding
import android.text.TextWatcher
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.taurusgamevault.list.createlist.CreateListViewModel
import androidx.fragment.app.setFragmentResult
import com.example.taurusgamevault.Model.room.entities.toSimplifiedGame
import com.example.taurusgamevault.classes.SimplifiedGame

class GamePickerFragment : DialogFragment() {

    lateinit var binding: FragmentGamePickerBinding
    private lateinit var adapter: GamePickerAdapter
    private var allGames: List<Game>? = null
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
        loadGames()
        restorePreselectedGames()
    }

    override fun onStart() {
        super.onStart()

        // Set the dialog size for fit screen
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.9).toInt()
        )
    }

    private fun setupRecyclerView() {
        adapter = GamePickerAdapter(
            onGameClick = { game ->
                toggleGameSelection(game)
            },
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
            val simplifiedGames = selectedGames.map {  it.toSimplifiedGame() }
            // Send the selected games back to the previous fragment
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_SELECTED_GAMES to ArrayList(simplifiedGames))
            )
            dismiss()
        }
    }

    private fun loadGames() {
        viewModel.getGames(requireContext())
        viewModel.games?.observe(viewLifecycleOwner) { games ->
            allGames = games
            adapter.submitList(games)
        }
    }

    // Restore preselected games from arguments
    private fun restorePreselectedGames() {
        arguments?.getParcelableArrayList<SimplifiedGame>(ARG_PRESELECTED_GAMES)?.let { preselected ->
            val preselectedGames = preselected.mapNotNull { simplified ->
                allGames?.find { it.name == simplified.name }
            }
            selectedGames.clear()
            selectedGames.addAll(preselectedGames)
            adapter.updateSelection(selectedGames)
            updateSelectedCount()
        }
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

    //keys for fragment result
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