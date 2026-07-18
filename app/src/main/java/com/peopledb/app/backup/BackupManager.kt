package com.peopledb.app.backup

import android.content.Context
import android.net.Uri
import com.peopledb.app.data.AppDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles exporting the whole app state (Room database + photo files) into a single
 * .zip file the user picks via the system file picker (SAF), and restoring from one.
 */
object BackupManager {

    private const val DB_ENTRY_PREFIX = "db/"
    private const val PHOTOS_ENTRY_PREFIX = "photos/"

    sealed class Result {
        object Success : Result()
        data class Failure(val message: String) : Result()
    }

    fun export(context: Context, destinationUri: Uri): Result {
        return try {
            // Checkpoint WAL so all data is in the main db file before copying.
            val db = AppDatabase.getInstance(context)
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

            val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
            val photosDir = File(context.filesDir, "photos")

            context.contentResolver.openOutputStream(destinationUri)?.use { rawOut ->
                ZipOutputStream(rawOut).use { zipOut ->
                    if (dbFile.exists()) {
                        addFileToZip(zipOut, dbFile, DB_ENTRY_PREFIX + dbFile.name)
                    }
                    if (photosDir.exists()) {
                        photosDir.listFiles()?.forEach { photo ->
                            if (photo.isFile) {
                                addFileToZip(zipOut, photo, PHOTOS_ENTRY_PREFIX + photo.name)
                            }
                        }
                    }
                }
            } ?: return Result.Failure("Could not open destination file")

            Result.Success
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error during export")
        }
    }

    fun restore(context: Context, sourceUri: Uri): Result {
        return try {
            // Close the DB before overwriting its file.
            AppDatabase.closeInstance()

            val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
            dbFile.parentFile?.mkdirs()
            // Remove any -wal/-shm side files from the current DB so they don't apply stale writes.
            File(dbFile.parentFile, "${dbFile.name}-wal").delete()
            File(dbFile.parentFile, "${dbFile.name}-shm").delete()

            val photosDir = File(context.filesDir, "photos")
            if (photosDir.exists()) {
                photosDir.listFiles()?.forEach { it.delete() }
            } else {
                photosDir.mkdirs()
            }

            context.contentResolver.openInputStream(sourceUri)?.use { rawIn ->
                ZipInputStream(rawIn).use { zipIn ->
                    var entry: ZipEntry? = zipIn.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        when {
                            name.startsWith(DB_ENTRY_PREFIX) -> {
                                val target = File(dbFile.parentFile, name.removePrefix(DB_ENTRY_PREFIX))
                                writeEntryTo(zipIn, target)
                            }
                            name.startsWith(PHOTOS_ENTRY_PREFIX) -> {
                                val target = File(photosDir, name.removePrefix(PHOTOS_ENTRY_PREFIX))
                                writeEntryTo(zipIn, target)
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: return Result.Failure("Could not open backup file")

            Result.Success
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error during restore")
        }
    }

    private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryName: String) {
        zipOut.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zipOut) }
        zipOut.closeEntry()
    }

    private fun writeEntryTo(zipIn: ZipInputStream, target: File) {
        target.parentFile?.mkdirs()
        FileOutputStream(target).use { out ->
            zipIn.copyTo(out)
        }
    }
}
