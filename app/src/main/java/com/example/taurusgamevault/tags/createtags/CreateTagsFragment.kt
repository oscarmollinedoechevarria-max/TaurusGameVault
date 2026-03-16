package com.example.taurusgamevault.tags.createtags

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.taurusgamevault.Model.room.entities.Tag
import com.example.taurusgamevault.R
import com.example.taurusgamevault.databinding.FragmentCreateTagsBinding

class CreateTagsFragment : Fragment() {

    private val viewModel: CreateTagsViewModel by viewModels()

    lateinit var binding: FragmentCreateTagsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCreateTagsBinding.inflate(inflater)

        binding.button.setOnClickListener {
            val tagName = binding.editTextText.text.toString()

            if(tagName.isEmpty()){
                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
            }
            else{
                val tag = Tag(
                    name = tagName,
                    image = ""
                )

                viewModel.saveTag(requireContext(), tag)

                Toast.makeText(requireContext(), "Tag created", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_createTagsFragment_to_listTagsFragment)

            }

        }

        return inflater.inflate(R.layout.fragment_create_tags, container, false)
    }
}