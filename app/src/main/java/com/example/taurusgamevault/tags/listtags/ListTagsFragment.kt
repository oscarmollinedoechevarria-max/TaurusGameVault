package com.example.taurusgamevault.tags.listtags

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.TagsAdapter
import com.example.taurusgamevault.databinding.FragmentListTagsBinding

class ListTagsFragment : Fragment() {

    private val viewModel: ListTagsViewModel by viewModels()

    lateinit var binding: FragmentListTagsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListTagsBinding.inflate(inflater, container, false)

        viewModel.getTags(requireContext())

        viewModel.tags?.observe(viewLifecycleOwner) { tags ->
            binding.listTagsImportRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.listTagsImportRecyclerView.adapter = TagsAdapter(tags) { tag ->
                val action = ListTagsFragmentDirections.actionListTagsFragmentToListByTagFragment(tag.tag_id, tag.name)
                findNavController().navigate(action)
            }
        }

        binding.fabAddTag.setOnClickListener {
            findNavController().navigate(R.id.action_listTagsFragment_to_createTagsFragment)
        }

        return binding.root
    }
}