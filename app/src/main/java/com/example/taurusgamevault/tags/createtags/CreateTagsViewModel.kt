package com.example.taurusgamevault.tags.createtags

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.Tag
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.taurusgamevault.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class CreateTagsViewModel : ViewModel() {

    // state flow for messages
    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    // navigation to fragment
    private val _navigateToLibrary = MutableSharedFlow<Boolean>()
    val navigateToLibrary = _navigateToLibrary.asSharedFlow()

    fun saveTag(context: Context, tag: Tag, updateTag: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isLocalUri = tag.image?.startsWith("content://") == true ||
                        tag.image?.startsWith("file://") == true

                var finalImageUrl = tag.image

                if (isLocalUri && tag.image != null) {
                    val parsedUri = tag.image!!.toUri()

                    context.contentResolver.openInputStream(parsedUri)?.use { input ->
                        val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                        var compressedFile: File? = null

                        try {
                            tempFile.outputStream().use { output -> input.copyTo(output) }

                            compressedFile = Compressor.compress(context, tempFile) {
                                quality(80)
                                format(Bitmap.CompressFormat.JPEG)
                            }

                            finalImageUrl = if (updateTag) {
                                Repository.updateTagImageAndGetPublicUrl(
                                    context, tag.tag_id, compressedFile, tag.image
                                )
                            } else {
                                Repository.uploadTagImageAndGetPublicUrl(compressedFile)
                            }
                        } finally {
                            tempFile.delete()
                            compressedFile?.delete()
                        }
                    }
                }

                val updatedTag = tag.copy(image = finalImageUrl)

                if (updateTag) {
                    Repository.updateTag(context, updatedTag)
                    _result.value = "Tag updated"
                } else {
                    Repository.addTag(context, updatedTag)
                    _result.value = "Tag created"
                }

                withContext(Dispatchers.Main) {
                    _navigateToLibrary.emit(true)
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving tag", e)
                _result.value = "Error: ${e.message}"
            }
        }
    }
}