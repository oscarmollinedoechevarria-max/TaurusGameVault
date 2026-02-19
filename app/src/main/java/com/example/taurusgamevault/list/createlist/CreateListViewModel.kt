package com.example.taurusgamevault.list.createlist

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game
import com.example.taurusgamevault.classes.ListTempData
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class CreateListViewModel : ViewModel() {
    fun saveList(context: Context, listTempData: ListTempData) {
        viewModelScope.launch {
            val gameImage: String? = listTempData.image?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }

                    val compressedFile = Compressor.compress(context, tempFile) {
                        quality(80)
                        format(Bitmap.CompressFormat.JPEG)
                    }

                    try {
                        Repository.uploadListImageAndGetPublicUrl(compressedFile)
                    } finally {
                        tempFile.delete()
                    }
                }
            }

            val list = GameList(
                name = listTempData.name,
                description = listTempData.description,
                image = gameImage,
            )

            val listID = Repository.addList(context, list)

            listTempData.games?.forEach { gameID ->
                val temp = List_game(
                    list_id = listID,
                    game_id = gameID
                )

                Repository.addListGame(context, temp)
            }

            Toast.makeText(context, "List saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

}