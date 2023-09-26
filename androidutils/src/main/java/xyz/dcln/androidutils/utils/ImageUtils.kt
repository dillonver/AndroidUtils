package xyz.dcln.androidutils.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Created by dcl on 2023/7/7.
 */
object ImageUtils {
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(AppUtils.getApp(), id)
    }

    fun encodeImageToBase64(imageFile: File): String? {
        return if (imageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            encodeBitmapToBase64(bitmap)
        } else {
            null
        }
    }

    fun encodeImageToBase64(imagePath: String): String? {
        return encodeImageToBase64(File(imagePath))
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        // Convert the bitmap to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

        // Encode byte array to Base64 string
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}