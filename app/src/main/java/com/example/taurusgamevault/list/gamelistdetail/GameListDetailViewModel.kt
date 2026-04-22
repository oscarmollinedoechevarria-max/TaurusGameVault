package com.example.taurusgamevault.list.gamelistdetail

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.Model.room.entities.List_game
import com.example.taurusgamevault.classes.SimplifiedGame
import com.example.taurusgamevault.Model.room.entities.toSimplifiedGame
import com.example.taurusgamevault.classes.ListTempData
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class GameListDetailViewModel : ViewModel() {
    private var _list: LiveData<GameList>? = null
    val list: LiveData<GameList>? get() = _list

    private var _games: LiveData<List<SimplifiedGame>>? = null
    val games: LiveData<List<SimplifiedGame>>? get() = _games

    fun getList(context: Context, gameListId: Long) {
        _list = Repository.getList(context, gameListId)
    }

    fun getGames(context: Context, gameListId: Long) {
        val gamesList = Repository.getListGamesById(context, gameListId)

        _games = gamesList?.map { games ->
            games.map { game -> game.toSimplifiedGame() }
        }
    }

    fun saveList(context: Context, listTempData: ListTempData, gameListId: Long, oldListImage: String?) {
        // upload list cover
        viewModelScope.launch {
            val gameImage: String? = if (listTempData.image != null) {
                val inputStream = context.contentResolver.openInputStream(listTempData.image)
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
                        Repository.updateListImageAndGetPublicUrl(context, gameListId, compressedFile, oldListImage)
                    } finally {
                        tempFile.delete()
                        compressedFile.delete()
                    }
                }
            } else {
                oldListImage
            }

            val list = GameList(
                list_id = gameListId,
                name = listTempData.name,
                description = listTempData.description,
                image = gameImage,
            )

            Repository.updateList(context, list)

            Toast.makeText(context, "List updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteGame(context: Context, game: List_game){
        viewModelScope.launch {
            Repository.deleteListGame(context, game)
        }
        Toast.makeText(context, "Game deleted successfully!", Toast.LENGTH_SHORT).show()
    }

    fun addListGame(context: Context, listGame: List_game){
        viewModelScope.launch {
            Repository.addListGame(context, listGame)
        }
    }
}