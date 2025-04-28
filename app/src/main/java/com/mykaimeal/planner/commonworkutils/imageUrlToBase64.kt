package com.mykaimeal.planner.commonworkutils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/*fun imageUrlToBase64(imageUrl: String): String? {
    return try {
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val inputStream = connection.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Convert Bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        Base64.encodeToString(byteArray, Base64.NO_WRAP) // Encode to Base64
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}*/

suspend fun imageUrlToBase64(imageUrl: String): String? {
    return withContext(Dispatchers.IO) { // Run in background thread
        try {
            var url = URL(imageUrl)
            var connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            while (connection.responseCode in 300..399) { // Handle redirects
                val newUrl = connection.getHeaderField("Location") ?: break
                url = URL(newUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                println("Error: HTTP ${connection.responseCode}")
                return@withContext null
            }

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            inputStream.close()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


/*fun imageUrlToBase64(imageUrl: String): String? {
    return try {
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.doInput = true
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            println("Error: HTTP ${connection.responseCode}")
            return null
        }

        val inputStream = connection.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}*/

