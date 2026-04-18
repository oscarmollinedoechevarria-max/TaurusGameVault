package com.example.taurusgamevault.list.gamelist

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.ListGameAdapter
import com.example.taurusgamevault.databinding.FragmentGameListBinding
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.adapters.TagsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GameListFragment : Fragment() {

    private val viewModel: GameListViewModel by viewModels()

    lateinit var binding: FragmentGameListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameListBinding.inflate(inflater)

        val recyclerview: RecyclerView = binding.listsRecyclerView

        recyclerview.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getList(requireContext())

        viewModel.list.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.tvEmptyLists.visibility = View.VISIBLE
                binding.listsRecyclerView.visibility = View.GONE
            } else {
                binding.tvEmptyLists.visibility = View.GONE
                binding.listsRecyclerView.visibility = View.VISIBLE

                recyclerview.adapter = ListGameAdapter(list, requireContext(), findNavController(), lifecycleScope)

            }
        }

        setupMenu()
        setupSearchBar()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_gameListFragment_to_createListFragment)
        }


        return binding.root
    }

    // setup game list superior menu
    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_game_list_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        toggleSearchBarVisibility()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupSearchBar() {
        binding.btnCloseSearch.setOnClickListener {
            toggleSearchBarVisibility()
            binding.etSearchQuery.text.clear()
            viewModel.searchList("")
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchQuery.text.toString().trim()
            viewModel.searchList(query)
        }
    }

    fun toggleSearchBarVisibility() {
        val isVisible = binding.searchBar.visibility == View.VISIBLE
        binding.searchBar.visibility = if (isVisible) View.GONE else View.VISIBLE
        if (!isVisible) {
            binding.etSearchQuery.requestFocus()
        }
    }
}
