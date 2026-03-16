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
        setupFabs()
        observeGames()

        return binding.root
    }

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
            binding.searchBar.visibility = View.GONE
            binding.labelTags.visibility = View.GONE
            binding.tagsRecyclerView.visibility = View.GONE
            binding.fabSearch.visibility = View.VISIBLE
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

    private fun setupFabs() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createGameFragment)
        }

        binding.fabSearch.setOnClickListener {
            binding.searchBar.visibility = View.VISIBLE
            binding.fabSearch.visibility = View.GONE
            if (tagsAdapter != null) {
                binding.labelTags.visibility = View.VISIBLE
                binding.tagsRecyclerView.visibility = View.VISIBLE
            }
            binding.etSearchQuery.requestFocus()
        }
    }

    private fun observeGames() {
        viewModel.games.observe(viewLifecycleOwner) { games ->
            val isEmpty = games.isNullOrEmpty()
            binding.tvEmptyGames.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.gamesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.fabSearch.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (!isEmpty) {
                binding.gamesRecyclerView.adapter = GameAdapter(
                    games, requireContext(), findNavController(), lifecycleScope
                )
            }
        }
    }
}
