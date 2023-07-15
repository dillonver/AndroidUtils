package xyz.dcln.androidutils.utils

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Created by dcl on 2023/7/7.
 */
object ImageUtils {
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(AppUtils.getApp(), id)
    }
}