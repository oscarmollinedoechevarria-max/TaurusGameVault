package com.example.taurusgamevault.objectives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.FragmentObjectivesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ObjectivesFragment : Fragment() {

    private val viewModel: ObjectivesViewModel by viewModels()
    private val args: ObjectivesFragmentArgs by navArgs()
    lateinit var binding: FragmentObjectivesBinding
    private lateinit var adapter: ObjectivesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentObjectivesBinding.inflate(inflater)

        setupRecyclerView()
        setupFab()
        setupMenu()
        observeObjectives()
        observeEditMode()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = ObjectivesAdapter(
            onChecked = { objective ->
                viewModel.toggleCompleted(requireContext(), objective)
            },
            onDelete = { objective ->
                viewModel.deleteObjective(requireContext(), objective)
            }
        )
        binding.objectivesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.objectivesRecyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddObjective.visibility = View.GONE
        binding.fabAddObjective.setOnClickListener {
            showAddObjectiveDialog()
        }
    }

    private fun showAddObjectiveDialog() {
        val input = EditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Objective")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotBlank()) {
                    viewModel.addObjective(requireContext(), args.gameId, title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.objectives_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_toggle_edit -> {
                        viewModel.toggleEditMode()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeObjectives() {
        viewModel.getLiveObjectives(requireContext(), args.gameId)
            .observe(viewLifecycleOwner) { objectives ->
                val isEmpty = objectives.isNullOrEmpty()
                binding.tvEmptyObjectives.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.objectivesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                adapter.updateData(objectives ?: emptyList(), viewModel.editMode.value ?: false)
            }
    }

    private fun observeEditMode() {
        viewModel.editMode.observe(viewLifecycleOwner) { isEditMode ->
            binding.fabAddObjective.visibility = if (isEditMode) View.VISIBLE else View.GONE
            adapter.updateEditMode(isEditMode)
        }
    }
}