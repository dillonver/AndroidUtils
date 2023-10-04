package xyz.dcln.androidutils.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by dcl on 2023/7/7.
 */
object ImageUtils {
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(AppUtils.getApp(), id)
    }

    //通知相册扫描指定的图片
    fun notifyGallery(
        context: Context,
        imageFile: File?,
    ) {
        if (imageFile == null) {
            LogUtils.e( "imageFile == null")
            return
        }
        // 通知媒体库更新
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 and above
            MediaScannerConnection.scanFile(
                context,
                arrayOf(imageFile.absolutePath),
                arrayOf("image/png")
            ) { path, uri ->
                // 扫描完成后的操作，如果需要的话
            }
        } else { // Below Android 10
            @Suppress("DEPRECATION")
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = Uri.fromFile(imageFile)
            intent.data = uri
            context.sendBroadcast(intent)
        }

    }

}