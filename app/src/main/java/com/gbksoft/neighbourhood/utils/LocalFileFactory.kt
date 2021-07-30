package com.gbksoft.neighbourhood.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.gbksoft.neighbourhood.model.LocalFile
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class LocalFileFactory(context: Context) {
    companion object {
        const val UNKNOWN_MIME = "application/octet-stream"
    }

    private val contentResolver = context.contentResolver

    @Throws(IllegalArgumentException::class)
    fun <TYPE> fromFile(file: File, dataForMime: Intent? = null, type: TYPE? = null, fileName: String? = null): LocalFile<TYPE> {
        if (!file.isFile) throw IllegalArgumentException("'${file.absolutePath}' is not a file")

        val mime = detectMimeType(file, dataForMime)

        return fromFile(file, mime, type, fileName)
    }

    @Throws(IllegalArgumentException::class)
    fun <TYPE> fromFile(file: File, mimeType: String, type: TYPE? = null, fileName: String? = null): LocalFile<TYPE> {
        if (!file.isFile) throw IllegalArgumentException("'${file.absolutePath}' is not a file")

        val size = file.length()
        val uri = file.toUri()
        val name = fileName ?: file.name

        return LocalFile(uri, name, mimeType, size, type)
    }

    @Throws(IllegalArgumentException::class)
    fun <TYPE> fromUri(fileUri: Uri, type: TYPE? = null, fileName: String? = null): LocalFile<TYPE> {
        val mimeType = contentResolver.getType(fileUri) ?: run {
            throw IllegalArgumentException("'${fileUri.path}' is not a suitable file")
        }
        val info = getUriInfo(fileUri) ?: run {
            throw IllegalArgumentException("'${fileUri.path}' is not a suitable file")
        }

        val size = info.size
        val name = fileName ?: info.name

        return LocalFile(fileUri, name, mimeType, size, type)
    }


    private fun detectMimeType(file: File, data: Intent?): String {
        data?.data?.let { uri ->
            contentResolver.getType(uri)?.let { type ->
                return type
            }
        }

        if (file.extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)?.let {
                if (it.isNotEmpty()) return it
            }
        }
        return UNKNOWN_MIME
    }

    private fun getUriInfo(uri: Uri): UriInfo? {
        Timber.tag("LocalFileTag").d("Uri: ${uri}")
        val fileSize: Long? = try {
            FileUtils.getFileSize(uri, contentResolver)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }

        var fileName: String? = try {
            FileUtils.getFileName(uri, contentResolver)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
        Timber.tag("LocalFileTag").d("fileSize: $fileSize   fileName: $fileName")

        if (fileName == null) fileName = uri.lastPathSegment

        return if (fileSize != null && fileSize != 0L && fileName != null) {
            fileName = FileUtils.removeInvalidCharacters(fileName)
            UriInfo(fileName, fileSize)
        } else {
            null
        }
    }

    private class UriInfo(val name: String, val size: Long)
}