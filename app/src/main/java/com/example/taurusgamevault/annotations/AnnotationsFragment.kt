package com.example.taurusgamevault.annotations

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.taurusgamevault.databinding.FragmentAnnotationsBinding
import android.webkit.WebViewClient
import jp.wasabeef.richeditor.RichEditor

class AnnotationsFragment : Fragment() {

    private val viewModel: AnnotationsViewModel by viewModels()
    private val args: AnnotationsFragmentArgs by navArgs()
    lateinit var binding: FragmentAnnotationsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnotationsBinding.inflate(inflater)

        setupEditor()
        setupToolbar()

        viewModel.loadAnnotation(requireContext(), args.gameId)

        viewModel.annotation.observe(viewLifecycleOwner) { annotation ->
            if (annotation != null) {
                binding.editor.setHtml(annotation.text)
            }
        }

        binding.btnSaveAnnotation.setOnClickListener {
            val htmlContent = binding.editor.html ?: ""
            viewModel.saveAnnotation(requireContext(), args.gameId, htmlContent)
            Toast.makeText(requireContext(), "Annotation saved", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun setupEditor() {
        binding.editor.apply {
            setEditorFontSize(55)
            setPlaceholder("Start writing here your annotations...")
            setPadding(16, 16, 16, 16)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowContentAccess = true
        }

        // Acceder al WebView interno de RichEditor para interceptar clicks
        val fieldName = listOf("mWebView", "webView", "mEditor").firstOrNull { name ->
            runCatching { RichEditor::class.java.getDeclaredField(name) }.isSuccess
        }

        if (fieldName != null) {
            try {
                val field = RichEditor::class.java.getDeclaredField(fieldName)
                field.isAccessible = true
                val webView = field.get(binding.editor) as? WebView
                webView?.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val uri = request.url
                        if (uri.host?.contains("youtube.com") == true ||
                            uri.host?.contains("youtu.be") == true
                        ) {
                            startActivity(Intent(Intent.ACTION_VIEW, uri))
                            return true
                        }
                        return false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupToolbar() {
        val editor = binding.editor

        binding.btnBold.setOnClickListener { editor.setBold() }
        binding.btnItalic.setOnClickListener { editor.setItalic() }
        binding.btnUnderline.setOnClickListener { editor.setUnderline() }
        binding.btnStrike.setOnClickListener { editor.setStrikeThrough() }

        binding.btnHighlightYellow.setOnClickListener {
            editor.setTextBackgroundColor(Color.YELLOW)
        }
        binding.btnHighlightGreen.setOnClickListener {
            editor.setTextBackgroundColor(Color.parseColor("#90EE90"))
        }
        binding.btnTextRed.setOnClickListener {
            editor.setTextColor(Color.RED)
        }
        binding.btnTextBlue.setOnClickListener {
            editor.setTextColor(Color.BLUE)
        }

        binding.btnH1.setOnClickListener { editor.setHeading(1) }
        binding.btnH2.setOnClickListener { editor.setHeading(2) }
        binding.btnBlockquote.setOnClickListener { editor.setBlockquote() }
        binding.btnBullets.setOnClickListener { editor.setBullets() }
        binding.btnCheckbox.setOnClickListener { editor.insertTodo() }

        binding.btnLink.setOnClickListener { showInsertLinkDialog() }
        binding.btnYoutube.setOnClickListener { showInsertYoutubeDialog() }

        binding.btnUndo.setOnClickListener { editor.undo() }
        binding.btnRedo.setOnClickListener { editor.redo() }
    }

    private fun showInsertLinkDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }
        val etUrl = EditText(requireContext()).apply { hint = "https://..." }
        val etText = EditText(requireContext()).apply { hint = "Link text" }
        layout.addView(etUrl)
        layout.addView(etText)

        AlertDialog.Builder(requireContext())
            .setTitle("Insert Link")
            .setView(layout)
            .setPositiveButton("Insert") { _, _ ->
                val url = etUrl.text.toString()
                val text = etText.text.toString()
                if (url.isNotBlank()) {
                    binding.editor.insertLink(url, text.ifBlank { url })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInsertYoutubeDialog() {
        val etUrl = EditText(requireContext()).apply {
            hint = "https://www.youtube.com/watch?v=..."
            setPadding(48, 16, 48, 0)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Insert YouTube")
            .setView(etUrl)
            .setPositiveButton("Insert") { _, _ ->
                val url = etUrl.text.toString()
                if (url.isNotBlank()) {
                    insertYoutubeEmbed(url)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun insertYoutubeEmbed(url: String) {
        val videoId = extractYoutubeId(url)
        if (videoId == null) {
            Toast.makeText(requireContext(), "URL de YouTube inválida", Toast.LENGTH_SHORT).show()
            return
        }

        val embedHtml = "<br>" +
                "<a href='https://www.youtube.com/watch?v=$videoId' target='_blank' " +
                "style='display:block;position:relative;text-decoration:none;'>" +
                "<img src='https://img.youtube.com/vi/$videoId/hqdefault.jpg' " +
                "style='width:100%;border-radius:8px;' />" +
                "<span style='position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);" +
                "background:rgba(0,0,0,0.7);border-radius:50%;width:56px;height:56px;" +
                "display:flex;align-items:center;justify-content:center;" +
                "font-size:24px;color:white;'>&#9654;</span>" +
                "</a><br>"

        binding.editor.focusEditor()
        val escaped = embedHtml.replace("'", "\\'")
        binding.editor.evaluateJavascript(
            "document.execCommand('insertHTML', false, '$escaped');",
            null
        )
    }

    private fun extractYoutubeId(url: String): String? {
        val patterns = listOf(
            Regex("(?:youtube\\.com/watch\\?v=)([a-zA-Z0-9_-]{11})"),
            Regex("(?:youtu\\.be/)([a-zA-Z0-9_-]{11})"),
            Regex("(?:youtube\\.com/shorts/)([a-zA-Z0-9_-]{11})")
        )
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) return match.groupValues[1]
        }
        return null
    }
}