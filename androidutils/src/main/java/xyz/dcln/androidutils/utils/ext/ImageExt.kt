package xyz.dcln.androidutils.utils.ext

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * 将 [Drawable] 转换为 [Bitmap]
 */
fun Drawable.toBitmap(): Bitmap? {
    if (this is BitmapDrawable) {
        // 如果 Drawable 已经是 BitmapDrawable 类型，则直接返回其 Bitmap 对象
        return this.bitmap
    }

    // 创建一个空的 Bitmap，大小与 Drawable 相同
    val bitmap = Bitmap.createBitmap(
        this.intrinsicWidth,
        this.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // 将 Drawable 绘制在 Canvas 上，然后将 Canvas 绘制到 Bitmap 上
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap
}

/**
 * 将 [Bitmap] 转换为 [Drawable]
 */
fun Bitmap.toDrawable(): Drawable {
    // 使用系统资源创建 BitmapDrawable，并传入 Bitmap 对象
    return BitmapDrawable(Resources.getSystem(), this)
}
