package com.example.taurusgamevault.importgog

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.FragmentImportGamesGogBinding
import com.example.taurusgamevault.importgamesigdb.ImportState



class ImportGamesGOGFragment : Fragment() {

    private val viewModel: ImportGamesGOGViewModel by viewModels()
    private lateinit var binding: FragmentImportGamesGogBinding

    private val GOG_CLIENT_ID = "46899977096215655"
    private val GOG_CLIENT_SECRET = "9d85c43b1482497dbbce61f6e4aa173a433796eeae2ca8c5f6129f2dc4de46d9"
    private val REDIRECT_URI = "https://embed.gog.com/on_login_success?origin=client"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportGamesGogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            showGOGLoginWebView()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ImportState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = ""
                }
                is ImportState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressBar.isVisible = true
                    binding.progressBar.max = state.total
                    binding.progressBar.progress = state.current
                    binding.tvProgress.text = if (state.total == 0)
                        state.currentGame
                    else
                        "${state.currentGame} (${state.current}/${state.total})"
                }
                is ImportState.Done -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = "✓ Completed"
                }
                is ImportState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.isVisible = false
                    binding.tvProgress.text = "Error: ${state.message}"
                }
            }
        }
    }

    private fun showGOGLoginWebView() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val webView = WebView(requireContext())

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled  = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // detect correct redirection for gog
                if (url != null && url.startsWith("https://embed.gog.com/on_login_success")) {
                    val uri  = Uri.parse(url)
                    val code = uri.getQueryParameter("code")

                    if (!code.isNullOrEmpty()) {
                        dialog.dismiss()
                        viewModel.fetchTokenAndImport(
                            requireContext(), code,
                            GOG_CLIENT_ID, GOG_CLIENT_SECRET, REDIRECT_URI
                        )
                    }
                }
            }
        }

        // URL auth with correct direction
        val authUrl = "https://auth.gog.com/auth" +
                "?client_id=$GOG_CLIENT_ID" +
                "&redirect_uri=${Uri.encode(REDIRECT_URI)}" +
                "&response_type=code" +
                "&layout=client2"

        webView.loadUrl(authUrl)
        dialog.setContentView(webView)
        dialog.show()
    }
}