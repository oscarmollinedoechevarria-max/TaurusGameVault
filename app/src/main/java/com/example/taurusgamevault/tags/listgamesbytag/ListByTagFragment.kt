package com.example.taurusgamevault.tags.listgamesbytag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.adapters.GameAdapter
import com.example.taurusgamevault.databinding.FragmentListByTagBinding

class ListByTagFragment : Fragment() {
    private val viewModel: ListByTagViewModel by viewModels()
    private val args: ListByTagFragmentArgs by navArgs()
    
    lateinit var binding: FragmentListByTagBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getGamesByTag(requireContext(), args.tagId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListByTagBinding.inflate(inflater, container, false)

        val recyclerview: RecyclerView = binding.recyclerViewGamesByTag
        recyclerview.layoutManager = LinearLayoutManager(requireContext())

        viewModel.games?.observe(viewLifecycleOwner, Observer { games ->
            if (games.isNullOrEmpty()) {
                binding.tvEmptyList.visibility = View.VISIBLE
                binding.recyclerViewGamesByTag.visibility = View.GONE
            } else {
                binding.tvEmptyList.visibility = View.GONE
                binding.recyclerViewGamesByTag.visibility = View.VISIBLE
                recyclerview.adapter = GameAdapter(games, requireContext(), findNavController(), lifecycleScope,
                    onItemClick = { game -> findNavController().navigate(
                        ListByTagFragmentDirections.actionListByTagFragmentToGameDetailFragment(gameId = game.game_id, game.name, editMode = false)
                            ) },
                    false)
            }
        })

        return binding.root
    }
}
