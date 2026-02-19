package com.example.taurusgamevault.mainscreen

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.GameAdapter
import com.example.taurusgamevault.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    //TODO: FIX IMAGE REFRESHMENT AND CACHE
    //TODO: add screenshots scroll view in xml game create view
    //TODO: add more data to game cards
    private val viewModel: MainViewModel by viewModels()

    lateinit var binding: FragmentMainBinding

    private lateinit var adapter: GameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getGames(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)

        val recyclerview: RecyclerView = binding.gamesRecyclerView

        recyclerview.layoutManager = LinearLayoutManager(requireContext())

        viewModel.games?.observe(viewLifecycleOwner, Observer { games ->
            recyclerview.adapter = GameAdapter(games, requireContext(), findNavController(), lifecycleScope)
        })

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createGameFragment)
        }

        return binding.root
    }


}