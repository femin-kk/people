package com.peopledb.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object PhotoStorage {

    private fun photosDir(context: Context): File {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Copies the content at [sourceUri] into app-private storage and returns the absolute file path. */
    fun copyToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = photosDir(context)
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val destFile = File(dir, fileName)
            context.contentResolver.openInputStream(sourceUri).use { input ->
                if (input == null) return null
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
        }
    }
}
