package xyz.dcln.androidutils.utils.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView

/**
 * 设置节流点击事件监听器
 * @param interval 点击事件的最小间隔时间，默认为 500 毫秒
 * @param action 点击事件回调函数
 */
fun View.setThrottleClickListener(interval: Long = 500, action: View.() -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= interval) {
            lastClickTime = currentTime
            action()
        }
    }
}



/**
 * 将 [View] 转换为 [Bitmap]
 */
fun View.toBitmap(): Bitmap {
    // 创建一个与 View 大小相同的空白 Bitmap
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)

    return bitmap
}

// 通过传入圆角大小和背景颜色来设置 View 的圆角效果（包含背景）
fun View.setCornerRadiusAndBackground(context: Context, radius: Float, backgroundColor: Int) {
    val shape = GradientDrawable()
    shape.cornerRadius = radius
    shape.setColor(backgroundColor)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        background = shape
    } else {
        setBackgroundDrawable(shape)
    }
}

// 通过传入圆角大小和背景图片来设置 View 的圆角效果
fun View.setCornerRadiusAndBackground(
    context: Context,
    radius: Float,
    backgroundDrawable: Drawable
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        clipToOutline = true
    }

    // 创建圆角矩形 ShapeDrawable
    val shapeDrawable = ShapeDrawable(
        RoundRectShape(
            floatArrayOf(
                radius,
                radius,
                radius,
                radius,
                radius,
                radius,
                radius,
                radius
            ), null, null
        )
    )
    shapeDrawable.paint.shader = getBackgroundDrawableShader(context, backgroundDrawable)

    background = shapeDrawable
}

// 创建用于绘制背景的 BitmapShader
private fun getBackgroundDrawableShader(
    context: Context,
    backgroundDrawable: Drawable
): BitmapShader {
    // 获取背景图片的 Bitmap
    val bitmap = (backgroundDrawable as? BitmapDrawable)?.bitmap
        ?: Bitmap.createBitmap(
            backgroundDrawable.intrinsicWidth,
            backgroundDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = Canvas(this)
            backgroundDrawable.setBounds(0, 0, canvas.width, canvas.height)
            backgroundDrawable.draw(canvas)
        }

    // 创建 BitmapShader
    return BitmapShader(
        bitmap,
        android.graphics.Shader.TileMode.CLAMP,
        android.graphics.Shader.TileMode.CLAMP
    )
}