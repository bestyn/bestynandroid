package com.gbksoft.neighbourhood.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

object FileUtils {
    private val invalidCharactersRegex = Regex("[\\\\#%&{}<>*?/$!\'\":@+`|=]")
    private val spacesRegex = Regex("\\s+")

    @JvmStatic
    fun toHumanReadableFileSize(fileSizeInBytes: Int): String {
        return toHumanReadableFileSize(fileSizeInBytes.toLong())
    }

    @JvmStatic
    fun toHumanReadableFileSize(fileSizeInBytes: Long): String {
        var bytes = fileSizeInBytes
        var i = -1
        val byteUnits = arrayOf(" kB", " MB", " GB", " TB")
        do {
            bytes /= 1024
            i++
        } while (bytes > 1024)
        return bytes.toDouble().coerceAtLeast(0.1).toString() + byteUnits[i]
    }

    @JvmStatic
    fun getFileSize(file: File): Long {
        return file.length()
    }

    @JvmStatic
    fun getFileName(file: File): String {
        return file.name
    }

    @JvmStatic
    @Throws(FileNotFoundException::class)
    fun getFileSize(uri: Uri, contentResolver: ContentResolver): Long? {
        contentResolver.query(uri, null, null, null, null)
            .use {
                val cursor = it ?: return null
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                return cursor.getLong(sizeIndex)
            }
    }

    @JvmStatic
    @Throws(FileNotFoundException::class)
    fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        contentResolver.query(uri, null, null, null, null)
            .use {
                val cursor = it ?: return null
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                return cursor.getString(nameIndex)
            }
    }

    fun removeInvalidCharacters(fileName: CharSequence): CharSequence {
        return fileName.toString().replace(invalidCharactersRegex, "")
            .replace(spacesRegex, "_")
    }

    fun removeInvalidCharacters(fileName: String): String {
        return fileName.replace(invalidCharactersRegex, "")
            .replace(spacesRegex, "_")
    }
}