package xyz.dcln.androidutils.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException

object BitmapUtils {
    fun saveLocal(
        context: Context, bitmap: Bitmap?, displayName: String,
        callback: ((result: Boolean, msg: String?, uri: Uri?) -> Unit)? = null
    ) {
        if (bitmap == null) {
            callback?.invoke(false, "error bitmap", null)
            return
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            // 在Android 10或更高版本中，我们不需要指定存储路径；系统会处理
        }

        val resolver = context.contentResolver
        try {
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)) {
                        // 如果压缩并写入成功，调用回调函数，传递成功状态和Uri
                        callback?.invoke(true, "", null)
                    } else {
                        // 如果压缩失败，调用回调函数，传递失败状态和null
                        callback?.invoke(
                            false,
                            "error compress",
                            null
                        )

                    }
                }
            } else {
                // 如果Uri为null，表示插入失败
                callback?.invoke(
                    false,
                    "error uri",
                    null
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // 发生异常，调用回调函数，传递失败状态和null
            callback?.invoke(false, "error exc", null)
        }
    }
}