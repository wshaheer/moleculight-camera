package com.moleculight.assessment.services

import android.media.Image
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import com.moleculight.assessment.App
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileService(
    private val image: Image,
    private val file: File
) : Runnable {

    override fun run() {
        var output: FileOutputStream? = null

        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())

        buffer.get(bytes)

        try {
            output = FileOutputStream(file).apply { write(bytes) }
        } catch (e: IOException) {
            Log.e("ImageService", "Failed to write file", e)
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e("ImageService", "Failed to close output stream", e)
                }
            }

            MediaScannerConnection.scanFile(
                App.instance,
                arrayOf(file.absolutePath),
                null
            ) { path, uri -> Log.i("FileService", "onScanCompleted: For path: $path, uri: $uri") }
        }
    }

}
