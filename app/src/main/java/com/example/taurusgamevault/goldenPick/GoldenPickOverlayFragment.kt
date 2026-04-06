package com.example.taurusgamevault.goldenPick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.R.attr.alpha
import androidx.core.view.ViewCompat.animate
import android.R.attr.translationX
import android.view.View
import androidx.fragment.app.viewModels
import com.example.taurusgamevault.databinding.FragmentGoldenPickOverlayBinding
import com.example.taurusgamevault.list.gamelistdetail.GameListDetailViewModel
import kotlin.getValue

class GoldenPickOverlayFragment : Fragment() {

    private val viewModel: GoldenPickOverlayViewModel by viewModels()
    private lateinit var binding: FragmentGoldenPickOverlayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoldenPickOverlayBinding.inflate(inflater, container, false)

        setupListeners()
        setupAnimations()

        viewModel.getVaultStats(requireContext())

        viewModel.vaultStats?.observe(viewLifecycleOwner) { stats ->

            stats?.let {
                updateVaultStats(
                    totalHours = it.totalPlaytime,
                    completedCount = it.completedCount,
                    backlogCount = it.pendingCount
                )
            }
        }
        
        return binding.root
    }

    private fun setupListeners() {
        binding.btnCloseVault.setOnClickListener { dismiss() }

        binding.overlayContainer.setOnClickListener { dismiss() }

        binding.vaultCard.setOnClickListener { /* click */ }

        binding.btnGoldenPick.setOnClickListener {
            // random
        }
    }

    private fun setupAnimations() {
        binding.vaultCard.apply {
            alpha = 0f
            translationX = 100f
            animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(300)
                .start()
        }
    }

    fun updateVaultStats(totalHours: Int, completedCount: Int, backlogCount: Int) {
        if (isAdded) {
            binding.tvTotalHours.text = String.format("%,d hrs", totalHours)
            binding.tvCompletedCount.text = completedCount.toString()
            binding.tvBacklogCount.text = backlogCount.toString()
        }
    }

    private fun dismiss() {
        binding.vaultCard.animate()
            .alpha(0f)
            .translationX(100f)
            .setDuration(250)
            .withEndAction {
                if (isAdded) parentFragmentManager.popBackStack()
            }
            .start()
    }

    companion object {
        fun newInstance(): GoldenPickOverlayFragment {
            return GoldenPickOverlayFragment()
        }
    }
}
