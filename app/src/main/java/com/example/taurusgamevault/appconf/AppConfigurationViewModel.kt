package com.example.taurusgamevault.appconf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.taurusgamevault.MainActivity
import com.example.taurusgamevault.Model.Repository.Repository
import com.example.taurusgamevault.Model.room.DataBase
import java.io.File

class AppConfigurationViewModel : ViewModel() {

    //backup with custom format(it's basically the .db)
    fun shareDatabase(context: Context) {
        try {
            val dbName = "taurus_game_vault"

            val dbFile = context.getDatabasePath(dbName)

            val tempFile = File(context.cacheDir, "$dbName.tgv")
            dbFile.inputStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Exportar Base de Datos"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // import database via deleting database and copying the new
    fun importDatabase(context: Context, uriNewFile: Uri) {
        try {

            val dbName = "taurus_game_vault"

            DataBase.Companion.getDatabase(context).close()

            val destination = context.getDatabasePath(dbName)

            File(destination.path + "-wal").delete()
            File(destination.path + "-shm").delete()

            context.contentResolver.openInputStream(uriNewFile)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            restartApp(context)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // restart for changes to take effect
    private fun restartApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        Runtime.getRuntime().exit(0)
    }
}