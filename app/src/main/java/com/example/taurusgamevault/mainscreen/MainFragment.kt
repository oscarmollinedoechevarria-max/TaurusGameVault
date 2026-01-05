package com.example.taurusgamevault.mainscreen

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taurusgamevault.R
import com.example.taurusgamevault.adapters.GameAdapter
import com.example.taurusgamevault.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    lateinit var binding: FragmentMainBinding

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

        viewModel.games?.observe(viewLifecycleOwner, Observer { llistat ->
            recyclerview.adapter = GameAdapter(llistat)
        })

        return binding.root
    }
}