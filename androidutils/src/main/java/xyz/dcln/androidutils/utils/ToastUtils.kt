package xyz.dcln.androidutils.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar
import xyz.dcln.androidutils.R
import xyz.dcln.androidutils.utils.AppUtils.isAppForeground
import xyz.dcln.androidutils.utils.CoroutineUtils.launchOnUI


/**
 * Description:
 * Author: Dillon
 * Date: 2023/4/5 21:49
 */
object ToastUtils {
    private var toastInstance: Toast? = null

    data class ToastConfig(
        val context: Context,
        var useCustomToast: Boolean = false,
        var toastDuration: Int = Toast.LENGTH_SHORT,
        @ColorInt var bgColor: Int = Color.BLACK,
        var cornerRadius: Float = 10f,
        var toastXOffset: Int = 0,
        var toastYOffset: Int = 0,
        var toastGravity: Int = Gravity.CENTER,
        val toastTextSize: Float = 16f,
        @ColorInt var toastTextColor: Int = Color.WHITE,
        var leftIcon: Drawable? = null,
        var topIcon: Drawable? = null,
        var rightIcon: Drawable? = null,
        var bottomIcon: Drawable? = null,
        var leftPadding: Float = 10f,
        var rightPadding: Float = 10f,
        var topPadding: Float = 5f,
        var bottomPadding: Float = 5f
    ) {
        fun show(message: String?) {
            if (message.isNullOrEmpty()) return  // Check if message is empty or null

            if (useCustomToast) {
                toastInstance?.cancel()
            }
            val textView = createTextView(message)

            toastInstance = Toast(context).apply {
                duration = toastDuration
                //Starting from Android Build.VERSION_CODES.R, for apps targeting API level Build.VERSION_CODES.R or higher, this method is a no-op when called on text toasts.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    setGravity(toastGravity, toastXOffset, toastYOffset)
                }
                view = textView
                show()
            }
        }

        private fun createTextView(message: CharSequence): TextView {
            return TextView(context).apply {
                text = message
                setTextColor(toastTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, toastTextSize)
                setCompoundDrawablesWithIntrinsicBounds(leftIcon, topIcon, rightIcon, bottomIcon)
                setBackgroundAndPadding()
            }
        }

        private fun TextView.setBackgroundAndPadding() {
            val metrics = context.resources.displayMetrics
            setPadding(
                dpToPx(leftPadding, metrics),
                dpToPx(topPadding, metrics),
                dpToPx(rightPadding, metrics),
                dpToPx(bottomPadding, metrics)
            )

            if (cornerRadius > 0) {
                background = GradientDrawable().apply {
                    setColor(bgColor)
                    cornerRadius = this@ToastConfig.cornerRadius
                }
            } else {
                setBackgroundColor(bgColor)
            }
        }
    }

    data class SnackbarConfig(
        val activity: Activity,
        var duration: Int = Snackbar.LENGTH_SHORT,
        @ColorInt var snarbarTextColor: Int = Color.WHITE,
        var snarbarTextSize: Float = 16f,
        var snarbarGravity: Int = Gravity.CENTER,
        @ColorInt var bgColor: Int = Color.BLACK,
        var cornerRadius: Float = 10f,
        var leftPadding: Float = 10f,
        var rightPadding: Float = 10f,
        var topPadding: Float = 5f,
        var bottomPadding: Float = 5f,
        var snarbarXOffset: Int = 0,
        var snarbarYOffset: Int = 0,
        var leftIcon: Drawable? = null,
        var topIcon: Drawable? = null,
        var rightIcon: Drawable? = null,
        var bottomIcon: Drawable? = null
    ) {
        fun show(message: CharSequence) {
            val snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", duration)
            val customView = createCustomView(message)

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = snarbarGravity
            layoutParams.setMargins(snarbarXOffset, snarbarYOffset, snarbarXOffset, snarbarYOffset)

            snackbar.view.layoutParams = layoutParams
            snackbar.view.background = ColorDrawable(Color.TRANSPARENT)
            (snackbar.view as? Snackbar.SnackbarLayout)?.apply {
                addView(customView)
            }
            snackbar.show()
        }

        private fun createCustomView(message: CharSequence): View {
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val customView = inflater.inflate(R.layout.custom_snackbar, null)

            customView.findViewById<TextView>(R.id.snackbar_text).apply {
                text = message
                setTextColor(snarbarTextColor)
                textSize = this@SnackbarConfig.snarbarTextSize
                setCompoundDrawablesWithIntrinsicBounds(leftIcon, topIcon, rightIcon, bottomIcon)
                val metrics = context.resources.displayMetrics
                setPadding(
                    dpToPx(leftPadding, metrics),
                    dpToPx(topPadding, metrics),
                    dpToPx(rightPadding, metrics),
                    dpToPx(bottomPadding, metrics)
                )
            }

            customView.background = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = this@SnackbarConfig.cornerRadius
            }

            return customView
        }
    }

    private fun dpToPx(dp: Float, metrics: DisplayMetrics): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }

    fun makeToast(context: Context, block: ToastConfig.() -> Unit): ToastConfig {
        return ToastConfig(context).apply(block)
    }

    fun showToast(context: Context, message: String?, duration: Int) {
        makeToast(context) {
            toastDuration = duration
        }.show(message)
    }

    fun cancel() {
        toastInstance?.cancel()
        toastInstance = null
    }

    fun makeSnackbar(activity: Activity, block: SnackbarConfig.() -> Unit): SnackbarConfig {
        return SnackbarConfig(activity).apply(block)
    }

    fun Any.toast(message: String?, duration: Int) {
        if (message.isNullOrEmpty()) return  // Check if message is empty or null

        val topActivity = ActivityUtils.getTopActivity()
        if (ActivityUtils.isActivityValid(topActivity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !AppUtils.isAppForeground()) {
                CoroutineUtils.launchOnUI {
                    topActivity?.let {
                        makeSnackbar(it) {
                            this.duration = duration
                        }.show(message)
                    }
                }
            } else {
                CoroutineUtils.launchOnUI {
                    topActivity?.let { showToast(it, message, duration) }
                }
            }
        }
    }

    fun Any.toastShort(message: String?) {
        if (AppUtils.isAppForeground()) {
            CoroutineUtils.launchOnUI {
                toast(message, Toast.LENGTH_SHORT)
            }
        }
    }

    fun Any.toastLong(message: String?) {
        if (AppUtils.isAppForeground()) {
            CoroutineUtils.launchOnUI {
                toast(message, Toast.LENGTH_LONG)
            }
        }
    }

}