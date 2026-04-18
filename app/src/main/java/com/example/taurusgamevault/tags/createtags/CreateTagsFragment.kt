package com.example.taurusgamevault.tags.createtags

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.FragmentCreateTagsBinding
import kotlinx.coroutines.launch

class CreateTagsFragment : Fragment() {

    private val viewModel: CreateTagsViewModel by viewModels()
    private val args: CreateTagsFragmentArgs by navArgs()

    lateinit var binding: FragmentCreateTagsBinding

    private var selectedImageUri: Uri? = null

    private var isEditMode: Boolean = false

    private var tagId: Long = 0L

    // pick single image
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.imageViewTag.setImageURI(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateTagsBinding.inflate(inflater)

        tagId = args.tagId
        val tagName = args.tagName
        val tagImage = args.tagImage

        if (tagId != -1L) {
            isEditMode = true
            binding.editTextText.setText(tagName)
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Editing tag: $tagName"
            binding.buttonCreateTag.text = "Update"
            binding.textView3.text = "Edit Tag"

            if (!tagImage.isNullOrEmpty() && tagImage != "null") {
                binding.imageViewTag.load(tagImage)
            }
        } else {
            isEditMode = false
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Create Tag"
            binding.buttonCreateTag.text = "Create Tag"
        }

        binding.buttonAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.buttonCreateTag.setOnClickListener {
            val name = binding.editTextText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imageToSave = selectedImageUri?.toString() ?: tagImage ?: ""

            val tag = Tag(
                name = name,
                image = imageToSave
            )


            if(isEditMode){
                tag.tag_id = tagId
            }

            viewModel.saveTag(requireContext(), tag, isEditMode)
        }

        setupObservers()

        return binding.root
    }

    //observers for navigation and messages
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.result.collect { msg ->
                msg?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToLibrary.collect { shouldNavigate ->
                if (shouldNavigate) {
                    findNavController().navigate(R.id.action_createTagsFragment_to_listTagsFragment)
                }
            }
        }
    }
}