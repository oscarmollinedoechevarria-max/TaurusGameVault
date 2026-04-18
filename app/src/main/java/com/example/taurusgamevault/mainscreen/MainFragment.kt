package com.example.taurusgamevault.mainscreen

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.GameAdapter
import com.example.taurusgamevault.databinding.FragmentMainBinding
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.SharedViewModel
import com.example.taurusgamevault.adapters.TagsAdapter
import com.example.taurusgamevault.goldenPick.GoldenPickOverlayFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    lateinit var binding: FragmentMainBinding
    private var tagsAdapter: TagsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getGames(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)

        setupMenu()
        setupGamesRecyclerView()
        setupTagsRecyclerView()
        setupSearchBar()
        setupAddFab()
        setupGoldenPickFab()
        setupSelectTagsFab()
        setupGoldenPickResultListener()

        observeGames()

        return binding.root
    }

    // setup main fragment superior menu
    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_fragment_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_import_games_auto -> {
                        findNavController().navigate(R.id.action_mainFragment_to_plataformsImportFragment)
                        true
                    }
                    R.id.action_search -> {
                        toggleSearchBarVisibility()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupGamesRecyclerView() {
        binding.gamesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // setup tags recycler view
    private fun setupTagsRecyclerView() {
        binding.labelTags.visibility = View.GONE
        binding.tagsRecyclerView.visibility = View.GONE

        Repository.getTags(requireContext())?.observe(viewLifecycleOwner) { tags ->
            if (tags.isNullOrEmpty()) {
                tagsAdapter = null
                return@observe
            }

            tagsAdapter = TagsAdapter(
                items = tags,
                onItemClick = {
                    tagsAdapter?.let { adapter ->
                        viewModel.filterByTags(requireContext(), adapter.getSelectedTags())
                    }
                },
                selectable = true,
                onEditClick = null,
                onDeleteClick = null
            )
            binding.tagsRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.tagsRecyclerView.adapter = tagsAdapter
        }
    }

    private fun setupSearchBar() {
        binding.btnCloseSearch.setOnClickListener {
            toggleSearchBarVisibility()
            binding.etSearchQuery.text.clear()
            viewModel.searchGames("")
            tagsAdapter?.clearSelection()
            viewModel.filterByTags(requireContext(), emptySet())
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchQuery.text.toString().trim()
            viewModel.searchGames(query)
        }
    }

    private fun setupAddFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createGameFragment)
        }
    }

    private fun setupGoldenPickFab() {
        binding.fabGoldenPick.setOnClickListener {
            showGoldenPickOverlay()
        }
    }

    // register one time the listener for the golden pick overlay
    private fun setupGoldenPickResultListener() {
        childFragmentManager.setFragmentResultListener(
            "overlay_request_key",
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("should_navigate")) {
                val gameId = bundle.getLong("game_id", -1L)
                val gameName = bundle.getString("game_name") ?: ""

                if (gameId != -1L) {
                    val action = MainFragmentDirections.actionMainFragmentToGameDetailFragment(
                        gameId, gameName, false
                    )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupSelectTagsFab() {
        binding.fabSelectTags.setOnClickListener {
            showTagSelectionDialog()
        }
    }

    // show the golden pick overlay(istance of GoldenPickOverlayFragment)
    private fun showGoldenPickOverlay() {
        GoldenPickOverlayFragment.newInstance()
            .show(childFragmentManager, "golden_pick_overlay")
    }

    fun toggleSearchBarVisibility() {
        val isVisible = binding.searchBar.visibility == View.VISIBLE
        binding.fabSelectTags.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.fabGoldenPick.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.searchBar.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.labelTags.visibility = if (isVisible || tagsAdapter == null) View.GONE else View.VISIBLE
        binding.tagsRecyclerView.visibility = if (isVisible || tagsAdapter == null) View.GONE else View.VISIBLE
        if (!isVisible) {
            binding.etSearchQuery.requestFocus()
        }
    }

    // observe the games list and update the ui accordingly
    private fun observeGames() {
        viewModel.games.observe(viewLifecycleOwner) { games ->
            val isEmpty = games.isNullOrEmpty()
            binding.tvEmptyGames.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.gamesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (!isEmpty) {
                binding.gamesRecyclerView.adapter = GameAdapter(
                    games, requireContext(), findNavController(), lifecycleScope
                )
            }
        }
    }

    private fun showTagSelectionDialog() {
        Repository.getTags(requireContext())?.observe(viewLifecycleOwner) { tags ->
            if (tags.isNullOrEmpty()) {
                return@observe
            }

            val tagNames = tags.map { it.name }.toTypedArray()
            val selectedTagsBooleans = BooleanArray(tagNames.size) { i ->
                tagsAdapter?.getSelectedTags()?.contains(tags[i]) ?: false
            }
            val preSelectedTags = tagsAdapter?.getSelectedTags()?.toMutableSet() ?: mutableSetOf()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Tags")
                .setMultiChoiceItems(tagNames, selectedTagsBooleans) { _, which, isChecked ->
                    if (isChecked) {
                        preSelectedTags.add(tags[which])
                    } else {
                        preSelectedTags.remove(tags[which])
                    }
                }
                .setPositiveButton("OK") { dialog, _ ->
                    tagsAdapter?.setSelectedTags(preSelectedTags)
                    viewModel.filterByTags(requireContext(), preSelectedTags)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}