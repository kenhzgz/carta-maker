package com.example.data

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class SavedFont(
    val id: String,
    val name: String,
    val file: File,
    val filePath: String
)

typealias CustomFontItem = SavedFont

object FontStorageManager {

    suspend fun getCustomFonts(context: Context): List<SavedFont> = getSavedFonts(context)

    suspend fun deleteFont(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getTypeface(file: File): Typeface? = loadTypefaceFromFile(file.absolutePath)

    private const val FONT_DIR_NAME = "custom_fonts"

    private fun getFontDir(context: Context): File {
        val dir = File(context.filesDir, FONT_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun saveFontFromUri(context: Context, sourceUri: Uri): SavedFont? = withContext(Dispatchers.IO) {
        try {
            val fontDir = getFontDir(context)
            val originalName = getFileNameFromUri(context, sourceUri) ?: "fuente_${System.currentTimeMillis()}"
            val cleanName = originalName.substringBeforeLast(".")
            val ext = if (originalName.endsWith(".otf", ignoreCase = true)) "otf" else "ttf"
            val filename = "${cleanName.replace(" ", "_")}_${UUID.randomUUID().toString().take(4)}.$ext"
            val destFile = File(fontDir, filename)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) return@withContext null

            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!destFile.exists() || destFile.length() == 0L) {
                destFile.delete()
                return@withContext null
            }

            // Verify it can be loaded as a Typeface
            try {
                Typeface.createFromFile(destFile)
            } catch (e: Exception) {
                destFile.delete()
                return@withContext null
            }

            SavedFont(
                id = filename,
                name = cleanName,
                file = destFile,
                filePath = destFile.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSavedFonts(context: Context): List<SavedFont> = withContext(Dispatchers.IO) {
        val fontDir = getFontDir(context)
        val files = fontDir.listFiles { file ->
            val ext = file.extension.lowercase()
            file.isFile && (ext == "ttf" || ext == "otf")
        } ?: emptyArray()

        files.map { file ->
            val displayName = file.nameWithoutExtension.substringBeforeLast("_").replace("_", " ")
            SavedFont(
                id = file.name,
                name = if (displayName.isNotBlank()) displayName else file.nameWithoutExtension,
                file = file,
                filePath = file.absolutePath
            )
        }
    }

    suspend fun deleteFont(context: Context, filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadTypefaceFromFile(filePath: String?): Typeface? {
        if (filePath.isNullOrBlank()) return null
        return try {
            val file = File(filePath)
            if (file.exists()) Typeface.createFromFile(file) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }
}
