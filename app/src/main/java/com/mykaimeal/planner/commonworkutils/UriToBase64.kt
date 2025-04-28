package com.mykaimeal.planner.commonworkutils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun UriToBase64(context: Context, imageUri: Uri): String?  {

        try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)

            inputStream?.let {
                val bitmap: Bitmap = BitmapFactory.decodeStream(it) // Convert URI to Bitmap
                it.close()

                // Convert Bitmap to Base64
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

                return Base64.encodeToString(byteArray, Base64.NO_WRAP) // Encode to Base64
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

}