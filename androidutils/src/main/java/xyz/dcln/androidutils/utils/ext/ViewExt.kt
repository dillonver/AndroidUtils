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
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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

/**
 * 扩展函数，用于展开View，并添加动画效果
 *
 * @param aniDuration 动画持续时间，默认为300毫秒
 * @param startCallback 展开动画开始时的回调函数
 * @param endCallback 展开动画结束时的回调函数
 * @param errorCallback 错误或异常回调
 */
fun View.expand(
    aniDuration: Long = 300L,
    startCallback: (() -> Unit)? = null,
    endCallback: (() -> Unit)? = null,
    errorCallback: ((reason: String) -> Unit)? = null
) {
    // 如果View已经可见，则直接返回
    if (visibility == View.VISIBLE) {
        //可见，表示已经展开
        errorCallback?.invoke("Error: had expand")
        return
    }

    try {
        // 测量View的大小
        measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = measuredHeight
        val animation = object : Animation() {
            var isAnimationStarted = false
            var isAnimationEnded = false

            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                // 根据插值计算高度
                layoutParams.height = (targetHeight * interpolatedTime).toInt()
                requestLayout()
                if (!isAnimationStarted && interpolatedTime > 0.01f) {
                    // 设置View可见，并执行开始回调
                    visibility = View.VISIBLE
                    startCallback?.invoke()
                    isAnimationStarted = true
                }
                if (!isAnimationEnded && interpolatedTime == 1f) {
                    // 执行结束回调
                    endCallback?.invoke()
                    isAnimationEnded = true
                }
            }
        }
        animation.duration = aniDuration
        startAnimation(animation)
    } catch (e: Exception) {
        errorCallback?.invoke(e.message ?: "Exception: unknown")
    }

}

/**
 * 扩展函数，用于折叠View，并添加动画效果
 *
 * @param aniDuration 动画持续时间，默认为300毫秒
 * @param startCallback 折叠动画开始时的回调函数
 * @param endCallback 折叠动画结束时的回调函数
 * @param errorCallback 错误或异常回调
 */
fun View.collapse(
    aniDuration: Long = 300L,
    startCallback: (() -> Unit)? = null,
    endCallback: (() -> Unit)? = null,
    errorCallback: ((reason: String) -> Unit)? = null

) {
    // 如果View不可见，则直接返回
    if (visibility != View.VISIBLE) {
        //不可见，表示已经折叠
        errorCallback?.invoke("Error: had collapse")
        return
    }
    try {
        val initialHeight = measuredHeight
        val animation = object : Animation() {
            var isAnimationStarted = false
            var isAnimationEnded = false

            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    // 设置View不可见，并执行结束回调
                    visibility = View.GONE
                    if (!isAnimationEnded) {
                        endCallback?.invoke()
                        isAnimationEnded = true
                    }
                } else {
                    // 根据插值计算高度
                    layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    requestLayout()
                    if (!isAnimationStarted) {
                        // 执行开始回调
                        startCallback?.invoke()
                        isAnimationStarted = true
                    }

                }
            }
        }
        animation.duration = aniDuration
        startAnimation(animation)
    } catch (e: Exception) {
        errorCallback?.invoke(e.message ?: "Exception: unknown")
    }
}

/**
 * 滚动 RecyclerView 到末尾位置。
 * @param useSmoothScroll 是否使用平滑滚动。true 表示平滑滚动，false 表示直接滚动到位置。
 */
fun RecyclerView.scrollToEnd(useSmoothScroll: Boolean = false) {
    this.adapter?.let { rv ->
        val itemCount = rv.itemCount
        if (itemCount > 0) {
            if (useSmoothScroll) {
                this.smoothScrollToPosition(itemCount - 1)
            } else {
                this.scrollToPosition(itemCount - 1)
            }
        }
    }
}