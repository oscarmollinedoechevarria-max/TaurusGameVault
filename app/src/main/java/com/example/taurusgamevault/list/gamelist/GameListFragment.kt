package com.example.taurusgamevault.list.gamelist

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.ListGameAdapter
import com.example.taurusgamevault.databinding.FragmentGameListBinding

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

        viewModel.list?.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.tvEmptyLists.visibility = View.VISIBLE
                binding.listsRecyclerView.visibility = View.GONE
            } else {
                binding.tvEmptyLists.visibility = View.GONE
                binding.listsRecyclerView.visibility = View.VISIBLE

                recyclerview.adapter = ListGameAdapter(list, requireContext(), findNavController(), lifecycleScope)

            }
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_gameListFragment_to_createListFragment)
        }


        return binding.root
    }
}