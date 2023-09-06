package xyz.dcln.androidutils.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI


/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/5 21:49
 */
object ToastUtils {
    private var toastInstance: Toast? = null

    data class DisplayConfig(
        val context: Context,
        var duration: Int = Toast.LENGTH_SHORT,
        @ColorInt var bgColor: Int = Color.BLACK,
        var cornerRadius: Float = 10f,
        var msg: CharSequence? = null,
        var gravity: Int = Gravity.CENTER,
        var xOffset: Int = 0,
        var yOffset: Int = 0,
        var textSize: Float = 16f,
        @ColorInt var textColor: Int = Color.WHITE,
        var leftIcon: Drawable? = null,
        var topIcon: Drawable? = null,
        var rightIcon: Drawable? = null,
        var bottomIcon: Drawable? = null,
        var leftPadding: Float = 10f,
        var rightPadding: Float = 10f,
        var topPadding: Float = 5f,
        var bottomPadding: Float = 5f,
        var cancelCurrent: Boolean = true
    )

    private fun dpToPx(dp: Float, metrics: DisplayMetrics): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }

    private fun toast(context: Context, block: DisplayConfig.() -> Unit) {
        val config = DisplayConfig(context).apply(block)
        val message = config.msg
        if (message.isNullOrBlank()) {
            LogUtils.w("message.isNullOrBlank")
            return
        }
        if (config.cancelCurrent) {
            toastInstance?.cancel()
        }
        val textView = createTextView(config)
        toastInstance = Toast(context).apply {
            duration = config.duration
            //Starting from Android R (30), the gravity, xOffset, and yOffset parameters in Toast are no longer used due to changes in the platform.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                setGravity(config.gravity, config.xOffset, config.yOffset)
            }
            view = textView
            show()
        }
    }

    private fun createTextView(config: DisplayConfig): TextView {
        return TextView(config.context).apply {
            text = config.msg
            setTextColor(config.textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, config.textSize)
            setCompoundDrawablesWithIntrinsicBounds(
                config.leftIcon,
                config.topIcon,
                config.rightIcon,
                config.bottomIcon
            )
            setBackgroundAndPadding(config)
        }
    }

    private fun TextView.setBackgroundAndPadding(config: DisplayConfig) {
        val metrics = config.context.resources.displayMetrics
        setPadding(
            dpToPx(config.leftPadding, metrics),
            dpToPx(config.topPadding, metrics),
            dpToPx(config.rightPadding, metrics),
            dpToPx(config.bottomPadding, metrics)
        )
        background = GradientDrawable().apply {
            setColor(config.bgColor)
            cornerRadius = config.cornerRadius
        }
    }


    /**
     * 显示自定义的Toast消息。此函数确保Toast消息是从UI线程中显示的，这对于与UI相关的操作是必要的。
     *
     * 使用 [launchOnUI] 协程构建器确保在关联的Activity不再处于前台时，
     * Toast依然可以被显示，因为Toast的显示与Activity的生命周期是独立的。
     *
     * 例 Activity finish的时候，希望toast显示，则使用launchOnUI { showToast{...} }
     *
     * @param context 用于显示Toast的上下文，默认为通过 [AppUtils.getApp] 获取的应用程序上下文。
     * @param block 一个带接收者的lambda，通过 [DisplayConfig] 配置Toast的属性。
     */
    fun showToast(context: Context? = AppUtils.getApp(), block: DisplayConfig.() -> Unit) {
        launchOnUI {
            toast(context ?: AppUtils.getApp(), block)
        }
    }

    fun Any.toastShort(
        message: String?,
        context: Context? = AppUtils.getApp(),
        cancelCurrentToast: Boolean? = true
    ) {
        if (message.isNullOrBlank()) {
            LogUtils.w("message.isNullOrBlank")
            return
        }
        launchOnUI {
            showToast(context ?: AppUtils.getApp()) {
                msg = message
                duration = Toast.LENGTH_SHORT
                cancelCurrent = cancelCurrentToast ?: true
            }
        }
    }

    fun Any.toastLong(
        message: String?,
        context: Context? = AppUtils.getApp(),
        cancelCurrentToast: Boolean? = true
    ) {
        if (message.isNullOrBlank()) {
            LogUtils.w("message.isNullOrBlank")
            return
        }
        launchOnUI {
            showToast(context ?: AppUtils.getApp()) {
                msg = message
                duration = Toast.LENGTH_LONG
                cancelCurrent = cancelCurrentToast ?: true
            }
        }
    }

    fun cancel() {
        toastInstance?.cancel()
        toastInstance = null
    }

}
