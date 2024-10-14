package com.example.dreamdoodler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.funfactassignment.DrawingDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class DrawingRepository(private val scope: CoroutineScope,
                        private val dao: DrawingDao,
                        private val filesDir: File) {

    val allDrawingPaths = dao.allDrawingPaths()

    // Save method - save drawing to files and filepath to database
    // Taken from https://stackoverflow.com/questions/46666308/how-to-convert-imageview-to-bytearray-in-kotlin
    public fun saveDrawing(filePath: String, bitmap: Bitmap) : Boolean {
        // May have to make it suspend on file saving?
        return try {
            val filename = File(filesDir, filePath)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            filename.writeBytes(stream.toByteArray())

            scope.launch {
                dao.addFilePath(FilePath(filePath))
            }

            true

        } catch (e: Exception) {
                e.printStackTrace()
                false
        }
    }

    // Load method - load drawing from files and filepath from the database
    public fun loadDrawing(filePath: String) : Bitmap? {
        return try {
            val filename = File(filesDir, filePath)

            // This method taken from chat gpt
            BitmapFactory.decodeFile(filename.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}