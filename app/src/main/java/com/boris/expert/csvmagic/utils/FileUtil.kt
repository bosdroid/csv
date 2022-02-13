package com.boris.expert.csvmagic.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.*


class FileUtil {

    companion object{
        private const val EOF = -1
        private const val DEFAULT_BUFFER_SIZE = 1024 * 4

        @Throws(IOException::class)
        fun from(context: Context, uri: Uri): File {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
            val fileName: String = getFileName(context, uri)
            val splitName: Array<String> = splitFileName(fileName)
            var tempFile: File = File.createTempFile(splitName[0], splitName[1])
            tempFile = rename(tempFile, fileName)
            tempFile.deleteOnExit()
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(tempFile)
                copy(inputStream, out)
                inputStream.close()
                out.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            return tempFile
        }

        private fun splitFileName(fileName: String): Array<String> {
            var name = fileName
            var extension = ""
            val i = fileName.lastIndexOf(".")
            if (i != -1) {
                name = fileName.substring(0, i)
                extension = fileName.substring(i)
            }
            return arrayOf(name, extension)
        }

        private fun getFileName(context: Context, uri: Uri): String {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (cursor != null) {
                        cursor.close()
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf(File.separator)
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result
        }

        private fun rename(file: File, newName: String): File {
            val newFile = File(file.parent, newName)
            if (!newFile.equals(file)) {
                if (newFile.exists() && newFile.delete()) {
                    Log.d("FileUtil", "Delete old $newName file")
                }
                if (file.renameTo(newFile)) {
                    Log.d("FileUtil", "Rename file to $newName")
                }
            }
            return newFile
        }

        @Throws(IOException::class)
        private fun copy(input: InputStream, output: OutputStream): Long {
            var count: Long = 0
            var n: Int
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (EOF != input.read(buffer).also { n = it }) {
                output.write(buffer, 0, n)
                count += n.toLong()
            }
            return count
        }

        fun getRealPathFromURI(context: Context, uri: Uri): String? {
            when {
                // DocumentProvider
                DocumentsContract.isDocumentUri(context, uri) -> {
                    when {
                        // ExternalStorageProvider
                        isExternalStorageDocument(uri) -> {
                            val docId = DocumentsContract.getDocumentId(uri)
                            val split = docId.split(":").toTypedArray()
                            val type = split[0]
                            // This is for checking Main Memory
                            return if ("primary".equals(type, ignoreCase = true)) {
                                if (split.size > 1) {
                                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                                } else {
                                    Environment.getExternalStorageDirectory().toString() + "/"
                                }
                                // This is for checking SD Card
                            } else {
                                "storage" + "/" + docId.replace(":", "/")
                            }
                        }
                        isDownloadsDocument(uri) -> {
                            val fileName = getFilePath(context, uri)
                            if (fileName != null) {
                                return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                            }
                            var id = DocumentsContract.getDocumentId(uri)
                            if (id.startsWith("raw:")) {
                                id = id.replaceFirst("raw:".toRegex(), "")
                                val file = File(id)
                                if (file.exists()) return id
                            }
                            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                            return getDataColumn(context, contentUri, null, null)
                        }
                        isMediaDocument(uri) -> {
                            val docId = DocumentsContract.getDocumentId(uri)
                            val split = docId.split(":").toTypedArray()
                            val type = split[0]
                            var contentUri: Uri? = null
                            when (type) {
                                "image" -> {
                                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                }
                                "video" -> {
                                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                }
                                "audio" -> {
                                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                }
                            }
                            val selection = "_id=?"
                            val selectionArgs = arrayOf(split[1])
                            return getDataColumn(context, contentUri, selection, selectionArgs)
                        }
                    }
                }
                "content".equals(uri.scheme, ignoreCase = true) -> {
                    // Return the remote address
                    return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
                }
                "file".equals(uri.scheme, ignoreCase = true) -> {
                    return uri.path
                }
            }
            return null
        }

        fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                          selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                if (uri == null) return null
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs,
                    null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }


        fun getFilePath(context: Context, uri: Uri?): String? {
            var cursor: Cursor? = null
            val projection = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            try {
                if (uri == null) return null
                cursor = context.contentResolver.query(uri, projection, null, null,
                    null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }

}