package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class SavedImage(
    val id: String,
    val file: File,
    val uriString: String,
    val displayName: String,
    val sizeBytes: Long,
    val createdAt: Long
)

object ImageStorageManager {

    private const val DIRECTORY_NAME = "user_images"

    private fun getImageDir(context: Context): File {
        val dir = File(context.filesDir, DIRECTORY_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun saveImageFromUri(context: Context, sourceUri: Uri): SavedImage? = withContext(Dispatchers.IO) {
        try {
            val imageDir = getImageDir(context)
            val extension = getExtensionFromUri(context, sourceUri) ?: "jpg"
            val filename = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension"
            val destFile = File(imageDir, filename)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) return@withContext null

            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Verify file exists and has content
            if (!destFile.exists() || destFile.length() == 0L) {
                destFile.delete()
                return@withContext null
            }

            SavedImage(
                id = filename,
                file = destFile,
                uriString = Uri.fromFile(destFile).toString(),
                displayName = filename,
                sizeBytes = destFile.length(),
                createdAt = destFile.lastModified()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSavedImages(context: Context): List<SavedImage> = withContext(Dispatchers.IO) {
        val imageDir = getImageDir(context)
        val files = imageDir.listFiles { file ->
            val ext = file.extension.lowercase()
            file.isFile && (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "webp")
        } ?: emptyArray()

        files.sortedByDescending { it.lastModified() }.map { file ->
            SavedImage(
                id = file.name,
                file = file,
                uriString = Uri.fromFile(file).toString(),
                displayName = file.name,
                sizeBytes = file.length(),
                createdAt = file.lastModified()
            )
        }
    }

    suspend fun deleteImage(context: Context, fileUriOrPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = when {
                fileUriOrPath.startsWith("file://") -> File(Uri.parse(fileUriOrPath).path ?: "")
                fileUriOrPath.startsWith("/") -> File(fileUriOrPath)
                else -> File(getImageDir(context), fileUriOrPath)
            }
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getExtensionFromUri(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("png", ignoreCase = true) == true -> "png"
            mimeType?.contains("webp", ignoreCase = true) == true -> "webp"
            mimeType?.contains("jpeg", ignoreCase = true) == true || mimeType?.contains("jpg", ignoreCase = true) == true -> "jpg"
            else -> "jpg"
        }
    }
}
