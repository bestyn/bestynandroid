package com.gbksoft.neighbourhood.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import java.io.*

object FileCreator {
    fun fileFromContentUri(context: Context, contentUri: Uri): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(context, contentUri)
        val fileName =
                if (contentUri.toString().contains("tempAudioRecord"))
                    "${contentUri.toString().substringAfterLast('/').substringBefore('.')}"
                else
                    "temp_file ${CreateEditPostFragment.audioAttachmentCount}" + if (fileExtension != null) ".$fileExtension" else ""
        CreateEditPostFragment.audioAttachmentCount++

        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        CreateEditPostFragment.filesMap[contentUri.toString()] = tempFile.absolutePath
        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }

}