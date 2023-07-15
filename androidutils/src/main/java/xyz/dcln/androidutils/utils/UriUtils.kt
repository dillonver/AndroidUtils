package xyz.dcln.androidutils.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File


/**
 * Description:
 * Author: Dillon
 * Date: 2023/7/4 1:06
 */
object UriUtils {
    /**
     * Resource to Uri.
     *
     * Example:
     * res2Uri("drawable/icon") -> res2Uri(drawable/icon)
     * res2Uri(R.drawable.icon) -> res2Uri(R.drawable.icon)
     *
     * @param resPath The path of the resource.
     * @return Uri
     */
    fun res2Uri(resPath: String): Uri {
        return Uri.parse("android.resource://" + AppUtils.getApp().packageName + "/" + resPath)
    }

    /**
     * File to Uri.
     *
     * @param file The file.
     * @return Uri
     */
    fun file2Uri(file: File?): Uri? {
        return file?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val authority: String = AppUtils.getApp().packageName + ".androidutils.fileprovider"
                FileProvider.getUriForFile(AppUtils.getApp(), authority, it)
            } else {
                Uri.fromFile(it)
            }
        }
    }
}