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
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.adapters.TagsAdapter
import com.example.taurusgamevault.goldenPick.GoldenPickOverlayFragment

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
                selectable = true
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

    // show the golden pick overlay(istance of GoldenPickOverlayFragment)
    private fun showGoldenPickOverlay() {
        val fragment = GoldenPickOverlayFragment.newInstance()

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .add(android.R.id.content, fragment, "golden_pick_overlay")
            .addToBackStack(null)
            .commit()
    }

    fun toggleSearchBarVisibility() {
        val isVisible = binding.searchBar.visibility == View.VISIBLE
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
}