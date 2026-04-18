package com.example.taurusgamevault.goldenPick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.R.attr.alpha
import androidx.core.view.ViewCompat.animate
import android.R.attr.translationX
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.FragmentGoldenPickOverlayBinding
import com.example.taurusgamevault.SharedViewModel
import com.example.taurusgamevault.list.gamelistdetail.GameListDetailViewModel
import kotlin.getValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast

class GoldenPickOverlayFragment : DialogFragment() {
    private val viewModel: GoldenPickOverlayViewModel by viewModels()
    private lateinit var binding: FragmentGoldenPickOverlayBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setGravity(Gravity.TOP)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.TransparentOverlayDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoldenPickOverlayBinding.inflate(inflater, container, false)

        setupListeners()
        setupAnimations()

        viewModel.getVaultStats(requireContext())
        setupObservers()

        return binding.root
    }

    private fun setupListeners() {
        binding.btnCloseVault.setOnClickListener { closeOverlay() }
        binding.overlayContainer.setOnClickListener { closeOverlay() }

        binding.btnGoldenPick.setOnClickListener {
            viewModel.pickRandomGame(requireContext())
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

    private fun setupObservers() {
        viewModel.vaultStats?.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                updateVaultStats(
                    totalHours = it.totalPlaytime,
                    completedCount = it.completedCount,
                    backlogCount = it.pendingCount
                )
            }
        }

        viewModel.gamePickedEvent.observe(viewLifecycleOwner) { game ->
            if (game != null) {
                closeOverlay(shouldNavigate = true, gameId = game.first, gameName = game.second)  // renombrado
            }
        }

        viewModel.errorNoGames.observe(viewLifecycleOwner) { hasError ->
            if (hasError == true) {
                Toast.makeText(requireContext(), "No pending games in your backlog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateVaultStats(totalHours: Int, completedCount: Int, backlogCount: Int) {
        if (isAdded) {
            binding.tvTotalHours.text = String.format("%,d hrs", totalHours)
            binding.tvCompletedCount.text = completedCount.toString()
            binding.tvBacklogCount.text = backlogCount.toString()
        }
    }

    private fun closeOverlay(shouldNavigate: Boolean = false, gameId: Long? = null, gameName: String? = null) {
        binding.vaultCard.animate()
            .alpha(0f)
            .translationX(100f)
            .setDuration(250)
            .withEndAction {
                if (isAdded) {
                    if (shouldNavigate && gameId != null) {
                        parentFragmentManager.setFragmentResult(
                            "overlay_request_key",
                            bundleOf(
                                "should_navigate" to true,
                                "game_id" to gameId,
                                "game_name" to gameName
                            )
                        )
                    }
                    dismissAllowingStateLoss()
                }
            }
            .start()
    }

    companion object {
        fun newInstance() = GoldenPickOverlayFragment()
    }
}