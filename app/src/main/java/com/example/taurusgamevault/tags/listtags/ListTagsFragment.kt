package com.example.taurusgamevault.tags.listtags

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
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.TagsAdapter
import com.example.taurusgamevault.databinding.FragmentListTagsBinding
import com.example.taurusgamevault.Model.Repository.Repository
import kotlinx.coroutines.launch
import android.widget.Toast

class ListTagsFragment : Fragment() {

    private val viewModel: ListTagsViewModel by viewModels()
    lateinit var binding: FragmentListTagsBinding
    private var tagsAdapter: TagsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListTagsBinding.inflate(inflater, container, false)

        viewModel.getTags(requireContext())

        viewModel.tags.observe(viewLifecycleOwner) { tags ->
            if (tags.isNullOrEmpty()) {
                binding.listTagsImportRecyclerView.visibility = View.GONE
            } else {
                binding.listTagsImportRecyclerView.visibility = View.VISIBLE
                binding.listTagsImportRecyclerView.layoutManager =
                    LinearLayoutManager(requireContext())

                tagsAdapter = TagsAdapter(
                    items = tags,
                    selectable = false,
                    onItemClick = { tag ->
                        val action = ListTagsFragmentDirections.actionListTagsFragmentToListByTagFragment(
                            tagId = tag.tag_id,
                            tagName = tag.name,
                        )
                        findNavController().navigate(action)
                    },
                    onEditClick = { tag ->
                        val action = ListTagsFragmentDirections.actionListTagsFragmentToCreateTagsFragment(
                            tagId = tag.tag_id,
                            tagName = tag.name,
                            tagImage = tag.image
                        )
                        findNavController().navigate(action)
                    },
                    onDeleteClick = { tag ->
                        lifecycleScope.launch {
                            Repository.deleteTag(requireContext(), tag)
                            Toast.makeText(requireContext(), "Tag deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                binding.listTagsImportRecyclerView.adapter = tagsAdapter
            }
        }

        setupMenu()
        setupSearchBar()

        binding.fabAddTag.setOnClickListener {
            val action = ListTagsFragmentDirections.actionListTagsFragmentToCreateTagsFragment(
                tagId = -1L,
                tagName = "",
                tagImage = ""
            )
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_list_tags_menu, menu)
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
            viewModel.searchTags("")
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchQuery.text.toString().trim()
            viewModel.searchTags(query)
        }
    }

    fun toggleSearchBarVisibility() {
        val isVisible = binding.searchBar.isVisible
        binding.searchBar.isVisible = !isVisible
        if (!isVisible) {
            binding.etSearchQuery.requestFocus()
        }
    }
}
